package com.example.travelapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import com.example.travelapp.data.model.Trip
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.travelapp.viewmodel.AppViewModel
import java.io.File

@Composable
fun ProfileScreen(viewModel: AppViewModel, onLogout: () -> Unit, onTripClick: (Int) -> Unit = {}) {
    val currentUser by viewModel.currentUser.collectAsState()
    if (currentUser == null) return

    var editMode by remember { mutableStateOf(false) }

    if (editMode) {
        EditProfileContent(viewModel = viewModel, onDone = { editMode = false })
    } else {
        ViewProfileContent(
            viewModel = viewModel,
            onEditClick = { editMode = true },
            onLogout = onLogout,
            onTripClick = onTripClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewProfileContent(
    viewModel: AppViewModel,
    onEditClick: () -> Unit,
    onLogout: () -> Unit,
    onTripClick: (Int) -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val trips by viewModel.getTripsForCurrentUser().observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        AvatarImage(uri = user?.profileImageUri, modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = user?.name ?: "", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onEditClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Modifica profilo")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onLogout) {
            Text("Logout", color = MaterialTheme.colorScheme.error)
        }

        if (trips.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "I miei viaggi (${trips.size})",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))
            TripGrid(trips = trips, onTripClick = onTripClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileContent(
    viewModel: AppViewModel,
    onDone: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val currentUser = user ?: return
    val context = LocalContext.current

    var name by remember { mutableStateOf(currentUser.name) }
    var photoUri by remember { mutableStateOf(currentUser.profileImageUri) }
    var showPhotoPicker by remember { mutableStateOf(false) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var currentPasswordError by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // URI temporaneo per la fotocamera
    val cameraUri = remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraUri.value?.let { photoUri = it.toString() }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createCameraUri(context)
            cameraUri.value = uri
            cameraLauncher.launch(uri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            photoUri = it.toString()
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            photoUri = it.toString()
        }
    }

    if (showPhotoPicker) {
        ModalBottomSheet(onDismissRequest = { showPhotoPicker = false }) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                PhotoPickerOption(icon = Icons.Default.CameraAlt, label = "Scatta una foto") {
                    showPhotoPicker = false
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        val uri = createCameraUri(context)
                        cameraUri.value = uri
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
                PhotoPickerOption(icon = Icons.Default.Photo, label = "Scegli dalla galleria") {
                    showPhotoPicker = false
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                PhotoPickerOption(icon = Icons.Default.Folder, label = "Scegli dai file") {
                    showPhotoPicker = false
                    fileLauncher.launch(arrayOf("image/*"))
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(contentAlignment = Alignment.BottomEnd) {
            AvatarImage(
                uri = photoUri,
                modifier = Modifier.size(100.dp).clickable { showPhotoPicker = true }
            )
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Cambia foto", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; nameError = null },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth(),
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it) } }
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Cambia password", style = MaterialTheme.typography.titleSmall, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it; currentPasswordError = null },
            label = { Text("Password attuale") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = currentPasswordError != null,
            supportingText = currentPasswordError?.let { { Text(it) } }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it; newPasswordError = null },
            label = { Text("Nuova password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = newPasswordError != null,
            supportingText = newPasswordError?.let { { Text(it) } }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmPasswordError = null },
            label = { Text("Conferma nuova password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = confirmPasswordError != null,
            supportingText = confirmPasswordError?.let { { Text(it) } }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                var valid = true

                if (name.isBlank()) {
                    nameError = "Il nome non può essere vuoto"
                    valid = false
                }

                val passwordChanged = currentPassword.isNotBlank() || newPassword.isNotBlank() || confirmPassword.isNotBlank()
                if (passwordChanged) {
                    if (currentPassword != currentUser.password) {
                        currentPasswordError = "Password attuale errata"
                        valid = false
                    }
                    if (newPassword.length < 6) {
                        newPasswordError = "Almeno 6 caratteri"
                        valid = false
                    }
                    if (newPassword != confirmPassword) {
                        confirmPasswordError = "Le password non coincidono"
                        valid = false
                    }
                }

                if (valid) {
                    val updatedUser = currentUser.copy(
                        name = name.trim(),
                        password = if (passwordChanged) newPassword else currentUser.password,
                        profileImageUri = photoUri
                    )
                    viewModel.updateUser(updatedUser, onSuccess = onDone)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salva")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Annulla")
        }
    }
}

@Composable
private fun PhotoPickerOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier.clickable { onClick() }
    )
}

private fun createCameraUri(context: Context): Uri {
    val dir = File(context.cacheDir, "images").also { it.mkdirs() }
    val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
fun TripGrid(trips: List<Trip>, onTripClick: (Int) -> Unit) {
    val rows = trips.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { trip ->
                    TripGridItem(trip = trip, modifier = Modifier.weight(1f), onClick = { onTripClick(trip.id) })
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TripGridItem(trip: Trip, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.BottomStart
    ) {
        if (trip.coverImageUri != null) {
            AsyncImage(
                model = trip.coverImageUri,
                contentDescription = trip.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.FlightTakeoff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            text = trip.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (trip.coverImageUri != null) androidx.compose.ui.graphics.Color.White
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(6.dp),
            maxLines = 2
        )
    }
}

@Composable
fun AvatarImage(uri: String?, modifier: Modifier = Modifier) {
    if (uri != null) {
        AsyncImage(
            model = uri,
            contentDescription = "Foto profilo",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
    } else {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Foto profilo",
            modifier = modifier,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
