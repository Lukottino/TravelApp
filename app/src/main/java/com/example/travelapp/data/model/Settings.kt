package com.example.travelapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Settings(
    @PrimaryKey val id: Int = 1, // solo un record per utente
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true
)
