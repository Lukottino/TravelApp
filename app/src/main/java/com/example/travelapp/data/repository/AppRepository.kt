package com.example.travelapp.data.repository

import androidx.lifecycle.LiveData
import com.example.travelapp.data.dao.*
import com.example.travelapp.data.model.*

class AppRepository(
    private val tripDao: TripDao,
    private val userDao: UserDao,
    private val locationDao: LocationDao,
    private val favoritePlaceDao: FavoritePlaceDao,
    private val settingsDao: SettingsDao
) {

    // LiveData per osservare i dati
    val allTrips: LiveData<List<Trip>> = tripDao.getAllTrips()
    val allUsers: LiveData<List<User>> = userDao.getAllUsers()
    val allLocations: LiveData<List<LocationLog>> = locationDao.getAllLocations()
    val allFavorites: LiveData<List<FavoritePlace>> = favoritePlaceDao.getAllFavorites()
    val settings: LiveData<Settings?> = settingsDao.getSettings()

    // Funzioni di inserimento
    suspend fun insertTrip(trip: Trip) = tripDao.insertTrip(trip)
    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }

    suspend fun insertLocation(location: LocationLog) = locationDao.insertLocation(location)
    suspend fun insertFavorite(favorite: FavoritePlace) = favoritePlaceDao.insertFavorite(favorite)
    suspend fun insertSettings(settings: Settings) = settingsDao.insertSettings(settings)
}
