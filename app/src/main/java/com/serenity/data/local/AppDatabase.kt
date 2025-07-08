package com.serenity.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.serenity.data.local.entities.ChatSession
import com.serenity.data.dao.ChatSessionDao
import com.serenity.data.local.entities.Journal
import com.serenity.data.dao.JournalDao

@Database(entities = [Journal::class, ChatSession::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao
    abstract fun chatSessionDao(): ChatSessionDao
}