package com.persianvoice.assistant.data.repository

import com.persianvoice.assistant.data.datasource.MemoryDao
import com.persianvoice.assistant.data.model.MemoryEntity
import com.persianvoice.assistant.domain.model.Memory
import com.persianvoice.assistant.domain.model.MemoryCategory
import com.persianvoice.assistant.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepositoryImpl @Inject constructor(
    private val memoryDao: MemoryDao
) : MemoryRepository {

    override fun observeMemories(): Flow<List<Memory>> =
        memoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun searchMemories(query: String): Flow<List<Memory>> =
        memoryDao.search(query).let { kotlinx.coroutines.flow.flowOf(it) }
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getRelevantMemories(query: String, limit: Int): List<Memory> =
        memoryDao.search(query, limit).map { it.toDomain() }

    override suspend fun addMemory(key: String, value: String, category: MemoryCategory): Long {
        val entity = MemoryEntity(
            key = key,
            value = value,
            category = category.name
        )
        return memoryDao.insert(entity)
    }

    override suspend fun deleteMemory(id: Long) = memoryDao.delete(id)

    override suspend fun deleteAllMemories() = memoryDao.deleteAll()

    override suspend fun getMemoryByKey(key: String): Memory? =
        memoryDao.getByKey(key)?.toDomain()

    private fun MemoryEntity.toDomain() = Memory(
        id = id,
        key = key,
        value = value,
        category = runCatching { MemoryCategory.valueOf(category) }.getOrDefault(MemoryCategory.FACT),
        relevance = relevance,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}