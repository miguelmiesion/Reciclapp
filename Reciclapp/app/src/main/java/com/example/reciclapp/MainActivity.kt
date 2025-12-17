package com.example.reciclapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.reciclapp.ui.theme.ReciclappTheme
import com.example.reciclapp.views.LoginScreen
import com.example.reciclapp.views.RegisterScreen
import com.example.reciclapp.views.ScanQrScreen
import com.example.reciclapp.components.PopupController
import com.example.reciclapp.components.LocalPopupState
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
                val popupController = remember { PopupController() }

                val navController = rememberNavController()

                CompositionLocalProvider(LocalPopupState provides popupController) {

                    Box(modifier = Modifier.fillMaxSize()) {

                        NavHost(
                            navController = navController,
                            startDestination = startDestination
                        ) {
                            composable("register_screen") {
                                RegisterScreen(navController)
                            }

                            composable("login_screen") {
                                LoginScreen(navController)
                            }

                            composable("home_screen") {
                                ScanQrScreen()
                            }
                        }

                        popupController.currentResult?.let { result ->
                            ResultPopup(
                                result = result,
                                onDismiss = {
                                    popupController.dismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}