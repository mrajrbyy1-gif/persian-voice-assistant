package com.persianvoice.assistant.core.ai

import com.persianvoice.assistant.core.common.AppLogger
import com.persianvoice.assistant.domain.model.AgentResult
import com.persianvoice.assistant.domain.model.AssistantState
import com.persianvoice.assistant.domain.model.ChatMessage
import com.persianvoice.assistant.domain.model.MessageRole
import com.persianvoice.assistant.domain.model.ToolCall
import com.persianvoice.assistant.domain.repository.ConversationRepository
import com.persianvoice.assistant.domain.repository.MemoryRepository
import com.persianvoice.assistant.domain.repository.SettingsRepository
import com.persianvoice.assistant.domain.usecase.DetectIntentUseCase
import com.persianvoice.assistant.domain.usecase.ConfirmationManager
import com.persianvoice.assistant.tools.AgentEngine
import com.persianvoice.assistant.tools.ToolRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * موتور مرکزی Conversation - State Machine اصلی.
 *
 * جریان:
 * 1. دریافت متن از STT
 * 2. تشخیص Intent محلی
 * 3. اگر Intent ساده بود → اجرای مستقیم (مثل باتری، ساعت)
 * 4. در غیر این صورت → ارسال به LLM
 * 5. اگر LLM Tool فراخوانی کرد → اجرای Tool و بازگشت نتیجه به LLM
 * 6. پاسخ نهایی به TTS
 */
@Singleton
class ConversationEngine @Inject constructor(
    private val stt: com.persianvoice.assistant.core.voice.SpeechToTextService,
    private val tts: com.persianvoice.assistant.core.voice.TextToSpeechService,
    private val llmRouter: LlmRouter,
    private val toolRegistry: ToolRegistry,
    private val agentEngine: AgentEngine,
    private val conversationRepository: ConversationRepository,
    private val memoryRepository: MemoryRepository,
    private val settingsRepository: SettingsRepository,
    private val detectIntent: DetectIntentUseCase,
    private val confirmationManager: ConfirmationManager,
    private val logger: AppLogger
) {

    private val _state = MutableStateFlow(AssistantState.IDLE)
    val state: StateFlow<AssistantState> = _state.asStateFlow()

    private val _currentResponse = MutableStateFlow("")
    val currentResponse: StateFlow<String> = _currentResponse.asStateFlow()

    private var currentConversationId: Long = 0L

    suspend fun initialize() {
        // ایجاد یا بارگذاری آخرین مکالمه
        val last = conversationRepository.getLastConversation()
        currentConversationId = last?.id ?: conversationRepository.createConversation("مکالمه جدید")
    }

    suspend fun processUserInput(text: String): AgentResult {
        if (text.isBlank()) {
            return AgentResult.Error("متن خالی است")
        }

        _state.value = AssistantState.PROCESSING
        conversationRepository.addMessage(currentConversationId, MessageRole.USER, text)

        return try {
            // مرحله ۱: تشخیص Intent محلی
            val intentMatch = detectIntent(text)
            logger.info("Intent detected: ${intentMatch.intent} (confidence: ${intentMatch.confidence})")

            // مرحله ۲: اگر intent ساده بود، Tool مستقیم اجرا شود
            val directResult = tryDirectToolExecution(intentMatch, text)
            if (directResult != null) {
                _currentResponse.value = directResult.text
                conversationRepository.addMessage(currentConversationId, MessageRole.ASSISTANT, directResult.text)
                _state.value = AssistantState.SPEAKING
                speakAsync(directResult.text)
                _state.value = AssistantState.IDLE
                return directResult
            }

            // مرحله ۳: ارسال به LLM
            _state.value = AssistantState.PROCESSING
            val response = processWithLlm(text)
            conversationRepository.addMessage(currentConversationId, MessageRole.ASSISTANT, response.text ?: "")

            _currentResponse.value = response.text ?: ""
            _state.value = AssistantState.SPEAKING
            response.text?.let { speakAsync(it) }

            _state.value = AssistantState.IDLE
            response
        } catch (e: Exception) {
            logger.error("Process failed", e)
            _state.value = AssistantState.ERROR
            AgentResult.Error(e.message ?: "خطای ناشناخته")
        }
    }

    private suspend fun tryDirectToolExecution(
        intentMatch: com.persianvoice.assistant.domain.model.IntentMatch,
        originalText: String
    ): AgentResult? {
        val toolName = when (intentMatch.intent) {
            com.persianvoice.assistant.domain.model.AssistantIntent.GET_BATTERY -> "device.battery"
            com.persianvoice.assistant.domain.model.AssistantIntent.GET_TIME -> "device.time"
            com.persianvoice.assistant.domain.model.AssistantIntent.CALCULATE -> "calculator.calculate"
            else -> return null
        }

        val tool = toolRegistry.get(toolName) ?: return null

        val args = if (toolName == "calculator.calculate") {
            mapOf("expression" to extractMathExpression(originalText))
        } else emptyMap()

        val toolCall = ToolCall(id = "direct_${System.currentTimeMillis()}", name = toolName, arguments = args.mapValues { it.value.toString() })
        return agentEngine.executeToolCall(toolCall)
    }

    private fun extractMathExpression(text: String): String {
        // استخراج عبارت ریاضی از متن
        val mathRegex = Regex("[0-9۰-۹٠-٩+\\-*/().×÷\\s]+")
        val matches = mathRegex.findAll(text)
        return matches.map { it.value.trim() }.joinToString("")
    }

    private suspend fun processWithLlm(text: String): com.persianvoice.assistant.domain.model.LlmResponse {
        // آماده‌سازی context
        val memories = memoryRepository.getRelevantMemories(text, 3)
        val memoryContext = if (memories.isNotEmpty()) {
            memories.joinToString("\n") { "- ${it.key}: ${it.value}" }
        } else ""

        val systemPrompt = PersianAssistantSystemPrompt.BASE_PROMPT +
                if (memoryContext.isNotBlank()) "\n\n## اطلاعات به‌خاطر سپرده‌شده:\n$memoryContext" else ""

        val messages = listOf(
            ChatMessage(role = MessageRole.SYSTEM, content = systemPrompt),
            ChatMessage(role = MessageRole.USER, content = text)
        )

        val request = com.persianvoice.assistant.domain.model.LlmRequest(
            messages = messages,
            tools = toolRegistry.getAllDefinitions(),
            model = settingsRepository.getActiveProvider()?.model,
            temperature = settingsRepository.getActiveProvider()?.temperature ?: 0.7f
        )

        var response = llmRouter.generate(request)

        // اجرای Tool ها در صورت نیاز
        while (response.toolCalls.isNotEmpty()) {
            _state.value = AssistantState.EXECUTING_TOOL
            val toolMessages = mutableListOf<ChatMessage>()

            for (toolCall in response.toolCalls) {
                val result = agentEngine.executeToolCall(toolCall)
                val resultText = when (result) {
                    is AgentResult.Response -> result.text
                    is AgentResult.Error -> "خطا: ${result.message}"
                    is AgentResult.ToolExecution -> "نیاز به تأیید کاربر"
                }
                toolMessages.add(
                    ChatMessage(
                        role = MessageRole.TOOL,
                        content = resultText,
                        toolCallId = toolCall.id,
                        name = toolCall.name
                    )
                )
            }

            // ادامه گفتگو با LLM
            val followUpRequest = request.copy(
                messages = messages + ChatMessage(MessageRole.ASSISTANT, response.text ?: "", toolCallId = response.toolCalls.firstOrNull()?.id) + toolMessages
            )
            response = llmRouter.generate(followUpRequest)
        }

        return response
    }

    private fun speakAsync(text: String) {
        // در یک Coroutine Scope جدا اجرا می‌شود
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                tts.speak(text, "fa-IR")
            } catch (e: Exception) {
                logger.warning("TTS failed: ${e.message}")
            }
        }
    }

    suspend fun stopSpeaking() {
        tts.stop()
        _state.value = AssistantState.IDLE
    }
}