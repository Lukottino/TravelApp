package com.example.travelapp

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.travelapp.data.model.Trip
import com.example.travelapp.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(viewModel: AppViewModel, onTripAdded: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var cityQuery by remember { mutableStateOf("") }
    var citySuggestions by remember { mutableStateOf(listOf<CitySuggestion>()) }
    var selectedCity by remember { mutableStateOf<CitySuggestion?>(null) }

    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var notes by remember { mutableStateOf("") }

    val currentUserId = viewModel.currentUser.value?.id ?: 0
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aggiungi Viaggio") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome Viaggio") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Campo città con autocomplete
            OutlinedTextField(
                value = selectedCity?.displayName ?: cityQuery,
                onValueChange = {
                    cityQuery = it
                    selectedCity = null
                    if (it.isNotBlank()) {
                        scope.launch {
                            citySuggestions = geocodeCity(it)
                        }
                    } else {
                        citySuggestions = emptyList()
                    }
                },
                label = { Text("Città / Paese") },
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(citySuggestions) { suggestion ->
                    Text(
                        text = suggestion.displayName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCity = suggestion
                                cityQuery = suggestion.displayName
                                citySuggestions = emptyList()
                            }
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            DatePickerField(
                label = "Data Inizio",
                date = startDate,
                dateFormat = dateFormat,
                onDateSelected = { startDate = it }
            )
            Spacer(modifier = Modifier.height(8.dp))

            DatePickerField(
                label = "Data Fine (opzionale)",
                date = endDate,
                dateFormat = dateFormat,
                onDateSelected = { endDate = it }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isBlank() || selectedCity == null || startDate == null) {
                        Toast.makeText(context, "Compila nome, città e data inizio", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val newTrip = Trip(
                        id = 0,
                        name = name,
                        destination = selectedCity!!.displayName,
                        latitude = selectedCity!!.lat,
                        longitude = selectedCity!!.lon,
                        startDate = startDate!!,
                        endDate = endDate,
                        notes = notes.ifBlank { null },
                        userId = currentUserId
                    )

                    scope.launch {
                        try {
                            viewModel.addTrip(newTrip)
                            Toast.makeText(context, "Viaggio aggiunto!", Toast.LENGTH_SHORT).show()
                            onTripAdded()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Errore durante il salvataggio", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salva Viaggio")
            }
        }
    }
}

// Classe per suggerimenti città
data class CitySuggestion(val displayName: String, val lat: Double, val lon: Double)

// Funzione di geocoding Nominatim
suspend fun geocodeCity(query: String): List<CitySuggestion> = withContext(Dispatchers.IO) {
    try {
        val url = "https://nominatim.openstreetmap.org/search?format=json&q=${query}"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "TravelApp")
            .build()
        val response = client.newCall(request).execute()
        val json = response.body?.string() ?: return@withContext emptyList()
        val array = JSONArray(json)
        val suggestions = mutableListOf<CitySuggestion>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val name = obj.getString("display_name")
            val lat = obj.getDouble("lat")
            val lon = obj.getDouble("lon")
            suggestions.add(CitySuggestion(name, lat, lon))
        }
        suggestions
    } catch (e: Exception) {
        emptyList()
    }
}
