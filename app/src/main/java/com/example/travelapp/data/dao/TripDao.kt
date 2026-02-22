package com.example.travelapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.travelapp.data.model.LocationLog
import com.example.travelapp.data.model.Trip
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Insert
    suspend fun insert(trip: Trip): Long

    @Update
    suspend fun update(trip: Trip)

    @Delete
    suspend fun delete(trip: Trip)

    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun getById(tripId: Int): Flow<Trip?>

    @Query("""
        SELECT t.* FROM trips t
        INNER JOIN trip_participants tp ON t.id = tp.tripId
        WHERE tp.userId = :userId
        ORDER BY t.startDate DESC
    """)
    fun getTripsForUser(userId: Int): Flow<List<Trip>>

    @Query("""
    SELECT DISTINCT t.* FROM trips t
    INNER JOIN trip_participants tp ON t.id = tp.tripId
    WHERE tp.userId IN (
        SELECT friendId FROM friendships WHERE userId = :userId
    )
    OR tp.userId = :userId
    ORDER BY t.startDate DESC
""")
    fun getFeed(userId: Int): Flow<List<Trip>>
}
