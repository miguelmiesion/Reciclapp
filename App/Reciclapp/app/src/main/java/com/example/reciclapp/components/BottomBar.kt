package com.example.reciclapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.reciclapp.ui.theme.Primary

@Composable
fun ReciclappBottomBar(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .height(70.dp)
            .clip(RoundedCornerShape(35.dp))
            .background(Color(0xFFA5D6A7)) // El verde claro de tu diseño
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- BOTÓN DE RANKING---
            Icon(
                imageVector = Icons.Default.Tag,
                contentDescription = "Ranking",
                tint = Color(0xFF424242),
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        // Navega a la pantalla de ranking
                        navController.navigate("ranking_screen") {
                            // Opcional: Para no apilar pantallas infinitamente
                            popUpTo("home_screen") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
            )
            Icon(Icons.Outlined.CalendarToday, "Calendario", tint = Color(0xFF424242), modifier = Modifier.size(28.dp))

            // Botón Central (Home / Escanear)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primary)
                    .clickable {
                        // Evita recargar si ya estás en home
                        if (navController.currentDestination?.route != "home_screen") {
                            navController.navigate("home_screen")
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.CameraAlt, "Escanear", tint = Color.White, modifier = Modifier.size(32.dp))
            }

            // --- BOTÓN DE PREMIOS ---
            Icon(Icons.Outlined.CardGiftcard, "Premios", tint = Color(0xFF424242), modifier = Modifier.size(28.dp)
                .clickable {
                    // Navega a la pantalla de ranking
                    navController.navigate("store_screen") {
                        // Opcional: Para no apilar pantallas infinitamente
                        popUpTo("home_screen") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            // Perfil (sin acción definida aún)
            Icon(Icons.Outlined.Person, "Perfil", tint = Color(0xFF424242), modifier = Modifier.size(28.dp)                .clickable {
                // Navega a la pantalla de ranking
                navController.navigate("profile_screen") {
                    // Opcional: Para no apilar pantallas infinitamente
                    popUpTo("home_screen") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        }
    }
}