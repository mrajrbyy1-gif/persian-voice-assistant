package com.persianvoice.assistant.core.ai

import com.persianvoice.assistant.core.network.ApiClient
import com.persianvoice.assistant.domain.model.LlmRequest
import com.persianvoice.assistant.domain.model.LlmResponse
import com.persianvoice.assistant.domain.model.TokenUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import javax.inject.Inject

/**
 * OpenAI-compatible API request.
 */
@kotlinx.serialization.Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val temperature: Float = 0.7f,
    val max_tokens: Int = 1000,
    val tools: List<OpenAiTool>? = null,
    val tool_choice: String = "auto",
    val stream: Boolean = false
)

@kotlinx.serialization.Serializable
data class OpenAiMessage(
    val role: String,
    val content: String? = null,
    val tool_calls: List<OpenAiToolCall>? = null,
    val tool_call_id: String? = null,
    val name: String? = null
)

@kotlinx.serialization.Serializable
data class OpenAiTool(
    val type: String = "function",
    val function: OpenAiFunction
)

@kotlinx.serialization.Serializable
data class OpenAiFunction(
    val name: String,
    val description: String,
    val parameters: JsonObject
)

@kotlinx.serialization.Serializable
data class OpenAiToolCall(
    val id: String,
    val type: String = "function",
    val function: OpenAiFunctionCall
)

@kotlinx.serialization.Serializable
data class OpenAiFunctionCall(
    val name: String,
    val arguments: String
)

@kotlinx.serialization.Serializable
data class ChatCompletionResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<Choice>,
    val usage: UsageResponse? = null
)

@kotlinx.serialization.Serializable
data class Choice(
    val index: Int,
    val message: OpenAiMessage,
    val finish_reason: String? = null
)

@kotlinx.serialization.Serializable
data class UsageResponse(
    val prompt_tokens: Int = 0,
    val completion_tokens: Int = 0,
    val total_tokens: Int = 0
)

interface OpenAiCompatibleService {
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}

/**
 * پیاده‌سازی OpenRouter / OpenAI-compatible LLM Provider.
 * کاربر می‌تواند Base URL، API Key و Model را در Settings تنظیم کند.
 */
class OpenAiCompatibleProvider(
    override val id: String,
    override val name: String,
    private val baseUrl: String,
    private val apiKey: String,
    private val defaultModel: String,
    private val apiClient: ApiClient
) : LlmProvider {

    private val service: OpenAiCompatibleService by lazy {
        apiClient.createRetrofit(baseUrl, apiKey, enableLogging = false)
            .create(OpenAiCompatibleService::class.java)
    }

    override suspend fun generate(request: LlmRequest): LlmResponse = withContext(Dispatchers.IO) {
        val apiRequest = request.toApiRequest(defaultModel)
        val response = service.chatCompletions(
            authorization = "Bearer $apiKey",
            request = apiRequest
        )

        val choice = response.choices.firstOrNull()
        val message = choice?.message

        LlmResponse(
            text = message?.content,
            toolCalls = message?.tool_calls?.map { tc ->
                com.persianvoice.assistant.domain.model.ToolCall(
                    id = tc.id,
                    name = tc.function.name,
                    arguments = parseArguments(tc.function.arguments)
                )
            } ?: emptyList(),
            finishReason = choice?.finish_reason,
            usage = response.usage?.let {
                TokenUsage(
                    promptTokens = it.prompt_tokens,
                    completionTokens = it.completion_tokens,
                    totalTokens = it.total_tokens
                )
            }
        )
    }

    override suspend fun stream(
        request: LlmRequest,
        onChunk: suspend (String) -> Unit
    ): LlmResponse {
        // Streaming در فاز 34 با OkHttp EventSource پیاده‌سازی می‌شود
        val response = generate(request)
        response.text?.let { onChunk(it) }
        return response
    }

    override suspend fun isAvailable(): Boolean =
        apiKey.isNotBlank()

    private fun parseArguments(json: String): Map<String, String> {
        return try {
            val parsed = apiClient.json.parseToJsonElement(json).jsonObject
            parsed.mapValues { (_, value) ->
                value.toString().trim('"')
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private val kotlinx.serialization.json.JsonElement.jsonObject: JsonObject
        get() = this as JsonObject

    private fun LlmRequest.toApiRequest(default: String): ChatCompletionRequest {
        val msgs = messages.map { msg ->
            OpenAiMessage(
                role = when (msg.role) {
                    com.persianvoice.assistant.domain.model.MessageRole.USER -> "user"
                    com.persianvoice.assistant.domain.model.MessageRole.ASSISTANT -> "assistant"
                    com.persianvoice.assistant.domain.model.MessageRole.SYSTEM -> "system"
                    com.persianvoice.assistant.domain.model.MessageRole.TOOL -> "tool"
                },
                content = msg.content,
                tool_call_id = msg.toolCallId,
                name = msg.name
            )
        }

        val tools = tools.takeIf { it.isNotEmpty() }?.map { def ->
            OpenAiTool(
                type = "function",
                function = OpenAiFunction(
                    name = def.name,
                    description = def.description,
                    parameters = buildJsonObject {
                        put("type", def.parameters.type)
                        putJsonObject("properties") {
                            def.parameters.properties.forEach { (key, prop) ->
                                putJsonObject(key) {
                                    put("type", prop.type)
                                    put("description", prop.description)
                                    prop.enum?.let { enum ->
                                        putJsonArray("enum") {
                                            enum.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) }
                                        }
                                    }
                                }
                            }
                        }
                        putJsonArray("required") {
                            def.parameters.required.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) }
                        }
                    }
                )
            )
        }

        return ChatCompletionRequest(
            model = model ?: default,
            messages = msgs,
            temperature = temperature,
            max_tokens = maxTokens,
            tools = tools,
            stream = false
        )
    }
}