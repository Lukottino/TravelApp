package com.example.travelapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.travelapp.data.model.UserStats
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {

    @Insert
    suspend fun insert(stats: UserStats)

    @Update
    suspend fun update(stats: UserStats)

    @Query("SELECT * FROM user_stats WHERE userId = :userId")
    fun getStats(userId: Int): Flow<UserStats?>
}