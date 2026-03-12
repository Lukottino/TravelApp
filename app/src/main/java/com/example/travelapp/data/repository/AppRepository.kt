package com.example.travelapp.data.repository

import androidx.lifecycle.LiveData
import com.example.travelapp.data.dao.*
import com.example.travelapp.data.model.*
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val tripDao: TripDao,
    private val userDao: UserDao,
    private val locationDao: LocationDao,
    private val favoritePlaceDao: FavoritePlaceDao,
    private val settingsDao: SettingsDao,
    private val friendshipDao: FriendshipDao,
    private val friendRequestDao: FriendRequestDao
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
    suspend fun getUserById(id: Int): User? = userDao.getByIdSuspend(id)
    suspend fun searchUsers(query: String, excludeId: Int): List<User> = userDao.searchUsers(query, excludeId)

    // --- Locations ---
    val allLocations: LiveData<List<LocationLog>> = locationDao.getAllLocations()
    suspend fun insertLocation(location: LocationLog) = locationDao.insertLocation(location)

    // --- Favorites ---
    val allFavorites: LiveData<List<FavoritePlace>> = favoritePlaceDao.getAllFavorites()
    suspend fun insertFavorite(favorite: FavoritePlace) = favoritePlaceDao.insertFavorite(favorite)

    // --- Settings ---
    val settings: LiveData<Settings?> = settingsDao.getSettings()
    suspend fun insertSettings(settings: Settings) = settingsDao.insertSettings(settings)

    // --- Friends ---
    fun getFriends(userId: Int): Flow<List<User>> = friendshipDao.getFriends(userId)
    suspend fun areFriends(userId: Int, friendId: Int): Boolean = friendshipDao.areFriends(userId, friendId) > 0

    // --- Friend Requests ---
    fun getIncomingRequests(userId: Int): Flow<List<FriendRequest>> = friendRequestDao.getIncomingRequests(userId)
    fun getSentRequests(userId: Int): Flow<List<FriendRequest>> = friendRequestDao.getSentRequests(userId)

    suspend fun sendFriendRequest(senderId: Int, receiverId: Int) {
        friendRequestDao.sendRequest(FriendRequest(senderId = senderId, receiverId = receiverId))
    }

    suspend fun acceptFriendRequest(request: FriendRequest) {
        friendRequestDao.update(request.copy(status = "ACCEPTED"))
        friendshipDao.insert(Friendship(userId = request.senderId, friendId = request.receiverId))
        friendshipDao.insert(Friendship(userId = request.receiverId, friendId = request.senderId))
    }

    suspend fun rejectFriendRequest(request: FriendRequest) {
        friendRequestDao.update(request.copy(status = "REJECTED"))
    }

    suspend fun hasPendingRequest(senderId: Int, receiverId: Int): Boolean =
        friendRequestDao.hasPendingRequest(senderId, receiverId) > 0
}
