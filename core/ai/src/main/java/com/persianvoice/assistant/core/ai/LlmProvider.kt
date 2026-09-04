package com.persianvoice.assistant.core.ai

import com.persianvoice.assistant.domain.model.LlmRequest
import com.persianvoice.assistant.domain.model.LlmResponse

/**
 * Interface LLM Provider - پیاده‌سازی‌های مختلف (OpenRouter, OpenCode, Local, Custom) قابل تعویض.
 */
interface LlmProvider {

    val id: String
    val name: String

    suspend fun generate(request: LlmRequest): LlmResponse

    suspend fun stream(
        request: LlmRequest,
        onChunk: suspend (String) -> Unit
    ): LlmResponse

    suspend fun isAvailable(): Boolean
}