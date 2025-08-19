package com.example.travelapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelapp.R
import com.example.travelapp.data.UserPreferences
import com.example.travelapp.data.model.User
import com.example.travelapp.viewmodel.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(viewModel: AppViewModel, onLogout: () -> Unit) {
    val context = LocalContext.current
    val users by viewModel.allUsers.observeAsState(emptyList())
    val loggedUserId by UserPreferences.getUserId(context).collectAsState(initial = null)

    // Trova l'utente corrente usando l'ID loggato
    val currentUser = users.firstOrNull { it.id == loggedUserId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "Foto profilo",
            tint = Color.Gray,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = currentUser?.name ?: "Utente sconosciuto",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = currentUser?.email ?: "N/A",
            fontSize = 16.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* TODO: modifica profilo */ },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Modifica Profilo")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                // Logout
                CoroutineScope(Dispatchers.IO).launch {
                    UserPreferences.clearUserId(context)
                    withContext(Dispatchers.Main) { onLogout() }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Logout", color = Color.White)
        }
    }
}
