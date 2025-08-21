package com.example.travelapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travelapp.viewmodel.AppViewModel
import com.example.travelapp.data.model.Trip
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TripsScreen(viewModel: AppViewModel, navController: NavController) {
    val trips by viewModel.getTripsForCurrentUser().observeAsState(emptyList())

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
                items(trips) { trip ->
                    TripItem(trip = trip, onClick = {
                        // Naviga alla schermata di modifica passando l'id del viaggio
                        navController.navigate("editTrip/${trip.id}")
                    })
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
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val startDateStr = dateFormat.format(Date(trip.startDate))
    val endDateStr = trip.endDate?.let { dateFormat.format(Date(it)) } ?: "In corso"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // <--- Card cliccabile
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = trip.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Destinazione: ${trip.destination}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Dal $startDateStr al $endDateStr", style = MaterialTheme.typography.bodySmall)
            if (!trip.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Note: ${trip.notes}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
