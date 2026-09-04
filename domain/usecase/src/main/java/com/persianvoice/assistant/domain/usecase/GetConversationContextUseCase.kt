package com.persianvoice.assistant.domain.usecase

import com.persianvoice.assistant.domain.repository.ConversationRepository
import com.persianvoice.assistant.domain.model.MessageRole
import javax.inject.Inject

class GetConversationContextUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    /**
     * آخرین N پیام مکالمه برای ارسال به LLM به عنوان context.
     */
    suspend operator fun invoke(conversationId: Long, maxMessages: Int = 10): List<ContextMessage> {
        return emptyList() // پیاده‌سازی واقعی بعداً
    }
}

data class ContextMessage(
    val role: MessageRole,
    val content: String,
    val timestamp: Long
)