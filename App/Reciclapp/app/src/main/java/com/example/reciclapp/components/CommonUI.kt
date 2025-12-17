package com.example.reciclapp.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.reciclapp.ui.theme.DarkerPrimary

class CommonUI {
    @Composable
    fun ReciclappLogo(modifier: Modifier = Modifier) {
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = DarkerPrimary)) {
                    append("Recicl")
                }
                withStyle(style = SpanStyle(color = Color.Black)) {
                    append("app")
                }
            },
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = modifier
        )
    }
}