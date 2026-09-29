package dev.shlok.samai.memory

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MemoryEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SamDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun chatMessageDao(): ChatMessageDao
}
