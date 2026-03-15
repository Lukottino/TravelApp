package com.example.travelapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trip_photos",
    foreignKeys = [
        ForeignKey(entity = Trip::class, parentColumns = ["id"], childColumns = ["tripId"], onDelete = CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["addedBy"], onDelete = CASCADE)
    ],
    indices = [Index("tripId"), Index("addedBy")]
)
data class TripPhoto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int,
    val addedBy: Int,
    val imageUri: String,
    val addedAt: Long = System.currentTimeMillis()
)
