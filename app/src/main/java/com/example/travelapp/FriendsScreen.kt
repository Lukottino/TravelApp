package com.example.travelapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.travelapp.data.model.FriendRequest
import com.example.travelapp.data.model.Trip
import com.example.travelapp.data.model.TripStatus
import com.example.travelapp.data.model.User
import com.example.travelapp.viewmodel.AppViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(viewModel: AppViewModel, navController: NavController) {
    val friends by viewModel.friends.collectAsState()
    val incomingRequests by viewModel.incomingRequests.collectAsState()
    val sentRequests by viewModel.sentRequests.collectAsState()

    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<User>>(emptyList()) }
    var showRequests by remember { mutableStateOf(false) }
    var selectedFriend by remember { mutableStateOf<User?>(null) }

    // Ricerca reattiva con debounce 300ms
    LaunchedEffect(query) {
        if (query.isBlank()) {
            searchResults = emptyList()
        } else {
            delay(300)
            viewModel.searchUsers(query) { searchResults = it }
        }
    }

    selectedFriend?.let { friend ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            onDismissRequest = { selectedFriend = null },
            sheetState = sheetState
        ) {
            FriendProfileSheet(
                user = friend,
                viewModel = viewModel,
                onTripClick = { tripId ->
                    selectedFriend = null
                    navController.navigate("tripDetail/$tripId")
                }
            )
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Amici") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Search bar
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Cerca per nome o email...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.large
                )
            }

            // Stato idle: nessuna ricerca attiva
            if (query.isBlank()) {

                // Sezione richieste pendenti
                if (incomingRequests.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        RequestsBanner(
                            count = incomingRequests.size,
                            expanded = showRequests,
                            onToggle = { showRequests = !showRequests }
                        )
                    }

                    if (showRequests) {
                        items(incomingRequests, key = { it.id }) { request ->
                            RequestItem(request = request, viewModel = viewModel)
                        }
                    }
                }

                // Lista amici
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "I tuoi amici (${friends.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (friends.isEmpty()) {
                    item {
                        Text(
                            "Nessun amico ancora. Cercane uno qui sopra!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else {
                    items(friends, key = { it.id }) { friend ->
                        FriendItem(user = friend, onClick = { selectedFriend = friend })
                    }
                }

            } else {
                // Stato ricerca attiva
                if (searchResults.isEmpty()) {
                    item {
                        Text(
                            "Nessun utente trovato",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else {
                    items(searchResults, key = { it.id }) { user ->
                        SearchResultItem(
                            user = user,
                            isFriend = friends.any { it.id == user.id },
                            hasSentRequest = sentRequests.any { it.receiverId == user.id },
                            incomingRequest = incomingRequests.firstOrNull { it.senderId == user.id },
                            onSendRequest = { viewModel.sendFriendRequest(user.id) },
                            onAcceptRequest = { req -> viewModel.acceptFriendRequest(req) }
                        )
                    }
                }
            }
        }
    }
}

// --- Banner richieste ---

@Composable
private fun RequestsBanner(count: Int, expanded: Boolean, onToggle: () -> Unit) {
    Card(
        onClick = onToggle,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "📩  $count ${if (count == 1) "richiesta in arrivo" else "richieste in arrivo"}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            Text(
                if (expanded) "Nascondi" else "Mostra",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// --- Amico ---

@Composable
private fun FriendItem(user: User, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarImage(uri = user.profileImageUri, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(user.name, style = MaterialTheme.typography.bodyLarge)
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// --- Richiesta in arrivo ---

@Composable
private fun RequestItem(request: FriendRequest, viewModel: AppViewModel) {
    var sender by remember { mutableStateOf<User?>(null) }
    LaunchedEffect(request.senderId) {
        sender = viewModel.getUserById(request.senderId)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarImage(uri = sender?.profileImageUri, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(sender?.name ?: "...", style = MaterialTheme.typography.bodyLarge)
                Text(sender?.email ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.rejectFriendRequest(request) }) { Text("Rifiuta") }
                Button(onClick = { viewModel.acceptFriendRequest(request) }) { Text("Accetta") }
            }
        }
    }
}

// --- Profilo amico ---

@Composable
private fun FriendProfileSheet(user: User, viewModel: AppViewModel, onTripClick: (Int) -> Unit) {
    var trips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    LaunchedEffect(user.id) {
        trips = viewModel.getTripsForUser(user.id).filter { it.status != TripStatus.DRAFT }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AvatarImage(uri = user.profileImageUri, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(user.name, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (trips.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Viaggi (${trips.size})",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            TripGrid(trips = trips, onTripClick = onTripClick)
        } else {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Nessun viaggio ancora",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// --- Risultato di ricerca ---

@Composable
private fun SearchResultItem(
    user: User,
    isFriend: Boolean,
    hasSentRequest: Boolean,
    incomingRequest: FriendRequest?,
    onSendRequest: () -> Unit,
    onAcceptRequest: (FriendRequest) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarImage(uri = user.profileImageUri, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, style = MaterialTheme.typography.bodyLarge)
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            when {
                isFriend -> Text("Amici", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                incomingRequest != null -> Button(onClick = { onAcceptRequest(incomingRequest) }) { Text("Accetta") }
                hasSentRequest -> Text("Inviata", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> IconButton(onClick = onSendRequest) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Aggiungi", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
