package com.example.travelapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelapp.ui.theme.TravelAppTheme
import com.example.travelapp.viewmodel.AppViewModel
import com.example.travelapp.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelAppTheme {
                // Ottieni il ViewModel in modo corretto
                val viewModel: AppViewModel by viewModels { AppViewModelFactory(this) }


                TravelApp(viewModel = viewModel)
            }
        }
    }
}
