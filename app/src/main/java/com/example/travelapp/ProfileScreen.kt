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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelapp.R
import com.example.travelapp.data.model.User
import com.example.travelapp.viewmodel.AppViewModel

@Composable
fun ProfileScreen(viewModel: AppViewModel) {
    // se allUsers è LiveData
    val users by viewModel.allUsers.observeAsState(emptyList())

    // se invece è Flow<List<User>>
    // val users by viewModel.allUsers.collectAsState(initial = emptyList())

    val user = users.firstOrNull() ?: User(
        id = 0,
        name = "Mario Rossi",
        email = "mario.rossi@example.com",
        password = "ciao123"
    )

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
            text = user.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = user.email,
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
            onClick = { /* TODO: logout */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Logout", color = Color.White)
        }
    }
}
