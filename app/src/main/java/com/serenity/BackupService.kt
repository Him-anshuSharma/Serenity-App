package com.serenity

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.serenity.data.ChatSession
import com.serenity.data.Journal
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.get

@Singleton
class BackupService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    data class BackupData(
        val journals: List<Journal> = emptyList(),
        val chatSessions: List<ChatSession> = emptyList(),
        val lastBackupTime: Long = System.currentTimeMillis()
    )
    
    suspend fun backupData(journals: List<Journal>, chatSessions: List<ChatSession>): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            
            val backupData = BackupData(
                journals = journals,
                chatSessions = chatSessions,
                lastBackupTime = System.currentTimeMillis()
            )
            
            val backupMap = mapOf(
                "journals" to journals.map { journal ->
                    mapOf(
                        "id" to journal.id,
                        "title" to journal.title,
                        "content" to journal.content,
                        "timestamp" to journal.timestamp,
                        "analysisJson" to journal.analysisJson,
                        "peopleJson" to journal.peopleJson,
                        "placesJson" to journal.placesJson
                    )
                },
                "chatSessions" to chatSessions.map { session ->
                    mapOf(
                        "id" to session.id,
                        "timestamp" to session.timestamp,
                        "messagesJson" to session.messagesJson
                    )
                },
                "lastBackupTime" to backupData.lastBackupTime
            )
            
            firestore.collection("users")
                .document(userId)
                .collection("backups")
                .document("latest")
                .set(backupMap)
                .await()
            
            Result.success("Backup completed successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun restoreData(): Result<BackupData> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            
            val document = firestore.collection("users")
                .document(userId)
                .collection("backups")
                .document("latest")
                .get()
                .await()
            
            if (!document.exists()) {
                return Result.failure(Exception("No backup found"))
            }
            
            val data = document.data ?: return Result.failure(Exception("Backup data is empty"))
            
            val journals = (data["journals"] as? List<*>)?.mapNotNull { journalData ->
                val journalMap = journalData as? Map<*, *>
                journalMap?.let {
                    Journal(
                        id = (it["id"] as? Number)?.toInt() ?: 0,
                        title = it["title"] as? String ?: "",
                        content = it["content"] as? String ?: "",
                        timestamp = (it["timestamp"] as? Number)?.toLong() ?: 0L,
                        analysisJson = it["analysisJson"] as? String,
                        peopleJson = it["peopleJson"] as? String,
                        placesJson = it["placesJson"] as? String
                    )
                }
            } ?: emptyList()
            
            val chatSessions = (data["chatSessions"] as? List<*>)?.mapNotNull { sessionData ->
                val sessionMap = sessionData as? Map<*, *>
                sessionMap?.let {
                    ChatSession(
                        id = (it["id"] as? Number)?.toInt() ?: 0,
                        timestamp = (it["timestamp"] as? Number)?.toLong() ?: 0L,
                        messagesJson = it["messagesJson"] as? String ?: ""
                    )
                }
            } ?: emptyList()
            
            val lastBackupTime = data["lastBackupTime"] as? Long ?: 0L
            
            Result.success(BackupData(journals, chatSessions, lastBackupTime))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getLastBackupTime(): Result<Long> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            
            val document = firestore.collection("users")
                .document(userId)
                .collection("backups")
                .document("latest")
                .get()
                .await()
            
            if (!document.exists()) {
                return Result.success(0L)
            }
            
            val lastBackupTime = document.getLong("lastBackupTime") ?: 0L
            Result.success(lastBackupTime)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 