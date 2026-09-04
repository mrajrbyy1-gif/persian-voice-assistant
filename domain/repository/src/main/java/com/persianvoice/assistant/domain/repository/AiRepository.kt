package com.persianvoice.assistant.domain.repository

import com.persianvoice.assistant.domain.model.AgentResult

interface AiRepository {
    suspend fun process(input: String, conversationId: Long): AgentResult
    suspend fun streamProcess(
        input: String,
        conversationId: Long,
        onPartial: suspend (String) -> Unit
    ): AgentResult
    suspend fun stopGeneration()
}