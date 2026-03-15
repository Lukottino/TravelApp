package com.example.travelapp

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero cover image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clickable { showImagePicker = true }
        ) {
            if (coverImageUri != null) {
                AsyncImage(
                    model = coverImageUri,
                    contentDescription = "Copertina",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                            )
                        )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.25f), Color.Black.copy(alpha = 0.6f))
                        )
                    )
            )
            // Titolo in basso
            Text(
                text = "Modifica Viaggio",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
            )
            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Indietro", tint = Color.White)
            }
            // Cambia foto
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
            ) {
                Icon(
                    Icons.Default.AddPhotoAlternate,
                    contentDescription = "Cambia copertina",
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // Form
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome Viaggio") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Luogo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Date affiancate
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dateFormat.format(Date(startDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Inizio") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker(context) { startDate = it } }) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        }
                    }
                )
                OutlinedTextField(
                    value = endDate?.let { dateFormat.format(Date(it)) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fine") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker(context) { endDate = it } }) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        }
                    }
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

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
                        status = computeTripStatus(startDate, endDate)
                    )
                    scope.launch {
                        viewModel.updateTrip(updatedTrip)
                        Toast.makeText(context, "Viaggio aggiornato!", Toast.LENGTH_SHORT).show()
                        onTripUpdated()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Salva Modifiche", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
