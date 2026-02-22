package com.example.travelapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "friend_requests",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["senderId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["receiverId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index(value = ["senderId", "receiverId"], unique = true)]
)
data class FriendRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val senderId: Int,
    val receiverId: Int,
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED
    val createdAt: Long = System.currentTimeMillis()
)