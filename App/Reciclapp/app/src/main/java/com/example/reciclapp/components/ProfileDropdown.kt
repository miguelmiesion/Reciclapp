package com.example.reciclapp.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciclapp.MainActivity
import com.example.reciclapp.network.RetrofitClient
import com.example.reciclapp.network.TokenManager
import com.example.reciclapp.repository.AuthRepository
import com.example.reciclapp.viewmodels.AuthViewModel
import com.example.reciclapp.viewmodels.AuthViewModelFactory

import com.example.reciclapp.ui.theme.ErrorColor
import com.example.reciclapp.ui.theme.TextColor

@Composable
fun ProfileDropdown(navController: NavController, tokenManager: TokenManager, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val authRepository = remember { AuthRepository(RetrofitClient.getApi(context)) }
    val viewModel : AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authRepository, tokenManager)
    )

    val uiState by viewModel.uiState.collectAsState()
    val popupController = LocalPopupState.current

    val currentRoute = navController.currentDestination?.route

    LaunchedEffect(uiState.isLogoutSuccess) {
        if (uiState.isLogoutSuccess) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)

            navController.navigate("login_screen") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            popupController.showError(uiState.error ?: "Error desconocido")
        }
    }

    Box(
        modifier = modifier
            .wrapContentSize(Alignment.TopEnd)
    ) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Perfil",
                tint = Color(0xFF424242),
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0))
                    .padding(4.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (currentRoute != "profile_screen") {
                DropdownMenuItem(
                    text = { Text("Perfil", color = TextColor) },
                    onClick = {
                        expanded = false
                        navController.navigate("profile_screen")
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Cerrar sesión", color = ErrorColor) },
                onClick = {
                    expanded = false
                    viewModel.logout()
                }
            )
        }
    }
}
