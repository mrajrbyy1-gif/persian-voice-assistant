package com.persianvoice.assistant.domain.usecase

import com.persianvoice.assistant.domain.repository.ToolRepository
import com.persianvoice.assistant.domain.model.ToolExecution
import com.persianvoice.assistant.domain.model.ToolResultData
import javax.inject.Inject

class LogToolExecutionUseCase @Inject constructor(
    private val toolRepository: ToolRepository
) {
    suspend operator fun invoke(
        toolId: String,
        arguments: Map<String, Any?>,
        result: ToolResultData,
        durationMs: Long
    ) {
        toolRepository.logExecution(
            ToolExecution(
                toolId = toolId,
                arguments = arguments.toString(),
                result = result,
                durationMs = durationMs
            )
        )
    }
}