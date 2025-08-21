package com.example.travelapp

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.travelapp.data.model.Trip
import com.example.travelapp.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddTripScreen(viewModel: AppViewModel, onTripAdded: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var notes by remember { mutableStateOf("") }

    val currentUserId = viewModel.currentUser.value?.id ?: 0
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Aggiungi Viaggio", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome Viaggio") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Luogo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Data inizio
        DatePickerField(
            label = "Data Inizio",
            date = startDate,
            dateFormat = dateFormat,
            onDateSelected = { startDate = it }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Data fine (opzionale)
        DatePickerField(
            label = "Data Fine (opzionale)",
            date = endDate,
            dateFormat = dateFormat,
            onDateSelected = { endDate = it }
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Note") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isBlank() || location.isBlank() || startDate == null) {
                    Toast.makeText(context, "Compila nome, luogo e data inizio", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val newTrip = Trip(
                    id = 0,
                    name = name,
                    destination = location,
                    startDate = startDate!!,
                    endDate = endDate,
                    notes = notes.ifBlank { null },
                    userId = currentUserId
                )

                scope.launch {
                    viewModel.addTrip(newTrip)
                    onTripAdded()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salva Viaggio")
        }
    }
}

@Composable
fun DatePickerField(
    label: String,
    date: Long?,
    dateFormat: SimpleDateFormat,
    onDateSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    OutlinedTextField(
        value = date?.let { dateFormat.format(Date(it)) } ?: "",
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { showDatePicker(context, onDateSelected) }) {
                Icon(Icons.Default.DateRange, contentDescription = label)
            }
        }
    )
}
