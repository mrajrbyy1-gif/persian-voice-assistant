package com.persianvoice.assistant.domain.repository

import com.persianvoice.assistant.domain.model.Conversation
import com.persianvoice.assistant.domain.model.Message
import com.persianvoice.assistant.domain.model.MessageRole
import kotlinx.coroutines.flow.Flow

/**
 * Repository مکالمات - UI فقط از این Interface استفاده می‌کند.
 */
interface ConversationRepository {
    fun observeConversations(): Flow<List<Conversation>>
    fun observeMessages(conversationId: Long): Flow<List<Message>>
    suspend fun getConversation(id: Long): Conversation?
    suspend fun createConversation(title: String): Long
    suspend fun addMessage(conversationId: Long, role: MessageRole, content: String): Long
    suspend fun deleteConversation(id: Long)
    suspend fun deleteAllConversations()
    suspend fun getLastConversation(): Conversation?
}