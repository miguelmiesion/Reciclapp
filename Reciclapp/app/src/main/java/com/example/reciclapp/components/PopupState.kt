package com.example.reciclapp.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

sealed class ScanResult {
    data class Success(val message : String) : ScanResult()
    data class Error(val message: String) : ScanResult()
}

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

val LocalPopupState = staticCompositionLocalOf<PopupController> {
    error("No se ha provisto el PopupController")
}