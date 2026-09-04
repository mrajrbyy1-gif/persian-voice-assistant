package com.persianvoice.assistant.data.repository

import com.persianvoice.assistant.data.datasource.TaskDao
import com.persianvoice.assistant.data.datasource.ToolExecutionDao
import com.persianvoice.assistant.data.model.TaskEntity
import com.persianvoice.assistant.data.model.ToolExecutionEntity
import com.persianvoice.assistant.domain.model.TaskRecord
import com.persianvoice.assistant.domain.model.TaskStatus
import com.persianvoice.assistant.domain.model.ToolExecution
import com.persianvoice.assistant.domain.model.ToolResultData
import com.persianvoice.assistant.domain.repository.ToolRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val toolExecutionDao: ToolExecutionDao
) : ToolRepository {

    override fun observeExecutions(): Flow<List<ToolExecution>> =
        toolExecutionDao.observeRecent().map { list -> list.map { it.toDomain() } }

    override fun observeTasks(): Flow<List<TaskRecord>> =
        taskDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun logExecution(execution: ToolExecution) {
        val (type, data) = when (val r = execution.result) {
            is ToolResultData.Success -> "success" to r.data
            is ToolResultData.Failure -> "failure" to r.error
            ToolResultData.RequiresPermission -> "permission" to "permission_required"
        }
        toolExecutionDao.insert(
            ToolExecutionEntity(
                toolId = execution.toolId,
                arguments = execution.arguments,
                resultType = type,
                resultData = data,
                durationMs = execution.durationMs,
                timestamp = execution.timestamp
            )
        )
    }

    override suspend fun createTask(description: String): Long {
        return taskDao.insert(
            TaskEntity(
                description = description,
                status = TaskStatus.PENDING.name
            )
        )
    }

    override suspend fun updateTaskStatus(taskId: Long, status: TaskStatus, result: String?) {
        taskDao.updateStatus(
            id = taskId,
            status = status.name,
            result = result,
            completedAt = if (status == TaskStatus.COMPLETED || status == TaskStatus.FAILED)
                System.currentTimeMillis() else null
        )
    }

    override suspend fun deleteAllHistory() {
        taskDao.deleteAll()
        toolExecutionDao.deleteAll()
    }

    private fun TaskEntity.toDomain() = TaskRecord(
        id = id,
        description = description,
        status = runCatching { TaskStatus.valueOf(status) }.getOrDefault(TaskStatus.PENDING),
        result = result,
        createdAt = createdAt,
        completedAt = completedAt
    )

    private fun ToolExecutionEntity.toDomain(): ToolExecution {
        val result: ToolResultData = when (resultType) {
            "success" -> ToolResultData.Success(resultData)
            "failure" -> ToolResultData.Failure(resultData)
            "permission" -> ToolResultData.RequiresPermission
            else -> ToolResultData.Failure("unknown")
        }
        return ToolExecution(
            id = id,
            toolId = toolId,
            arguments = arguments,
            result = result,
            durationMs = durationMs,
            timestamp = timestamp
        )
    }
}