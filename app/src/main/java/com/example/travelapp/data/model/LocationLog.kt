package com.example.travelapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)
