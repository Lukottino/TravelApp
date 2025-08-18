package com.example.travelapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.travelapp.data.model.LocationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM LocationLog ORDER BY timestamp DESC")
    fun getAllLocations(): LiveData<List<LocationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationLog)

    @Delete
    suspend fun deleteLocation(location: LocationLog)
}
