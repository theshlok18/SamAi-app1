package dev.shlok.samai.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.shlok.samai.memory.ChatMessageDao
import dev.shlok.samai.memory.MemoryDao
import dev.shlok.samai.memory.SamDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSamDatabase(@ApplicationContext context: Context): SamDatabase {
        return Room.databaseBuilder(context, SamDatabase::class.java, "sam_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideMemoryDao(db: SamDatabase): MemoryDao = db.memoryDao()

    @Provides
    fun provideChatMessageDao(db: SamDatabase): ChatMessageDao = db.chatMessageDao()
}
