package com.example.travelapp

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.travelapp.viewmodel.AppViewModel
import com.example.travelapp.viewmodel.AppViewModelFactory

@Composable
fun TravelApp(context: Context) {
    val navController = rememberNavController()

    // Creo il ViewModel
    val appViewModel: AppViewModel = viewModel(
        factory = AppViewModelFactory(context.applicationContext)
    )

    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(appViewModel) }
            composable("trips") { TripsScreen(appViewModel) }
            composable("add") { AddTripScreen(appViewModel) }
            composable("map") { MapScreen(appViewModel) }
            composable("profile") { ProfileScreen(appViewModel) }
        }
    }
}
