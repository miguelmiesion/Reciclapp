package com.example.reciclapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.network.Station
import com.example.reciclapp.repository.MapsRepository
import com.example.reciclapp.repository.RankingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapsUiState (
    val stations : List<Station> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MapsViewModel(private val repository: MapsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MapsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            fetchStations()
        }
    }

    suspend fun fetchStations() {
        when(val result = repository.getStations()) {
            is NetworkResult.Success -> {
                _uiState.update { it.copy(stations = result.data ?: emptyList(), isLoading = false) }
            }
            is NetworkResult.Error -> {
                _uiState.update { it.copy(error = result.message ?: "Error desconocido", isLoading = false) }
            }
        }
    }
}

class MapsViewModelFactory(private val repository: MapsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}