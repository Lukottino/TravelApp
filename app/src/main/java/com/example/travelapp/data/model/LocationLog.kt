package com.example.travelapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_logs")
data class LocationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int,   // collegato a Trip
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)
