package dev.shlok.samai.memory

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepository @Inject constructor(
    private val memoryDao: MemoryDao
) {
    suspend fun remember(key: String, value: String, category: String = "general") {
        val existing = memoryDao.getMemoryByKey(key)
        if (existing != null) {
            memoryDao.updateMemory(existing.copy(value = value, updatedAt = System.currentTimeMillis()))
        } else {
            memoryDao.insertMemory(MemoryEntity(key = key, value = value, category = category))
        }
    }

    suspend fun recall(key: String): String? = memoryDao.getMemoryByKey(key)?.value

    suspend fun getAllMemories(): List<MemoryEntity> = memoryDao.getAllMemories()

    suspend fun searchMemories(query: String): List<MemoryEntity> = memoryDao.searchMemories(query)

    suspend fun deleteMemory(memory: MemoryEntity) = memoryDao.deleteMemory(memory)

    suspend fun clearAll() = memoryDao.clearAll()

    suspend fun buildMemoryContext(): String {
        val memories = memoryDao.getAllMemories()
        if (memories.isEmpty()) return ""
        return memories.joinToString("\n") { "- ${it.key}: ${it.value}" }
    }
}
