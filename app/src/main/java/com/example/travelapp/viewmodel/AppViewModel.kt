package com.example.travelapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.database.AppDatabase
import com.example.travelapp.data.model.User
import com.example.travelapp.data.model.LocationLog
import com.example.travelapp.data.model.FavoritePlace
import com.example.travelapp.data.model.Settings
import com.example.travelapp.data.model.Trip
import com.example.travelapp.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.CoroutineScope


class AppViewModel(context: Context) : ViewModel() {
    private val database = AppDatabase.getDatabase(context)
    private val repository = AppRepository(
        database.tripDao(),
        database.userDao(),
        database.locationDao(),
        database.favoritePlaceDao(),
        database.settingsDao()
    )

    val allTrips = repository.allTrips
    val allUsers = MutableStateFlow<List<User>>(emptyList())
    init {
        viewModelScope.launch {
            repository.allUsers.observeForever { users ->
                allUsers.value = users
            }
        }
    }


    val allLocations = repository.allLocations
    val allFavorites = repository.allFavorites
    val settings = repository.settings

    // Utente loggato
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun getTripsForCurrentUser(): LiveData<List<Trip>> {
        val user = _currentUser.value
        return if (user != null) {
            repository.getTripsForUser(user.id)
        } else {
            // Restituisce sempre una lista vuota come LiveData
            MutableLiveData(emptyList())
        }
    }

    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            repository.updateTrip(trip)
        }
    }

    fun getTripById(tripId: Int): LiveData<Trip> = repository.getTripById(tripId)

    fun addTripForCurrentUser(
        name: String,
        destination: String,
        startDate: Long,
        endDate: Long? = null,
        notes: String? = null,
        latitude: Double?,
        longitude: Double?
    ) {
        val user = _currentUser.value ?: return
        val trip = Trip(
            userId = user.id,
            name = name,
            destination = destination,
            startDate = startDate,
            endDate = endDate,
            notes = notes,
            latitude = latitude,
            longitude = longitude
        )
        viewModelScope.launch {
            repository.insertTrip(trip)
        }
    }


    fun setCurrentUser(user: User) {
        _currentUser.value = user
    }

    fun clearCurrentUser() {
        _currentUser.value = null
    }

    fun addTrip(trip: Trip) = viewModelScope.launch {
        repository.insertTrip(trip)
    }

    fun addUser(user: User) = viewModelScope.launch { repository.insertUser(user) }
    fun addLocation(location: LocationLog) = viewModelScope.launch { repository.insertLocation(location) }
    fun addFavorite(favorite: FavoritePlace) = viewModelScope.launch { repository.insertFavorite(favorite) }
    fun saveSettings(settings: Settings) = viewModelScope.launch { repository.insertSettings(settings) }
    fun login(email: String, password: String): User? {
        val user = allUsers.value.firstOrNull { it.email == email && it.password == password }
        return user
    }
    fun registerUser(user: User, onSuccess: (User) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val existingUser = allUsers.value.firstOrNull { it.email == user.email }
            if (existingUser != null) {
                onError("Email già registrata")
            } else {
                val insertedId = repository.insertUser(user)
                val addedUser = user.copy(id = insertedId.toInt())

                // Aggiorna la lista degli utenti
                val updatedUsers = allUsers.value.toMutableList()
                updatedUsers.add(addedUser)
                allUsers.value = updatedUsers

                // Imposta l'utente loggato
                _currentUser.value = addedUser
                onSuccess(addedUser)
            }
        }
    }
}
