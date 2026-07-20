package com.example.travelapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.lazy.LazyRow
import com.example.travelapp.data.model.TripStatus
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.travelapp.viewmodel.AppViewModel
import com.example.travelapp.data.model.Trip
import androidx.navigation.NavController
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TripsScreen(viewModel: AppViewModel, navController: NavController) {
    val trips by viewModel.getTripsForCurrentUser().observeAsState(emptyList())
    var selectedStatus by remember { mutableStateOf<TripStatus?>(null) }

    val filteredTrips = if (selectedStatus == null) trips
                        else trips.filter { it.status == selectedStatus }

    Box(modifier = Modifier.fillMaxSize()) {
        if (trips.isEmpty()) {
            Text(
                "Nessun viaggio trovato per questo utente",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "I tuoi viaggi",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedStatus == null,
                                onClick = { selectedStatus = null },
                                label = { Text("Tutti") }
                            )
                        }
                        items(TripStatus.entries) { status ->
                            FilterChip(
                                selected = selectedStatus == status,
                                onClick = { selectedStatus = if (selectedStatus == status) null else status },
                                label = { Text(status.label) }
                            )
                        }
                    }
                }
                if (filteredTrips.isEmpty()) {
                    item {
                        Text(
                            "Nessun viaggio con questo stato",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else {
                    items(filteredTrips) { trip ->
                        TripItem(trip = trip, onClick = {
                            navController.navigate("tripDetail/${trip.id}")
                        })
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate("addTrip") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Aggiungi viaggio")
        }
    }
}

@Composable
fun TripItem(trip: Trip, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            if (trip.coverImageUri != null) {
                AsyncImage(
                    model = trip.coverImageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trip.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = trip.destination,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                val statusContainerColor = when (trip.status) {
                    com.example.travelapp.data.model.TripStatus.DRAFT -> androidx.compose.ui.graphics.Color(0xFF9E9E9E)
                    com.example.travelapp.data.model.TripStatus.PLANNED -> androidx.compose.ui.graphics.Color(0xFF9E9E9E)
                    com.example.travelapp.data.model.TripStatus.IN_PROGRESS -> androidx.compose.ui.graphics.Color(0xFFFFC107)
                    com.example.travelapp.data.model.TripStatus.COMPLETED -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                }
                val statusContentColor = androidx.compose.ui.graphics.Color.White
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                    color = statusContainerColor,
                    modifier = Modifier.width(100.dp)
                ) {
                    Text(
                        text = trip.status.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusContentColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
