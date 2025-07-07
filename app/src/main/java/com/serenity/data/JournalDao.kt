package java.com.serenity.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.serenity.data.Journal
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(journal: Journal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturnId(journal: Journal): Long

    @Query("UPDATE journals SET analysisJson = :analysisJson WHERE id = :id")
    suspend fun updateAnalysis(id: Int, analysisJson: String)

    @Query("UPDATE journals SET peopleJson = :peopleJson WHERE id = :id")
    suspend fun updatePeople(id: Int, peopleJson: String)

    @Query("UPDATE journals SET momentsJson = :momentsJson WHERE id = :id")
    suspend fun updateMoments(id: Int, momentsJson: String)

    @Query("UPDATE journals SET placesJson = :placesJson WHERE id = :id")
    suspend fun updatePlaces(id: Int, placesJson: String)

    @Query("SELECT * FROM journals ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Journal>>
    
    @Query("DELETE FROM journals")
    suspend fun deleteAll()
} 