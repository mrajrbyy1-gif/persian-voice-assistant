package com.persianvoice.assistant.domain.model

/**
 * نتیجه تشخیص گفتار - از SpeechToTextService
 */
data class SpeechResult(
    val text: String,
    val isFinal: Boolean,
    val confidence: Float? = null,
    val language: String = "fa-IR"
)

/**
 * درخواست LLM
 */
data class LlmRequest(
    val messages: List<ChatMessage>,
    val tools: List<ToolDefinition> = emptyList(),
    val model: String? = null,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 1000,
    val stream: Boolean = false
)

data class ChatMessage(
    val role: MessageRole,
    val content: String,
    val name: String? = null,
    val toolCallId: String? = null
)

/**
 * پاسخ LLM
 */
data class LlmResponse(
    val text: String?,
    val toolCalls: List<ToolCall> = emptyList(),
    val finishReason: String? = null,
    val usage: TokenUsage? = null
)

data class TokenUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0
)

/**
 * فراخوانی Tool از LLM
 */
data class ToolCall(
    val id: String,
    val name: String,
    val arguments: Map<String, String>
)

/**
 * تعریف Tool برای ارسال به LLM (OpenAI-compatible format)
 */
data class ToolDefinition(
    val name: String,
    val description: String,
    val parameters: ToolParameters
)

data class ToolParameters(
    val type: String = "object",
    val properties: Map<String, ToolProperty> = emptyMap(),
    val required: List<String> = emptyList()
)

data class ToolProperty(
    val type: String,
    val description: String,
    val enum: List<String>? = null
)

/**
 * Intent های پایه - قبل از ارسال به LLM تشخیص داده می‌شوند
 */
enum class AssistantIntent {
    CALL_CONTACT,
    SEND_SMS,
    SET_ALARM,
    CREATE_EVENT,
    OPEN_APP,
    SEARCH_WEB,
    GET_WEATHER,
    CALCULATE,
    GET_BATTERY,
    GET_TIME,
    STOP_SPEAKING,
    GENERAL_CHAT,
    UNKNOWN
}

/**
 * نتیجه تشخیص Intent محلی
 */
data class IntentMatch(
    val intent: AssistantIntent,
    val confidence: Float,
    val arguments: Map<String, String> = emptyMap()
)

/**
 * نتیجه اجرای Agent Engine
 */
sealed class AgentResult {
    data class Response(val text: String) : AgentResult()
    data class ToolExecution(val tool: ToolCall) : AgentResult()
    data class Error(val message: String) : AgentResult()
}

/**
 * Confirmation Action - برای عملیات حساس
 */
sealed class ConfirmationAction {
    val description: String
    val riskLevel: RiskLevel

    data class CallAction(val contactName: String, val phoneNumber: String) : ConfirmationAction() {
        override val description = "تماس با $contactName"
        override val riskLevel = RiskLevel.HIGH
    }

    data class SmsAction(val contactName: String, val phoneNumber: String, val message: String) : ConfirmationAction() {
        override val description = "ارسال پیام به $contactName"
        override val riskLevel = RiskLevel.HIGH
    }

    data class DeleteAction(val target: String) : ConfirmationAction() {
        override val description = "حذف $target"
        override val riskLevel = RiskLevel.HIGH
    }

    data class AppLaunchAction(val appName: String, val packageName: String) : ConfirmationAction() {
        override val description = "باز کردن $appName"
        override val riskLevel = RiskLevel.LOW
    }

    data class CalendarEventAction(val title: String, val dateTime: Long) : ConfirmationAction() {
        override val description = "ایجاد رویداد $title"
        override val riskLevel = RiskLevel.MEDIUM
    }

    data class AlarmAction(val label: String, val timeInMillis: Long) : ConfirmationAction() {
        override val description = "تنظیم آلارم $label"
        override val riskLevel = RiskLevel.MEDIUM
    }

    data class GenericAction(val description: String, val riskLevel: RiskLevel) : ConfirmationAction()
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Weather data
 */
data class WeatherData(
    val temperature: Double,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val city: String,
    val icon: String? = null
)

/**
 * Search Result
 */
data class SearchResult(
    val title: String,
    val snippet: String,
    val url: String
)

/**
 * Contact data
 */
data class Contact(
    val id: Long,
    val name: String,
    val phoneNumbers: List<String>,
    val email: String? = null
)

/**
 * Calendar Event
 */
data class CalendarEventData(
    val id: Long = 0,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long,
    val location: String? = null
)

/**
 * AI Provider configuration
 */
data class AiProviderConfig(
    val id: String,
    val name: String,
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 1000,
    val enabled: Boolean = true
)