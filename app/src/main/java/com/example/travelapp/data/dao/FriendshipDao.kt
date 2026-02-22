package com.example.travelapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.travelapp.data.model.Friendship
import com.example.travelapp.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendshipDao {

    @Insert
    suspend fun insert(friendship: Friendship)

    @Query("""
        SELECT u.* FROM users u
        INNER JOIN friendships f ON u.id = f.friendId
        WHERE f.userId = :userId
        ORDER BY u.name ASC
    """)
    fun getFriends(userId: Int): Flow<List<User>>

    @Query("""
        SELECT COUNT(*) FROM friendships
        WHERE userId = :userId AND friendId = :friendId
    """)
    suspend fun areFriends(userId: Int, friendId: Int): Int
}