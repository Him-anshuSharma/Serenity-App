package com.serenity.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ChatSession): Long

    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_sessions WHERE id = :id")
    suspend fun getById(id: Int): ChatSession?
    
    @Query("DELETE FROM chat_sessions")
    suspend fun deleteAll()
} 