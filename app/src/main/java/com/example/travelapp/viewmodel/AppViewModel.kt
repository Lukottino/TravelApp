package com.example.travelapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.data.database.AppDatabase
import com.example.travelapp.data.model.Trip
import com.example.travelapp.data.model.User
import com.example.travelapp.data.model.LocationLog
import com.example.travelapp.data.model.FavoritePlace
import com.example.travelapp.data.model.Settings
import com.example.travelapp.data.repository.AppRepository
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

    val allTrips = repository.allTrips
    val allUsers = repository.allUsers
    val allLocations = repository.allLocations
    val allFavorites = repository.allFavorites
    val settings = repository.settings

    fun addTrip(trip: Trip) = viewModelScope.launch { repository.insertTrip(trip) }
    fun addUser(user: User) = viewModelScope.launch { repository.insertUser(user) }
    fun addLocation(location: LocationLog) = viewModelScope.launch { repository.insertLocation(location) }
    fun addFavorite(favorite: FavoritePlace) = viewModelScope.launch { repository.insertFavorite(favorite) }
    fun saveSettings(settings: Settings) = viewModelScope.launch { repository.insertSettings(settings) }
    fun login(email: String, password: String): User? {
        val user = allUsers.value.firstOrNull { it.email == email && it.password == password }
        return user
    }
    fun registerUser(user: User, onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            val insertedId = repository.insertUser(user)
            val addedUser = user.copy(id = insertedId.toInt())
            onSuccess(addedUser)  // ora ID esiste sempre
        }
    }



}
