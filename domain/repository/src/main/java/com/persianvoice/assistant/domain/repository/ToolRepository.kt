package com.persianvoice.assistant.domain.repository

import com.persianvoice.assistant.domain.model.TaskRecord
import com.persianvoice.assistant.domain.model.TaskStatus
import com.persianvoice.assistant.domain.model.ToolExecution
import kotlinx.coroutines.flow.Flow

interface ToolRepository {
    fun observeExecutions(): Flow<List<ToolExecution>>
    fun observeTasks(): Flow<List<TaskRecord>>
    suspend fun logExecution(execution: ToolExecution)
    suspend fun createTask(description: String): Long
    suspend fun updateTaskStatus(taskId: Long, status: TaskStatus, result: String? = null)
    suspend fun deleteAllHistory()
}