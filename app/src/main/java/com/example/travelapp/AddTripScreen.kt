package com.example.travelapp

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.travelapp.data.model.Trip
import com.example.travelapp.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AddTripScreen(viewModel: AppViewModel, onTripAdded: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") } // "yyyy-MM-dd"
    var endDate by remember { mutableStateOf("") }   // "yyyy-MM-dd"
    var notes by remember { mutableStateOf("") }

    // Recupera ID utente corrente
    val currentUserId = viewModel.currentUser.value?.id ?: 0

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Aggiungi Viaggio", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome Viaggio") })
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Luogo") })
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("Data Inizio (yyyy-MM-dd)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text("Data Fine (yyyy-MM-dd)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Note") })
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val startMillis = dateFormat.parse(startDate)?.time
            val endMillis = dateFormat.parse(endDate)?.time

            if (name.isBlank() || location.isBlank() || startMillis == null || endMillis == null) {
                Toast.makeText(context, "Compila tutti i campi correttamente", Toast.LENGTH_SHORT).show()
                return@Button
            }

            val newTrip = Trip(
                id = 0,
                name = name,
                destination = location,
                startDate = startMillis,
                endDate = endMillis,
                notes = notes.ifBlank { null },
                userId = currentUserId
            )

            scope.launch {
                viewModel.addTrip(newTrip)
                onTripAdded()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Salva Viaggio")
        }
    }
}
