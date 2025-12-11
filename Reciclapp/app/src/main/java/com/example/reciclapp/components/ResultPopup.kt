package com.example.reciclapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Si definiste ReciclappGreen en otro lado, impórtalo. Si no, úsalo aquí:
val PopupGreen = Color(0xFF2E7D32)
val PopupRed = Color(0xFFD32F2F)

@Composable
fun ResultPopup(
    result: ScanResult, // Esta clase viene de tu archivo PopupState.kt
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss, // Se cierra si tocas fuera

        // --- BOTÓN INFERIOR ---
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (result is ScanResult.Success) PopupGreen else PopupRed
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aceptar", color = Color.White)
            }
        },

        // --- TÍTULO CON ICONO ---
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (result is ScanResult.Success) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (result is ScanResult.Success) PopupGreen else PopupRed,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (result is ScanResult.Success) "¡Bien hecho!" else "Ups, hubo un problema",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },

        // --- TEXTO DEL MENSAJE ---
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (result is ScanResult.Success) {
                    Text("¡Reciclaje exitoso!", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "+ ${result.points} Puntos",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PopupGreen
                    )
                } else if (result is ScanResult.Error) {
                    Text(
                        text = result.message,
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}