package com.example.travelapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.travelapp.data.model.TripPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface TripPhotoDao {

    @Insert
    suspend fun insert(photo: TripPhoto)

    @Delete
    suspend fun delete(photo: TripPhoto)

    @Query("SELECT * FROM trip_photos WHERE tripId = :tripId ORDER BY addedAt ASC")
    fun getPhotosForTrip(tripId: Int): Flow<List<TripPhoto>>
}
