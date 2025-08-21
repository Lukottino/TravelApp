package com.example.travelapp

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat

@Composable
fun DatePickerField(
    label: String,
    date: Long?,
    dateFormat: SimpleDateFormat,
    onDateSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    OutlinedTextField(
        value = date?.let { dateFormat.format(java.util.Date(it)) } ?: "",
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
