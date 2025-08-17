package com.example.travelapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FavoritePlace(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double
)
