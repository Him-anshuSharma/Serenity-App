package com.serenity.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.serenity.data.AppDatabase
import com.serenity.data.ChatSessionDao
import java.com.serenity.data.JournalDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase =
        Room.databaseBuilder(app, AppDatabase::class.java, "serenity_db").fallbackToDestructiveMigration().build()

    @Provides
    fun provideJournalDao(db: AppDatabase): JournalDao = db.journalDao()

    @Provides
    fun provideChatSessionDao(db: AppDatabase): ChatSessionDao = db.chatSessionDao()
} 