package com.persianvoice.assistant.data.datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.persianvoice.assistant.data.model.ConversationEntity
import com.persianvoice.assistant.data.model.MessageEntity
import com.persianvoice.assistant.data.model.MemoryEntity
import com.persianvoice.assistant.data.model.TaskEntity
import com.persianvoice.assistant.data.model.ToolExecutionEntity
import com.persianvoice.assistant.data.model.UserPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: Long): ConversationEntity?

    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLast(): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: ConversationEntity): Long

    @Update
    suspend fun update(conversation: ConversationEntity)

    @Query("UPDATE conversations SET updatedAt = :timestamp WHERE id = :id")
    suspend fun touch(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM conversations")
    suspend fun deleteAll()
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun observeByConversation(conversationId: Long): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId")
    suspend fun countByConversation(conversationId: Long): Int

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(conversationId: Long, limit: Int = 20): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity): Long

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteByConversation(conversationId: Long)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE key = :key LIMIT 1")
    suspend fun getByKey(key: String): MemoryEntity?

    @Query("SELECT * FROM memories WHERE key LIKE '%' || :query || '%' OR value LIKE '%' || :query || '%' ORDER BY relevance DESC LIMIT :limit")
    suspend fun search(query: String, limit: Int = 10): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: MemoryEntity): Long

    @Update
    suspend fun update(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM memories")
    suspend fun deleteAll()
}

@Dao
interface UserPreferenceDao {
    @Query("SELECT * FROM user_preferences")
    fun observeAll(): Flow<List<UserPreferenceEntity>>

    @Query("SELECT * FROM user_preferences WHERE key = :key")
    suspend fun get(key: String): UserPreferenceEntity?

    @Query("SELECT value FROM user_preferences WHERE key = :key")
    suspend fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pref: UserPreferenceEntity)

    @Query("DELETE FROM user_preferences WHERE key = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM user_preferences")
    suspend fun deleteAll()
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status, result = :result, completedAt = :completedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, result: String?, completedAt: Long? = System.currentTimeMillis())

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}

@Dao
interface ToolExecutionDao {
    @Query("SELECT * FROM tool_executions ORDER BY timestamp DESC LIMIT 100")
    fun observeRecent(): Flow<List<ToolExecutionEntity>>

    @Insert
    suspend fun insert(execution: ToolExecutionEntity): Long

    @Query("DELETE FROM tool_executions")
    suspend fun deleteAll()
}