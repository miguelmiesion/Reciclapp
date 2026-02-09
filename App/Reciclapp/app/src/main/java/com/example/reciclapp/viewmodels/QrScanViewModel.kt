package com.example.reciclapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.repository.WasteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

data class QrScanUiState(
    val message: String? = null,
    val error: Boolean = false,
    val isLoading: Boolean = false,
    val success: Boolean = false,
)

class QrScanViewModel(private val repository: WasteRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(QrScanUiState())
    val uiState = _uiState.asStateFlow()

    fun claimWaste(resultString: String) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = false,
                    success = false,
                    message = null
                )
            }
            try {
                val jsonQr = JSONObject(resultString)
                val wasteId = jsonQr.getString("ID Residuo")
                val points = jsonQr.optInt("Puntos", 0)

                when (val result = repository.claimWaste(wasteId)) {
                    is NetworkResult.Success -> {
                        _uiState.update {
                            it.copy(
                                success = true,
                                isLoading = false,
                                message = "Sumaste $points puntos!"
                            )
                        }
                    }

                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = true,
                                message = result.message
                                    ?: "Error en el servidor, intente nuevamente."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CLAIMWASTE", e.message ?: "ERROR SIN MENSAJE")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = true,
                        message = "Error en el servidor, intente nuevamente."
                    )
                }
            }
        }
    }

    fun resetState() {
        _uiState.update { QrScanUiState() }
    }
}


class QrScanViewModelFactory(private val repository: WasteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QrScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QrScanViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}