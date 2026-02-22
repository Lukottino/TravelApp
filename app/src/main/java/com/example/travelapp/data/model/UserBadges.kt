package com.example.travelapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

@Entity(
    tableName = "user_badges",
    primaryKeys = ["userId", "badgeId"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Badge::class,
            parentColumns = ["id"],
            childColumns = ["badgeId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("userId"), Index("badgeId")]
)
data class UserBadge(
    val userId: Int,
    val badgeId: Int,
    val earnedAt: Long = System.currentTimeMillis()
)