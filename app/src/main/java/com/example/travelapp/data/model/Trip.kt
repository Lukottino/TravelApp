package com.example.travelapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,                // Chi ha fatto il viaggio
    val name: String,               // Nome del viaggio
    val destination: String,        // Destinazione
    val startDate: Long,            // Timestamp in millisecondi
    val endDate: Long? = null,      // Timestamp opzionale
    val notes: String? = null       // Note opzionali
)
