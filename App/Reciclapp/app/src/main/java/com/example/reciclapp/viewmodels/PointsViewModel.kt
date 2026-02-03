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
            // Transformamos el Int del repo al objeto de UI
            PointsUiState(
                userBalance = calculatedBalance,
                isLoading = false // Si recibimos dato, ya no estamos cargando
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Mantiene el estado 5s si la app rota
            initialValue = PointsUiState(isLoading = true) // Estado inicial mientras carga la DB
        )

    init {
        // Al iniciar, pedimos datos frescos a la API para actualizar la "Base"
        refreshData()
    }

    // 2. SINCRONIZACIÓN (Solo pedimos actualizar, no manejamos el resultado UI aquí)
    fun update() {
        refreshData()
    }

    private fun refreshData() {
        viewModelScope.launch {
            repository.refreshUserBalance()
            // No necesitamos hacer _uiState.update { ... }
            // Al guardarse en Room, el bloque 'uiState' de arriba se dispara solo.
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
            // Si es success, no hacemos nada.
            // Room detecta la inserción -> Recalcula la resta -> Actualiza el UI State solo.
        }
    }
}