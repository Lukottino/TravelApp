package com.example.travelapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.travelapp.data.dao.*
import com.example.travelapp.data.model.*

@Database(
    entities = [
        Trip::class,
        User::class,
        LocationLog::class,
        FavoritePlace::class,
        Settings::class,
        TripParticipant::class,
        Friendship::class,
        FriendRequest::class,
        UserStats::class,
        Badge::class,
        UserBadge::class,
        AppNotification::class,
        TripPhoto::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun userDao(): UserDao
    abstract fun locationDao(): LocationDao
    abstract fun favoritePlaceDao(): FavoritePlaceDao
    abstract fun settingsDao(): SettingsDao
    abstract fun friendshipDao(): FriendshipDao
    abstract fun friendRequestDao(): FriendRequestDao
    abstract fun tripParticipantDao(): TripParticipantDao
    abstract fun tripPhotoDao(): TripPhotoDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travel_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
