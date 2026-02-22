package com.example.travelapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.travelapp.data.model.FriendRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendRequestDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun sendRequest(request: FriendRequest)

    @Update
    suspend fun update(request: FriendRequest)

    @Delete
    suspend fun delete(request: FriendRequest)

    @Query("""
        SELECT * FROM friend_requests
        WHERE receiverId = :userId AND status = 'PENDING'
        ORDER BY createdAt DESC
    """)
    fun getIncomingRequests(userId: Int): Flow<List<FriendRequest>>

    @Query("""
        SELECT * FROM friend_requests
        WHERE senderId = :userId
    """)
    fun getSentRequests(userId: Int): Flow<List<FriendRequest>>
}