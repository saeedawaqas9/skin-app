package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.JournalEntryEntity
import com.example.data.local.entity.PreferencesEntity
import com.example.data.local.entity.RoutineStepEntity
import com.example.data.local.entity.ShelfEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkinRoutineDao {

    // Preferences
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getPreferences(): Flow<PreferencesEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getPreferencesDirect(): PreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: PreferencesEntity)

    // Shelf Products
    @Query("SELECT * FROM shelf_products ORDER BY id DESC")
    fun getAllShelfProducts(): Flow<List<ShelfEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelfProduct(product: ShelfEntity)

    @Query("DELETE FROM shelf_products WHERE id = :id")
    suspend fun deleteShelfProduct(id: String)

    // Routine Steps
    @Query("SELECT * FROM routine_steps ORDER BY routineType, stepNumber ASC")
    fun getAllRoutineSteps(): Flow<List<RoutineStepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineSteps(steps: List<RoutineStepEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineStep(step: RoutineStepEntity)

    @Update
    suspend fun updateRoutineStep(step: RoutineStepEntity)

    @Query("DELETE FROM routine_steps WHERE id = :id")
    suspend fun deleteRoutineStep(id: String)

    @Query("UPDATE routine_steps SET isCompletedToday = :completed WHERE id = :id")
    suspend fun setStepCompleted(id: String, completed: Boolean)

    @Query("UPDATE routine_steps SET isCompletedToday = 0")
    suspend fun resetAllStepsCompleted()

    // Journal Entries
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteJournalEntry(id: String)
}
