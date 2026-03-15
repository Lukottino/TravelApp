package com.example.travelapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.travelapp.data.model.Trip
import com.example.travelapp.data.model.TripStatus
import com.example.travelapp.data.model.User
import com.example.travelapp.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(viewModel: AppViewModel, navController: NavController) {
    val feed by viewModel.getFeed().observeAsState(emptyList())
    val currentUser by viewModel.currentUser.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Text(
            text = "Bon Voyage",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        if (feed.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Nessun viaggio nel feed", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Aggiungi amici per vedere i loro viaggi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(feed) { trip ->
                    FeedCard(
                        trip = trip,
                        viewModel = viewModel,
                        onClick = { navController.navigate("tripDetail/${trip.id}") }
                    )
                }
            }
        }
    }
}

@Composable
fun FeedCard(trip: Trip, viewModel: AppViewModel, onClick: () -> Unit) {
    val author by produceState<User?>(null, trip.userId) {
        value = viewModel.getUserById(trip.userId)
    }
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateStr = trip.endDate?.let {
        "Dal ${dateFormat.format(Date(trip.startDate))} al ${dateFormat.format(Date(it))}"
    } ?: "Dal ${dateFormat.format(Date(trip.startDate))}"

    val statusContainerColor = when (trip.status) {
        TripStatus.DRAFT -> MaterialTheme.colorScheme.surfaceVariant
        TripStatus.PLANNED -> MaterialTheme.colorScheme.secondaryContainer
        TripStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
        TripStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val statusContentColor = when (trip.status) {
        TripStatus.DRAFT -> MaterialTheme.colorScheme.onSurfaceVariant
        TripStatus.PLANNED -> MaterialTheme.colorScheme.onSecondaryContainer
        TripStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onPrimaryContainer
        TripStatus.COMPLETED -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // Cover image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
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
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                            )
                        )
                )
                // Trip name + destination
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = trip.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = trip.destination,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            // Info row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Author + date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar iniziale
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (author?.profileImageUri != null) {
                            AsyncImage(
                                model = author!!.profileImageUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = author?.name?.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = author?.name ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status chip
                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusContainerColor
                ) {
                    Text(
                        text = trip.status.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusContentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
