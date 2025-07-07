package com.serenity.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journals")
data class Journal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val analysisJson: String? = null, // Stores Gemini analysis as JSON
    val peopleJson: String? = null,   // Stores people and relationships as JSON
    val momentsJson: String? = null,  // Stores moments as JSON
    val placesJson: String? = null    // Stores places as JSON
) 