package com.example.travelapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.travelapp.data.model.LocationLog

@Dao
interface LocationDao {
    @Query("SELECT * FROM location_logs")   // deve corrispondere a tableName!
    fun getAllLocations(): LiveData<List<LocationLog>>

    @Insert
    suspend fun insertLocation(location: LocationLog)
}
