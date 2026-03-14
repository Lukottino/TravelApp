package com.example.travelapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.travelapp.viewmodel.AppViewModel
import androidx.navigation.NavController

@Composable
fun TravelApp(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsState()

    // Controlla la route corrente per decidere se mostrare la BottomBar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute !in listOf("login", "register", "addTrip") &&
        currentRoute?.startsWith("tripDetail/") == false &&
        currentRoute?.startsWith("editTrip/") == false

    Scaffold(
        bottomBar = { if (showBottomBar) BottomBar(navController, viewModel) }
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
            composable("trips") { TripsScreen(viewModel = viewModel, navController = navController) }
            composable("addTrip") {
                AddTripScreen(
                    viewModel = viewModel,
                    onTripAdded = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("tripDetail/{tripId}") { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId")?.toInt() ?: 0
                TripDetailScreen(
                    viewModel = viewModel,
                    tripId = tripId,
                    onEdit = { navController.navigate("editTrip/$tripId") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("editTrip/{tripId}") { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId")?.toInt() ?: 0
                EditTripScreen(
                    viewModel = viewModel,
                    tripId = tripId,
                    onTripUpdated = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }


            composable("map") { MapScreen(viewModel, navController) }

            composable("friends") {
                FriendsScreen(viewModel = viewModel)
            }

            // Profile
            composable("profile") {
                ProfileScreen(
                    viewModel = viewModel,
                    onLogout = {
                        viewModel.clearCurrentUser()
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    },
                    onTripClick = { tripId -> navController.navigate("tripDetail/$tripId") }
                )
            }
        }
    }
}
