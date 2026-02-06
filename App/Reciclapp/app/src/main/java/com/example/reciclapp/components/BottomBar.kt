package com.example.reciclapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.reciclapp.ui.theme.Primary

@Composable
fun ReciclappBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .height(70.dp)
            .clip(RoundedCornerShape(35.dp))
            .background(Color(0xFFA5D6A7)) // Background color from your design
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                icon = Icons.Outlined.CameraAlt,
                description = "Escanear",
                isSelected = currentRoute == "home_screen",
                onClick = {
                    if (currentRoute != "home_screen") {
                        navController.navigate("home_screen") {
                            popUpTo("home_screen") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )

            BottomBarItem(
                icon = Icons.Outlined.Map,
                description = "Mapa",
                isSelected = currentRoute == "maps_screen",
                onClick = {
                    if (currentRoute != "maps_screen") {
                        navController.navigate("maps_screen") {
                            // Mantiene el estado y evita duplicados
                            popUpTo("home_screen") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )

            BottomBarItem(
                icon = Icons.Default.Tag,
                description = "Ranking",
                isSelected = currentRoute == "ranking_screen",
                onClick = {
                    if (currentRoute != "ranking_screen") {
                        navController.navigate("ranking_screen") {
                            popUpTo("home_screen") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )

            BottomBarItem(
                icon = Icons.Outlined.CardGiftcard,
                description = "Premios",
                isSelected = currentRoute == "store_screen",
                onClick = {
                    if (currentRoute != "store_screen") {
                        navController.navigate("store_screen") {
                            popUpTo("home_screen") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )


        }
    }
}

@Composable
fun BottomBarItem(
    icon: ImageVector,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Primary else Color.Transparent) // Highlight background if selected
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (isSelected) Color.White else Color(0xFF424242), // White icon if selected, Dark Gray if not
            modifier = Modifier.size(28.dp)
        )
    }
}