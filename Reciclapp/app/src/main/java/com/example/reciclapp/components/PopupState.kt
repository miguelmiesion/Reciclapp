package com.example.reciclapp.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

// Reutilizamos tu clase de resultados
sealed class ScanResult {
    data class Success(val message : String) : ScanResult()
    data class Error(val message: String) : ScanResult()
}

// Clase que controla el estado
class PopupController {
    var currentResult by mutableStateOf<ScanResult?>(null)
        private set

    fun showSuccess(message: String) {
        currentResult = ScanResult.Success(message)
    }

    fun showError(message: String) {
        currentResult = ScanResult.Error(message)
    }

    fun dismiss() {
        currentResult = null
    }
}

// Este es el "Cable" invisible para acceder al controller desde cualquier lado
val LocalPopupState = staticCompositionLocalOf<PopupController> {
    error("No se ha provisto el PopupController")
}