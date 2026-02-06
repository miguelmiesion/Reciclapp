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

    fun redeemItem(itemPrice: Int, itemName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.redeemItem(itemPrice, itemName)

            result.onSuccess {
                refreshData() // Sincronizamos balance local
                onSuccess()
            }.onFailure { e ->
                // Aquí puedes mapear errores específicos, ej: "Créditos insuficientes"
                val errorMsg = e.message ?: "No se pudo realizar el canje"
                onError(errorMsg)
            }
        }
    }
}