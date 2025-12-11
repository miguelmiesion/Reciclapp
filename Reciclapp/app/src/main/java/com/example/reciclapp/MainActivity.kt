package com.example.reciclapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.reciclapp.views.RegisterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
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
                        HomeScreen()
                    }
                }
            }
        }
    }
}

// Pantalla temporal de Home para que no te de error la navegación
@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "¡Bienvenido al Home de Reciclapp!")
    }
}