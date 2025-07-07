package com.serenity.data

import androidx.room.Database
import androidx.room.RoomDatabase
import java.com.serenity.data.JournalDao

@Database(entities = [Journal::class, ChatSession::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao
    abstract fun chatSessionDao(): ChatSessionDao
} 