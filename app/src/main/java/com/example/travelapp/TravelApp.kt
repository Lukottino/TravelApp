package com.example.travelapp

import RegisterScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.travelapp.viewmodel.AppViewModel

@Composable
fun TravelApp(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsState()

    // Controlla la route corrente per decidere se mostrare la BottomBar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != "login" && currentRoute != "register"

    Scaffold(
        bottomBar = { if (showBottomBar) BottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (currentUser != null) "home" else "login",
            modifier = Modifier.padding(padding)
        ) {
            // Login
            composable("login") {
                LoginScreen(
                    viewModel,
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register") }
                )
            }

            // Register
            composable("register") {
                RegisterScreen(
                    viewModel,
                    onRegisterSuccess = {
                        navController.navigate("home") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                )
            }

            // Home / Tabs
            composable("home") { HomeScreen(viewModel) }
            composable("trips") { TripsScreen(viewModel) }
            composable("add") {
                AddTripScreen(viewModel) {
                    navController.navigate("trips") {
                        popUpTo("add") { inclusive = true }
                    }
                }
            }
            composable("map") { MapScreen(viewModel) }

            // Profile
            composable("profile") {
                ProfileScreen(viewModel) {
                    viewModel.clearCurrentUser()
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            }
        }
    }
}
