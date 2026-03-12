package com.example.travelapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.travelapp.data.model.Trip
import com.example.travelapp.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    fun getById(id: Int): Flow<User?>

    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getByIdSuspend(id: Int): User?

    @Query("SELECT * FROM users WHERE (name LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%') AND id != :excludeId LIMIT 20")
    suspend fun searchUsers(query: String, excludeId: Int): List<User>

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)
}
