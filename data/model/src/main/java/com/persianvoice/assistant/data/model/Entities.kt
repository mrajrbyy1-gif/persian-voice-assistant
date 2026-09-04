package com.persianvoice.assistant.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity مکالمه در Room.
 */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Entity پیام.
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolCallId: String? = null,
    val toolName: String? = null
)

/**
 * Entity حافظه.
 */
@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val value: String,
    val category: String,
    val relevance: Float = 1.0f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Entity تنظیمات کاربر.
 */
@Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Entity وظیفه.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val status: String,
    val result: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

/**
 * Entity لاگ اجرای Tool.
 */
@Entity(tableName = "tool_executions")
data class ToolExecutionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val toolId: String,
    val arguments: String,
    val resultType: String,
    val resultData: String,
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)