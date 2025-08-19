package com.example.travelapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.travelapp.data.model.Trip
import com.example.travelapp.data.model.User
import com.example.travelapp.data.model.LocationLog
import com.example.travelapp.data.model.FavoritePlace
import com.example.travelapp.data.model.Settings
import com.example.travelapp.data.dao.TripDao
import com.example.travelapp.data.dao.UserDao
import com.example.travelapp.data.dao.FavoritePlaceDao
import com.example.travelapp.data.dao.LocationDao
import com.example.travelapp.data.dao.SettingsDao

@Database(
    entities = [Trip::class, User::class, LocationLog::class, FavoritePlace::class, Settings::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun userDao(): UserDao
    abstract fun locationDao(): LocationDao
    abstract fun favoritePlaceDao(): FavoritePlaceDao
    abstract fun settingsDao(): SettingsDao

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
