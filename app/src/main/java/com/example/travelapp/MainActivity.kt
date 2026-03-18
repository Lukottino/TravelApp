package com.example.travelapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.example.travelapp.ui.theme.TravelAppTheme
import com.example.travelapp.viewmodel.AppViewModel
import com.example.travelapp.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels { AppViewModelFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.observeAsState()
            TravelAppTheme(themeMode = settings?.themeMode ?: "AUTO") {
                TravelApp(viewModel = viewModel)
            }
        }
    }
}
