package com.example.reciclapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.reciclapp.ui.theme.ReciclappTheme
import com.example.reciclapp.views.RegisterScreen
import com.example.reciclapp.views.ScanQrScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReciclappTheme {
                // 1. Setup del NavController
                val navController = rememberNavController()

                // 2. Definición de rutas
                NavHost(
                    navController = navController,
                    startDestination = "register_screen" // Pantalla inicial
                ) {
                    // Ruta Registro
                    composable("register_screen") {
                        RegisterScreen(navController)
                    }

                    // Ruta Home (La pantalla a la que vas al tener éxito)
                    composable("home_screen") {
                        ScanQrScreen()
                    }
                }
            }
        }
    }
}
