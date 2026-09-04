package com.persianvoice.assistant.domain.usecase

import com.persianvoice.assistant.domain.model.AgentResult
import com.persianvoice.assistant.domain.repository.AiRepository
import javax.inject.Inject

class ProcessWithAgentUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {
    suspend operator fun invoke(input: String, conversationId: Long): AgentResult {
        return aiRepository.process(input, conversationId)
    }
}