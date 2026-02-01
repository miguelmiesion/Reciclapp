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
    val currentUserId: Int? = null,
    val currentUserName: String = "",
    val currentFilter: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class RankingViewModel(
    private val api: ReciclappApi
) : ViewModel() {

    var uiState by mutableStateOf(RankingUiState())
        private set

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                // 1. Perfil
                val profileResponse = api.getUserProfile()
                if (profileResponse.isSuccessful && profileResponse.body() != null) {
                    val user = profileResponse.body()!!
                    val nombreLimpio = user.username.trim()

                    uiState = uiState.copy(
                        currentUserId = user.id,
                        currentUserName = nombreLimpio
                    )

                    // 2. Ranking
                    fetchRankings(user.id, uiState.currentFilter, nombreLimpio)
                } else {
                    uiState = uiState.copy(isLoading = false, error = "Fallo perfil")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun updateFilter(newFilter: String?) {
        if (uiState.currentFilter != newFilter) {
            uiState = uiState.copy(currentFilter = newFilter)
            uiState.currentUserId?.let { userId ->
                viewModelScope.launch { fetchRankings(userId, newFilter, uiState.currentUserName) }
            }
        }
    }

    private suspend fun fetchRankings(userId: Int, filter: String?, miNombre: String) {
        try {
            val topDeferred = viewModelScope.async { api.getTopRanking(filter) }
            val posDeferred = viewModelScope.async { api.getUserPosition(userId, filter) }

            val topResponse = topDeferred.await()
            val posResponse = posDeferred.await()

            if (topResponse.isSuccessful && posResponse.isSuccessful) {
                val rawList = topResponse.body() ?: emptyList()

                // 1. ORDENAMOS Z-A (Visualmente correcto)
                val listaOrdenada = rawList.sortedWith(
                    compareByDescending<RankingEntry> { it.totalPoints }
                        .thenByDescending { it.username }
                )

                // 2. BUSCAMOS TU INDICE (La lógica que arregló el 7 vs 4)
                val index = listaOrdenada.indexOfFirst {
                    it.username.trim().equals(miNombre, ignoreCase = true)
                }

                // 3. SOBRESCRIBIMOS POSICIÓN
                val posFinal = if (index != -1) (index + 1) else posResponse.body()?.posicion

                uiState = uiState.copy(
                    isLoading = false,
                    topUsers = listaOrdenada,
                    userPosition = posFinal
                )
            } else {
                uiState = uiState.copy(isLoading = false, error = "Error API")
            }
        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false, error = e.message)
        }
    }
}

class RankingViewModelFactory(private val api: ReciclappApi) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RankingViewModel(api) as T
    }
}