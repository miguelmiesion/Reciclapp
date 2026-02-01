package com.example.reciclapp.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.network.RankingEntry
import com.example.reciclapp.network.ReciclappApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class RankingUiState(
    val topUsers: List<RankingEntry> = emptyList(),
    val userPosition: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class RankingViewModel(
    private val api: ReciclappApi,
    private val currentUserId: Int
) : ViewModel() {

    var uiState by mutableStateOf(RankingUiState())
        private set

    init {
        loadRanking()
    }

    private fun loadRanking() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val topDeferred = async { api.getTopRanking() }
                val posDeferred = async { api.getUserPosition(currentUserId) }

                val topResponse = topDeferred.await()
                val posResponse = posDeferred.await()

                if (topResponse.isSuccessful) {
                    val listaUsuarios = topResponse.body() ?: emptyList()

                    uiState = uiState.copy(
                        isLoading = false,
                        topUsers = listaUsuarios, // Asignación directa
                        userPosition = posResponse.body()?.posicion
                    )
                } else {
                    uiState = uiState.copy(isLoading = false, error = "Error API: ${topResponse.code()}")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
                e.printStackTrace()
            }
        }
    }
}

// Factory para poder pasar parámetros al ViewModel
class RankingViewModelFactory(private val api: ReciclappApi, private val userId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RankingViewModel(api, userId) as T
    }
}