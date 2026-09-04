package com.persianvoice.assistant.data.repository

import com.persianvoice.assistant.data.datasource.ConversationDao
import com.persianvoice.assistant.data.datasource.MessageDao
import com.persianvoice.assistant.data.model.ConversationEntity
import com.persianvoice.assistant.data.model.MessageEntity
import com.persianvoice.assistant.domain.model.Conversation
import com.persianvoice.assistant.domain.model.Message
import com.persianvoice.assistant.domain.model.MessageRole
import com.persianvoice.assistant.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) : ConversationRepository {

    override fun observeConversations(): Flow<List<Conversation>> =
        conversationDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeMessages(conversationId: Long): Flow<List<Message>> =
        messageDao.observeByConversation(conversationId).map { list -> list.map { it.toDomain() } }

    override suspend fun getConversation(id: Long): Conversation? =
        conversationDao.getById(id)?.toDomain()

    override suspend fun createConversation(title: String): Long {
        val entity = ConversationEntity(title = title)
        return conversationDao.insert(entity)
    }

    override suspend fun addMessage(
        conversationId: Long,
        role: MessageRole,
        content: String
    ): Long {
        val entity = MessageEntity(
            conversationId = conversationId,
            role = role.name,
            content = content
        )
        val id = messageDao.insert(entity)
        conversationDao.touch(conversationId)
        return id
    }

    override suspend fun deleteConversation(id: Long) {
        messageDao.deleteByConversation(id)
        conversationDao.delete(id)
    }

    override suspend fun deleteAllConversations() {
        messageDao.deleteAll()
        conversationDao.deleteAll()
    }

    override suspend fun getLastConversation(): Conversation? =
        conversationDao.getLast()?.toDomain()

    private fun ConversationEntity.toDomain() = Conversation(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun MessageEntity.toDomain() = Message(
        id = id,
        conversationId = conversationId,
        role = runCatching { MessageRole.valueOf(role) }.getOrDefault(MessageRole.USER),
        content = content,
        timestamp = timestamp,
        toolCallId = toolCallId,
        toolName = toolName
    )
}