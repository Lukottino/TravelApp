package com.example.travelapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1, // sempre una sola riga
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true
)
