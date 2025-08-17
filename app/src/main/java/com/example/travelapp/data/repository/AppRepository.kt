package com.example.travelapp.data.repository

import com.example.travelapp.data.dao.*
import com.example.travelapp.data.model.*
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val tripDao: TripDao,
    private val userDao: UserDao,
    private val locationDao: LocationDao,
    private val favoritePlaceDao: FavoritePlaceDao,
    private val settingsDao: SettingsDao
) {

    // --- TRIPS ---
    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()
    suspend fun insertTrip(trip: Trip) = tripDao.insertTrip(trip)
    suspend fun deleteTrip(trip: Trip) = tripDao.deleteTrip(trip)

    // --- USERS ---
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun deleteUser(user: User) = userDao.deleteUser(user)

    // --- LOCATION LOGS ---
    val allLocations: Flow<List<LocationLog>> = locationDao.getAllLocations()
    suspend fun insertLocation(location: LocationLog) = locationDao.insertLocation(location)
    suspend fun deleteLocation(location: LocationLog) = locationDao.deleteLocation(location)

    // --- FAVORITE PLACES ---
    val allFavorites: Flow<List<FavoritePlace>> = favoritePlaceDao.getAllFavorites()
    suspend fun insertFavorite(favorite: FavoritePlace) = favoritePlaceDao.insertFavorite(favorite)
    suspend fun deleteFavorite(favorite: FavoritePlace) = favoritePlaceDao.deleteFavorite(favorite)

    // --- SETTINGS ---
    val settings: Flow<Settings?> = settingsDao.getSettings()
    suspend fun insertSettings(settings: Settings) = settingsDao.insertSettings(settings)
    suspend fun updateSettings(settings: Settings) = settingsDao.updateSettings(settings)
}
