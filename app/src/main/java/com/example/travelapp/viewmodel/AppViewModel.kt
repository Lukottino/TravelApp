package com.example.travelapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.database.AppDatabase
import com.example.travelapp.data.model.*
import com.example.travelapp.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(context: Context) : ViewModel() {
    private val database = AppDatabase.getDatabase(context)
    private val repository = AppRepository(
        database.tripDao(),
        database.userDao(),
        database.locationDao(),
        database.favoritePlaceDao(),
        database.settingsDao(),
        database.friendshipDao(),
        database.friendRequestDao(),
        database.tripParticipantDao(),
        database.tripPhotoDao()
    )

    val allTrips: LiveData<List<Trip>> = repository.allTrips
    val allLocations: LiveData<List<LocationLog>> = repository.allLocations
    val allFavorites: LiveData<List<FavoritePlace>> = repository.allFavorites
    val settings: LiveData<Settings?> = repository.settings

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // --- Friends ---
    val friends: StateFlow<List<User>> = currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getFriends(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomingRequests: StateFlow<List<FriendRequest>> = currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getIncomingRequests(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sentRequests: StateFlow<List<FriendRequest>> = currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getSentRequests(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Trips ---
    fun getTripsForCurrentUser(): LiveData<List<Trip>> {
        val user = _currentUser.value ?: return MutableLiveData(emptyList())
        return repository.getTripsForUser(user.id)
    }

    fun getTripById(tripId: Int): LiveData<Trip> = repository.getTripById(tripId)
    fun addTrip(trip: Trip) = viewModelScope.launch {
        val tripId = repository.insertTrip(trip)
        val userId = _currentUser.value?.id ?: return@launch
        repository.insertParticipant(TripParticipant(tripId.toInt(), userId, TripRole.OWNER))
    }
    fun updateTrip(trip: Trip) = viewModelScope.launch { repository.updateTrip(trip) }
    fun deleteTrip(trip: Trip) = viewModelScope.launch { repository.deleteTrip(trip) }

    // --- Photos ---
    fun getPhotosForTrip(tripId: Int): kotlinx.coroutines.flow.Flow<List<com.example.travelapp.data.model.TripPhoto>> =
        repository.getPhotosForTrip(tripId)

    fun addPhoto(tripId: Int, imageUri: String) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.insertPhoto(com.example.travelapp.data.model.TripPhoto(tripId = tripId, addedBy = userId, imageUri = imageUri))
        }
    }

    fun deletePhoto(photo: com.example.travelapp.data.model.TripPhoto) = viewModelScope.launch {
        repository.deletePhoto(photo)
    }

    // --- Participants ---
    fun getParticipants(tripId: Int): kotlinx.coroutines.flow.Flow<List<User>> = repository.getParticipants(tripId)
    fun addParticipant(tripId: Int, userId: Int) = viewModelScope.launch {
        repository.insertParticipant(TripParticipant(tripId, userId, TripRole.MEMBER))
    }
    fun removeParticipant(tripId: Int, userId: Int) = viewModelScope.launch {
        repository.removeParticipant(tripId, userId)
    }

    // --- Misc ---
    fun addLocation(location: LocationLog) = viewModelScope.launch { repository.insertLocation(location) }
    fun addFavorite(favorite: FavoritePlace) = viewModelScope.launch { repository.insertFavorite(favorite) }
    fun saveSettings(settings: Settings) = viewModelScope.launch { repository.insertSettings(settings) }

    // --- Auth ---
    fun setCurrentUser(user: User) { _currentUser.value = user }
    fun clearCurrentUser() { _currentUser.value = null }

    fun login(email: String, password: String, onSuccess: (User) -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null && user.password == password) {
                _currentUser.value = user
                onSuccess(user)
            } else {
                onError()
            }
        }
    }

    fun registerUser(user: User, onSuccess: (User) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val existing = repository.getUserByEmail(user.email)
            if (existing != null) {
                onError("Email già registrata")
            } else {
                val insertedId = repository.insertUser(user)
                val addedUser = user.copy(id = insertedId.toInt())
                _currentUser.value = addedUser
                onSuccess(addedUser)
            }
        }
    }

    fun updateUser(user: User, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            repository.updateUser(user)
            _currentUser.value = user
            onSuccess()
        }
    }

    // --- Users ---
    suspend fun getUserById(id: Int): User? = repository.getUserById(id)
    suspend fun getTripsForUser(userId: Int): List<Trip> = repository.getTripsForUserList(userId)

    fun searchUsers(query: String, onResult: (List<User>) -> Unit) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            onResult(repository.searchUsers(query, excludeId = userId))
        }
    }

    // --- Friend requests ---
    fun sendFriendRequest(receiverId: Int) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch { repository.sendFriendRequest(userId, receiverId) }
    }

    fun acceptFriendRequest(request: FriendRequest) {
        viewModelScope.launch { repository.acceptFriendRequest(request) }
    }

    fun rejectFriendRequest(request: FriendRequest) {
        viewModelScope.launch { repository.rejectFriendRequest(request) }
    }
}
