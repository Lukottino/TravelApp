package com.example.travelapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travelapp.viewmodel.AppViewModel
import com.example.travelapp.data.model.Trip

@Composable
fun TripsScreen(viewModel: AppViewModel) {
    val trips by viewModel.getTripsForCurrentUser().observeAsState(emptyList())

    if (trips.isEmpty()) {
        Text("Nessun viaggio trovato per questo utente")
    } else {
        LazyColumn {
            items(trips) { trip ->
                TripItem(trip)
            }
        }
    }
}



@Composable
fun TripItem(trip: Trip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = trip.destination, // ad esempio
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${trip.startDate} - ${trip.endDate}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = trip.notes ?: "Nessuna descrizione",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

