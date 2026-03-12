package com.example.travelapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.database.AppDatabase
import com.example.travelapp.data.model.*
import com.example.travelapp.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppViewModel(context: Context) : ViewModel() {
    private val database = AppDatabase.getDatabase(context)
    private val repository = AppRepository(
        database.tripDao(),
        database.userDao(),
        database.locationDao(),
        database.favoritePlaceDao(),
        database.settingsDao()
    )

    val allTrips: LiveData<List<Trip>> = repository.allTrips
    val allLocations: LiveData<List<LocationLog>> = repository.allLocations
    val allFavorites: LiveData<List<FavoritePlace>> = repository.allFavorites
    val settings: LiveData<Settings?> = repository.settings

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun getTripsForCurrentUser(): LiveData<List<Trip>> {
        val user = _currentUser.value ?: return MutableLiveData(emptyList())
        return repository.getTripsForUser(user.id)
    }

    fun getTripById(tripId: Int): LiveData<Trip> = repository.getTripById(tripId)

    fun addTrip(trip: Trip) = viewModelScope.launch { repository.insertTrip(trip) }

    fun updateTrip(trip: Trip) = viewModelScope.launch { repository.updateTrip(trip) }

    fun deleteTrip(trip: Trip) = viewModelScope.launch { repository.deleteTrip(trip) }

    fun addLocation(location: LocationLog) = viewModelScope.launch { repository.insertLocation(location) }

    fun addFavorite(favorite: FavoritePlace) = viewModelScope.launch { repository.insertFavorite(favorite) }

    fun saveSettings(settings: Settings) = viewModelScope.launch { repository.insertSettings(settings) }

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
}
