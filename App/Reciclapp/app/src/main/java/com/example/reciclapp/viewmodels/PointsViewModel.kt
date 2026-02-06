package com.example.reciclapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.repository.RewardsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PointsUiState(
    val userBalance: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class PointsViewModel(
    private val repository: RewardsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PointsUiState())
    val uiState: StateFlow<PointsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userBalance.collect { points ->
                _uiState.update { it.copy(userBalance = points) }
            }
        }
        refreshData()
    }

    // 2. SINCRONIZACIÓN
    fun update() {
        refreshData()
    }

    private fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.refreshUserBalance()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    // 3. ACCIÓN DE COMPRA
    fun redeemItem(itemPrice: Int, itemName: String) {
        viewModelScope.launch {
            // Intentamos registrar la compra en la BD local
            val result = repository.redeemItem(itemPrice, itemName)

            result.onFailure { e ->
                // Opcional: Podrías exponer este error en un Snackbar
                println("Error al canjear: ${e.message}")
            }
        }
    }
}