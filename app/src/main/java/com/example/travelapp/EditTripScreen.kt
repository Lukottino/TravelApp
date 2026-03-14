package com.example.travelapp

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.travelapp.data.model.computeTripStatus
import com.example.travelapp.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTripScreen(viewModel: AppViewModel, tripId: Int, onTripUpdated: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val tripState by viewModel.getTripById(tripId).observeAsState()
    val trip = tripState ?: return

    var name by remember { mutableStateOf(trip.name) }
    var location by remember { mutableStateOf(trip.destination) }
    var startDate by remember { mutableStateOf(trip.startDate) }
    var endDate by remember { mutableStateOf(trip.endDate ?: trip.startDate) }
    var notes by remember { mutableStateOf(trip.notes ?: "") }
    var coverImageUri by remember { mutableStateOf(trip.coverImageUri) }
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

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifica Viaggio") },
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
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome Viaggio") })
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Luogo") })
            Spacer(modifier = Modifier.height(8.dp))

            // Data inizio
            OutlinedTextField(
                value = dateFormat.format(Date(startDate)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Data Inizio") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker(context) { startDate = it } }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleziona Data Inizio")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Data fine
            OutlinedTextField(
                value = endDate?.let { dateFormat.format(Date(it)) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Data Fine") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker(context) { endDate = it } }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleziona Data Fine")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Note") })
            Spacer(modifier = Modifier.height(8.dp))

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
                    if (name.isBlank() || location.isBlank()) {
                        Toast.makeText(context, "Compila tutti i campi correttamente", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val updatedTrip = trip.copy(
                        name = name,
                        destination = location,
                        startDate = startDate,
                        endDate = endDate,
                        notes = notes.ifBlank { null },
                        coverImageUri = coverImageUri,
                        status = computeTripStatus(endDate)
                    )

                    scope.launch {
                        viewModel.updateTrip(updatedTrip)
                        Toast.makeText(context, "Viaggio aggiornato!", Toast.LENGTH_SHORT).show()
                        onTripUpdated()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salva Modifiche")
            }
        }
    }
}
