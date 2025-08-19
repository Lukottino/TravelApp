package com.example.travelapp

import RegisterScreen
import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.travelapp.data.UserPreferences
import com.example.travelapp.viewmodel.AppViewModel
import com.example.travelapp.viewmodel.AppViewModelFactory

@Composable
fun TravelApp(viewModel: AppViewModel) {
    val context = LocalContext.current
    val loggedUserId by UserPreferences.getUserId(context).collectAsState(initial = null)
    val navController = rememberNavController()

    val startDestination = if (loggedUserId != null) "home" else "login"
    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    viewModel,
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register") }
                )
            }
            composable("register") {
                RegisterScreen(
                    viewModel,
                    onRegisterSuccess = {
                        navController.navigate("home") {
                            popUpTo("register") {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            composable("home") { HomeScreen(viewModel) }
            composable("trips") { TripsScreen(viewModel) }
            composable("add") { AddTripScreen(viewModel) }
            composable("map") { MapScreen(viewModel) }
            composable("profile") { ProfileScreen(viewModel) }
        }
    }
}
