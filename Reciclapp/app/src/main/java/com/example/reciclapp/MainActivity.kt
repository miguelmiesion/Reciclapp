package com.example.reciclapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.reciclapp.ui.theme.ReciclappTheme

// Importa tus vistas
import com.example.reciclapp.views.LoginScreen
import com.example.reciclapp.views.RegisterScreen
import com.example.reciclapp.views.ScanQrScreen

import com.example.reciclapp.components.PopupController
import com.example.reciclapp.components.LocalPopupState
import com.example.reciclapp.components.ScanResult
import com.example.reciclapp.components.ResultPopup
import com.example.reciclapp.network.TokenManager
import isTokenValid

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val tokenManager = TokenManager(applicationContext)

        val startDestination = if (isTokenValid(tokenManager.getAccessToken()) || isTokenValid(tokenManager.getRefreshToken())) {
            "home_screen"
        } else {
            "login_screen"
        }

        setContent {
            ReciclappTheme {
                // 1. Inicializamos el Controlador del Popup (El "mando a distancia")
                val popupController = remember { PopupController() }

                // 2. Setup del NavController
                val navController = rememberNavController()

                // 3. Proveemos el controlador a toda la jerarquía de la app
                CompositionLocalProvider(LocalPopupState provides popupController) {

                    // Usamos un Box para apilar capas (Capas Z)
                    Box(modifier = Modifier.fillMaxSize()) {

                        // --- CAPA DE FONDO: La Navegación ---
                        NavHost(
                            navController = navController,
                            startDestination = startDestination
                        ) {
                            // Ruta Registro
                            composable("register_screen") {
                                RegisterScreen(navController)
                            }

                            // Ruta Login
                            composable("login_screen") {
                                LoginScreen(navController)
                            }

                            // Ruta Home (Escáner QR)
                            composable("home_screen") {
                                ScanQrScreen()
                            }
                        }

                        // --- CAPA SUPERIOR: El Popup Global ---
                        // Esta parte "observa" si hay un resultado. Si lo hay, dibuja el popup encima de todo.
                        popupController.currentResult?.let { result ->
                            ResultPopup(
                                result = result,
                                onDismiss = {
                                    popupController.dismiss() // Cierra el popup al aceptar
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}