package com.example.travelapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val destination: String,
    val latitude: Double?,
    val longitude: Double?,
    val startDate: Long,
    val endDate: Long? = null,
    val notes: String? = null,
    val userId: Int
)
