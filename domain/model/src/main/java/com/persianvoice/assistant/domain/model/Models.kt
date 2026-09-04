package com.persianvoice.assistant.domain.model

/**
 * State های دستیار - State Machine مرکزی
 * UI براساس این state تغییر می‌کند.
 */
enum class AssistantState {
    IDLE,
    LISTENING,
    PROCESSING,
    EXECUTING_TOOL,
    SPEAKING,
    ERROR
}

/**
 * Role پیام در مکالمه
 */
enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM,
    TOOL
}

/**
 * Message در مکالمه
 */
data class Message(
    val id: Long = 0,
    val conversationId: Long,
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolCallId: String? = null,
    val toolName: String? = null
)

/**
 * Conversation (مکالمه)
 */
data class Conversation(
    val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messageCount: Int = 0
)

/**
 * User Preference
 */
data class UserPreference(
    val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Memory - Long term memory برای دستیار
 */
data class Memory(
    val id: Long = 0,
    val key: String,
    val value: String,
    val category: MemoryCategory,
    val relevance: Float = 1.0f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class MemoryCategory {
    USER_INFO,
    PREFERENCE,
    TASK,
    FACT,
    RELATIONSHIP
}

/**
 * Task History - وظایف انجام‌شده
 */
data class TaskRecord(
    val id: Long = 0,
    val description: String,
    val status: TaskStatus,
    val result: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

enum class TaskStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Tool Execution - لاگ tool های اجراشده
 */
data class ToolExecution(
    val id: Long = 0,
    val toolId: String,
    val arguments: String,
    val result: ToolResultData,
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class ToolResultData {
    data class Success(val data: String) : ToolResultData()
    data class Failure(val error: String) : ToolResultData()
    data object RequiresPermission : ToolResultData()
}