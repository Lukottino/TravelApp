package com.example.travelapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

@Entity(
    tableName = "trip_participants",
    primaryKeys = ["tripId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("tripId"), Index("userId")]
)
data class TripParticipant(
    val tripId: Int,
    val userId: Int,
    val role: TripRole // OWNER, MEMBER
)

enum class TripRole{
    OWNER,
    MEMBER
}