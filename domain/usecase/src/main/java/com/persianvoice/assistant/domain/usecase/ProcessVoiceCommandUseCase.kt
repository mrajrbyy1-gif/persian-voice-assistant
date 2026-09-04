package com.persianvoice.assistant.domain.usecase

/**
 * UseCase اصلی - پردازش دستور صوتی و برگرداندن پاسخ.
 */
interface ProcessVoiceCommandUseCase {
    suspend operator fun invoke(text: String, conversationId: Long): ProcessResult
}

sealed class ProcessResult {
    data class Response(val text: String, val speakable: String = text) : ProcessResult()
    data class ToolExecuted(val toolName: String, val result: String) : ProcessResult()
    data class NeedsConfirmation(val description: String) : ProcessResult()
    data class Error(val message: String) : ProcessResult()
}