package com.example.reciclapp.viewmodels

import RewardsRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PointsUiState(
    val userBalance: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class PointsViewModel(
    private val repository: RewardsRepository
) : ViewModel() {
    val uiState: StateFlow<PointsUiState> = repository.userBalance
        .map { calculatedBalance ->
            PointsUiState(
                userBalance = calculatedBalance,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PointsUiState(isLoading = true)
        )

    init {
        refreshData()
    }

    fun update() {
        refreshData()
    }

    private fun refreshData() {
        viewModelScope.launch {
            repository.refreshUserBalance()
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