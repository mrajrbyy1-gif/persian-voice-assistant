package com.persianvoice.assistant.domain.repository

import com.persianvoice.assistant.domain.model.Memory
import com.persianvoice.assistant.domain.model.MemoryCategory
import kotlinx.coroutines.flow.Flow

interface MemoryRepository {
    fun observeMemories(): Flow<List<Memory>>
    fun searchMemories(query: String): Flow<List<Memory>>
    suspend fun getRelevantMemories(query: String, limit: Int = 5): List<Memory>
    suspend fun addMemory(key: String, value: String, category: MemoryCategory): Long
    suspend fun deleteMemory(id: Long)
    suspend fun deleteAllMemories()
    suspend fun getMemoryByKey(key: String): Memory?
}