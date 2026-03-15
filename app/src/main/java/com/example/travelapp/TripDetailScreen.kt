package com.example.travelapp

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travelapp.data.model.TripPhoto
import com.example.travelapp.data.model.TripStatus
import com.example.travelapp.data.model.User
import com.example.travelapp.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    viewModel: AppViewModel,
    tripId: Int,
    onEdit: () -> Unit,
    onBack: () -> Unit
) {
    val tripState by viewModel.getTripById(tripId).observeAsState()
    val trip = tripState ?: return

    val currentUser by viewModel.currentUser.collectAsState()
    val participants by viewModel.getParticipants(tripId).collectAsState(initial = emptyList())
    val friends by viewModel.friends.collectAsState()
    val photos by viewModel.getPhotosForTrip(tripId).collectAsState(initial = emptyList())
    val isOwner = currentUser?.id == trip.userId
    val isParticipant = participants.any { it.id == currentUser?.id }
    val canAddPhoto = isOwner || isParticipant

    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddParticipantSheet by remember { mutableStateOf(false) }
    var showPhotoPickerSheet by remember { mutableStateOf(false) }
    var selectedPhoto by remember { mutableStateOf<TripPhoto?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.addPhoto(tripId, it.toString())
        }
    }
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.addPhoto(tripId, it.toString())
        }
    }
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Elimina viaggio") },
            text = { Text("Sei sicuro di voler eliminare \"${trip.name}\"? L'operazione non è reversibile.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTrip(trip)
                    showDeleteDialog = false
                    onBack()
                }) {
                    Text("Elimina", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    val startStr = dateFormat.format(Date(trip.startDate))
    val dateRangeStr = trip.endDate?.let { "Dal $startStr al ${dateFormat.format(Date(it))}" } ?: "Dal $startStr"

    val statusContainerColor = when (trip.status) {
        TripStatus.DRAFT -> MaterialTheme.colorScheme.surfaceVariant
        TripStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
        TripStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val statusContentColor = when (trip.status) {
        TripStatus.DRAFT -> MaterialTheme.colorScheme.onSurfaceVariant
        TripStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onPrimaryContainer
        TripStatus.COMPLETED -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        // Hero section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            if (trip.coverImageUri != null) {
                AsyncImage(
                    model = trip.coverImageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                )
            }
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.25f), Color.Black.copy(alpha = 0.65f))
                        )
                    )
            )
            // Trip name + destination at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = trip.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(trip.destination, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
                }
            }
            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Indietro", tint = Color.White)
            }
            // Edit + Delete (solo per l'owner)
            if (isOwner) {
                Row(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifica", tint = Color.White)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = Color.White.copy(alpha = 0.9f))
                    }
                }
            }
        }

        // Content
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status + date row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusContainerColor
                ) {
                    Text(
                        text = trip.status.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = statusContentColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(dateRangeStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (!trip.notes.isNullOrBlank()) {
                DetailCard(label = "Note", value = trip.notes)
            }

            // Sezione foto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Foto", style = MaterialTheme.typography.titleMedium)
                if (canAddPhoto) {
                    IconButton(onClick = { showPhotoPickerSheet = true }) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Aggiungi foto")
                    }
                }
            }

            if (photos.isEmpty()) {
                Text("Nessuna foto aggiunta", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photos) { photo ->
                        val canDelete = isOwner || photo.addedBy == currentUser?.id
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedPhoto = photo }
                        ) {
                            AsyncImage(
                                model = photo.imageUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (canDelete) {
                                IconButton(
                                    onClick = { viewModel.deletePhoto(photo) },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Elimina foto",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showPhotoPickerSheet) {
                ModalBottomSheet(onDismissRequest = { showPhotoPickerSheet = false }) {
                    Column(modifier = Modifier.padding(bottom = 32.dp)) {
                        ListItem(
                            headlineContent = { Text("Scegli dalla galleria") },
                            leadingContent = { Icon(Icons.Default.Photo, contentDescription = null) },
                            modifier = Modifier.clickable {
                                showPhotoPickerSheet = false
                                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Scegli dai file") },
                            leadingContent = { Icon(Icons.Default.Folder, contentDescription = null) },
                            modifier = Modifier.clickable {
                                showPhotoPickerSheet = false
                                fileLauncher.launch(arrayOf("image/*"))
                            }
                        )
                    }
                }
            }

            // Sezione partecipanti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Partecipanti", style = MaterialTheme.typography.titleMedium)
                if (isOwner) {
                    IconButton(onClick = { showAddParticipantSheet = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Aggiungi partecipante")
                    }
                }
            }
            participants.forEach { user ->
                val isCurrentUser = user.id == currentUser?.id
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCurrentUser) "${user.name} (tu)" else user.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (isOwner && !isCurrentUser) {
                        IconButton(onClick = { viewModel.removeParticipant(tripId, user.id) }) {
                            Icon(Icons.Default.PersonRemove, contentDescription = "Rimuovi", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            if (showAddParticipantSheet) {
                val friendsNotInTrip = friends.filter { f -> participants.none { p -> p.id == f.id } }
                ModalBottomSheet(onDismissRequest = { showAddParticipantSheet = false }) {
                    Column(modifier = Modifier.padding(bottom = 32.dp)) {
                        Text(
                            "Aggiungi amico al viaggio",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                        if (friendsNotInTrip.isEmpty()) {
                            Text(
                                "Tutti i tuoi amici sono già nel viaggio",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            friendsNotInTrip.forEach { friend ->
                                ListItem(
                                    headlineContent = { Text(friend.name) },
                                    supportingContent = { Text(friend.email) },
                                    modifier = Modifier.clickable {
                                        viewModel.addParticipant(tripId, friend.id)
                                        showAddParticipantSheet = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } // end content Column
    } // end outer Column

    // Fullscreen photo viewer
    selectedPhoto?.let { photo ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable { selectedPhoto = null }
        ) {
            AsyncImage(
                model = photo.imageUri,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = { selectedPhoto = null },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Chiudi", tint = Color.White)
            }
        }
    }
}

@Composable
private fun DetailCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
