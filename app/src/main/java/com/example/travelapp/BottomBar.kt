package com.example.travelapp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.travelapp.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(navController: NavController, viewModel: AppViewModel) {
    val items = listOf(
        BottomNavItem("home", Icons.Default.Home),
        BottomNavItem("trips", Icons.Default.List),
        BottomNavItem("map", Icons.Default.Place),
        BottomNavItem("friends", Icons.Default.Group),
        BottomNavItem("profile", Icons.Default.Person)
    )

    val incomingRequests by viewModel.incomingRequests.collectAsState()

    NavigationBar {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    if (item.route == "friends" && incomingRequests.isNotEmpty()) {
                        BadgedBox(badge = { Badge { Text(incomingRequests.size.toString()) } }) {
                            Icon(item.icon, contentDescription = item.route)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.route)
                    }
                },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
