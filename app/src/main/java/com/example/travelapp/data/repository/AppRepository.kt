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

    // --- Trips ---
    val allTrips: LiveData<List<Trip>> = tripDao.getAllTrips()
    fun getTripsForUser(userId: Int): LiveData<List<Trip>> = tripDao.getTripsForUser(userId)
    fun getTripById(tripId: Int): LiveData<Trip> = tripDao.getTripById(tripId)
    suspend fun insertTrip(trip: Trip) = tripDao.insertTrip(trip)
    suspend fun deleteTrip(trip: Trip) = tripDao.deleteTrip(trip)
    suspend fun updateTrip(trip: Trip) = tripDao.updateTrip(trip)

    // --- Users ---
    suspend fun insertUser(user: User): Long = userDao.insert(user)
    suspend fun updateUser(user: User) = userDao.update(user)
    suspend fun getUserByEmail(email: String): User? = userDao.getByEmail(email)

    // --- Locations ---
    val allLocations: LiveData<List<LocationLog>> = locationDao.getAllLocations()
    suspend fun insertLocation(location: LocationLog) = locationDao.insertLocation(location)

    // --- Favorites ---
    val allFavorites: LiveData<List<FavoritePlace>> = favoritePlaceDao.getAllFavorites()
    suspend fun insertFavorite(favorite: FavoritePlace) = favoritePlaceDao.insertFavorite(favorite)

    // --- Settings ---
    val settings: LiveData<Settings?> = settingsDao.getSettings()
    suspend fun insertSettings(settings: Settings) = settingsDao.insertSettings(settings)
}
