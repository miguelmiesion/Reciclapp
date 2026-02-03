package com.example.reciclapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.network.ReciclappApi
import com.example.reciclapp.repository.RewardsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            getUserBalance()
        }
    }

    private suspend fun getUserBalance() {
        _uiState.update { it.copy(isLoading = true) }
        val result = repository.getUserBalance()

        result.onSuccess { puntos ->
            _uiState.update { it.copy(userBalance = puntos) }
        }.onFailure { error ->
            _uiState.update { it.copy(error = error.message ?: "Error del servidor") }
        }

        _uiState.update { it.copy(isLoading = false) }
    }

    fun update() {
        viewModelScope.launch {
            getUserBalance()
        }
    }
}
