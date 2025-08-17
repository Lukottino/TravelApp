package com.example.travelapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.travelapp.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Insert
    suspend fun insertUser(user: User)
}
