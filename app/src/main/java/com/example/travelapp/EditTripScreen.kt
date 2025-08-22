package com.example.travelapp

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
                        notes = notes.ifBlank { null }
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
