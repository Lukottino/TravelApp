package com.example.travelapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.travelapp.data.model.Trip

@Dao
interface TripDao {

    @Insert
    suspend fun insertTrip(trip: Trip): Long

    @Update
    suspend fun updateTrip(trip: Trip)

    @Delete
    suspend fun deleteTrip(trip: Trip)

    @Query("SELECT * FROM trips ORDER BY startDate DESC")
    fun getAllTrips(): LiveData<List<Trip>>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun getTripById(tripId: Int): LiveData<Trip>

    @Query("SELECT * FROM trips WHERE userId = :userId ORDER BY startDate DESC")
    fun getTripsForUser(userId: Int): LiveData<List<Trip>>

    @Query("""
        SELECT DISTINCT t.* FROM trips t
        LEFT JOIN friendships f ON f.userId = :userId
        WHERE t.userId = :userId OR t.userId = f.friendId
        ORDER BY t.startDate DESC
    """)
    fun getFeed(userId: Int): LiveData<List<Trip>>
}
