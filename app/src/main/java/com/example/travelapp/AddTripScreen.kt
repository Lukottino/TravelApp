package com.example.travelapp

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.travelapp.data.model.Trip
import com.example.travelapp.data.model.TripStatus
import com.example.travelapp.data.model.computeTripStatus
import com.example.travelapp.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    var geocodeJob by remember { mutableStateOf<Job?>(null) }

    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var notes by remember { mutableStateOf("") }
    var coverImageUri by remember { mutableStateOf<String?>(null) }
    var showDraftDialog by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            coverImageUri = it.toString()
        }
    }
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            coverImageUri = it.toString()
        }
    }

    val currentUserId = viewModel.currentUser.value?.id ?: 0
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aggiungi Viaggio") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (name.isNotBlank()) showDraftDialog = true else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->

        if (showDraftDialog) {
            AlertDialog(
                onDismissRequest = { showDraftDialog = false },
                title = { Text("Salva come bozza?") },
                text = { Text("Vuoi salvare il viaggio come bozza?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDraftDialog = false
                        val city = selectedCity
                        val start = startDate ?: System.currentTimeMillis()
                        val draft = Trip(
                            name = name.ifBlank { "Bozza" },
                            destination = city?.displayName ?: "",
                            latitude = city?.lat,
                            longitude = city?.lon,
                            startDate = start,
                            endDate = endDate,
                            notes = notes.ifBlank { null },
                            userId = currentUserId,
                            coverImageUri = coverImageUri,
                            status = TripStatus.DRAFT
                        )
                        scope.launch { viewModel.addTrip(draft); onBack() }
                    }) { Text("Salva bozza") }
                },
                dismissButton = {
                    TextButton(onClick = { showDraftDialog = false; onBack() }) { Text("Scarta") }
                }
            )
        }

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
                    geocodeJob?.cancel()
                    if (it.isNotBlank()) {
                        geocodeJob = scope.launch {
                            delay(500)
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
            Spacer(modifier = Modifier.height(8.dp))

            // Immagine copertina
            if (showImagePicker) {
                ModalBottomSheet(onDismissRequest = { showImagePicker = false }) {
                    Column(modifier = Modifier.padding(bottom = 32.dp)) {
                        ListItem(
                            headlineContent = { Text("Scegli dalla galleria") },
                            leadingContent = { Icon(Icons.Default.Photo, contentDescription = null) },
                            modifier = Modifier.clickable {
                                showImagePicker = false
                                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Scegli dai file") },
                            leadingContent = { Icon(Icons.Default.Folder, contentDescription = null) },
                            modifier = Modifier.clickable {
                                showImagePicker = false
                                fileLauncher.launch(arrayOf("image/*"))
                            }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .clickable { showImagePicker = true },
                contentAlignment = Alignment.Center
            ) {
                if (coverImageUri != null) {
                    AsyncImage(
                        model = coverImageUri,
                        contentDescription = "Copertina",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Aggiungi copertina", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val city = selectedCity
                    val start = startDate
                    if (name.isBlank() || city == null || start == null) {
                        Toast.makeText(context, "Compila nome, città e data inizio", Toast.LENGTH_SHORT).show()
                    } else {
                        val newTrip = Trip(
                            id = 0,
                            name = name,
                            destination = city.displayName,
                            latitude = city.lat,
                            longitude = city.lon,
                            startDate = start,
                            endDate = endDate,
                            notes = notes.ifBlank { null },
                            userId = currentUserId,
                            coverImageUri = coverImageUri,
                            status = computeTripStatus(start, endDate)
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
