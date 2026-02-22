package com.example.travelapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_stats",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class UserStats(
    @PrimaryKey
    val userId: Int,

    val totalTrips: Int = 0,
    val totalKm: Double = 0.0,
    val points: Int = 0,
    val level: Int = 1
)