package com.example.travelapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.travelapp.data.model.TripParticipant
import com.example.travelapp.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface TripParticipantDao {

    @Insert
    suspend fun insert(participant: TripParticipant)

    @Delete
    suspend fun delete(participant: TripParticipant)

    @Query("""
        SELECT u.* FROM users u
        INNER JOIN trip_participants tp ON u.id = tp.userId
        WHERE tp.tripId = :tripId
    """)
    fun getParticipants(tripId: Int): Flow<List<User>>

    @Query("""
        SELECT * FROM trip_participants
        WHERE tripId = :tripId AND role = 'OWNER'
        LIMIT 1
    """)
    suspend fun getOwner(tripId: Int): TripParticipant?

    @Query("DELETE FROM trip_participants WHERE tripId = :tripId AND userId = :userId")
    suspend fun removeParticipant(tripId: Int, userId: Int)
}