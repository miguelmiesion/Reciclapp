package com.example.reciclapp.views

import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciclapp.components.LocalPopupState
import com.example.reciclapp.components.ReciclappBottomBar
import com.example.reciclapp.network.RetrofitClient
import com.example.reciclapp.network.TokenManager
import com.example.reciclapp.repository.AuthRepository
import com.example.reciclapp.viewmodels.AuthViewModel
import com.example.reciclapp.viewmodels.AuthViewModelFactory

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current

    val authRepository = remember { AuthRepository(RetrofitClient.getApi(context)) }
    val tokenManager = TokenManager(context)

    val viewModel : AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authRepository, tokenManager)
    )

    val uiState by viewModel.uiState.collectAsState()

    val popupController = LocalPopupState.current

    LaunchedEffect(uiState.isLogoutSuccess) {
        if (uiState.isLogoutSuccess) {
            navController.navigate("login_screen") {
                popUpTo("login_screen") { inclusive = true }
            }
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            popupController.showError(uiState.error ?: "Error desconocido")
        }
    }

    Scaffold(
        bottomBar = { ReciclappBottomBar(navController) },
    ) {
        Button(
            onClick = {viewModel.logout()}
        ) {
            Text(text = "Cerrar sesión")
        }
    }


}