package com.example.travelapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trips",
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
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Int,
    val name: String,
    val destination: String,
    val notes: String?,
    val latitude: Double?,
    val longitude: Double?,

    val startDate: Long,
    val endDate: Long?,

    val createdAt: Long = System.currentTimeMillis(),

    val coverImageUri: String? = null,

    val status: TripStatus = TripStatus.DRAFT
)
