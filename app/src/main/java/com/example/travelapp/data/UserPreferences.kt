package com.example.travelapp.data

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPreferences {

    private val USER_ID_KEY = intPreferencesKey("user_id")

    suspend fun saveUserId(context: Context, userId: Int) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
        }
    }

    suspend fun clearUserId(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(USER_ID_KEY)
        }
    }

    fun getUserId(context: Context): Flow<Int?> =
        context.dataStore.data.map { prefs -> prefs[USER_ID_KEY] }
}
