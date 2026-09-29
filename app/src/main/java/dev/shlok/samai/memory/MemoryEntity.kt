package dev.shlok.samai.memory

import androidx.room.*

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val value: String,
    val category: String = "general",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY updatedAt DESC")
    suspend fun getAllMemories(): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE key = :key LIMIT 1")
    suspend fun getMemoryByKey(key: String): MemoryEntity?

    @Query("SELECT * FROM memories WHERE category = :category")
    suspend fun getMemoriesByCategory(category: String): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE key LIKE '%' || :query || '%' OR value LIKE '%' || :query || '%'")
    suspend fun searchMemories(query: String): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)

    @Query("DELETE FROM memories")
    suspend fun clearAll()
}
