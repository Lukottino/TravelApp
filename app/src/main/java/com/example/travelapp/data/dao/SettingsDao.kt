package com.example.travelapp.data.dao

import androidx.room.*
import com.example.travelapp.data.model.Settings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM Settings LIMIT 1")
    fun getSettings(): Flow<Settings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: Settings)

    @Update
    suspend fun updateSettings(settings: Settings)
}
