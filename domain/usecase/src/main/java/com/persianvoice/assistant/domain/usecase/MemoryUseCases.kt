package com.persianvoice.assistant.domain.usecase

import com.persianvoice.assistant.domain.model.Memory
import com.persianvoice.assistant.domain.model.MemoryCategory
import com.persianvoice.assistant.domain.repository.MemoryRepository
import javax.inject.Inject

/**
 * ذخیره و بازیابی حافظه بلندمدت.
 * مثلاً: "اسم همسرم سارا است"
 */
class SaveMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(
        key: String,
        value: String,
        category: MemoryCategory
    ): Long {
        val existing = memoryRepository.getMemoryByKey(key)
        return if (existing != null) {
            memoryRepository.addMemory(existing.key, value, category)
        } else {
            memoryRepository.addMemory(key, value, category)
        }
    }
}

class RetrieveMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(query: String, limit: Int = 5): List<Memory> {
        return memoryRepository.getRelevantMemories(query, limit)
    }
}