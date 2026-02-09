package com.example.reciclapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.network.RankingEntry
import com.example.reciclapp.repository.RankingRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private val repository: RankingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            getUserProfileAndRankings()
        }
    }

    private suspend fun getUserProfileAndRankings() {
        _uiState.update { it.copy(isLoading = true) }
        when (val result = repository.getUserProfile()) {
            is NetworkResult.Success -> {
                val user = result.data
                if (user != null) {
                    val trimmedUsername = user.username.trim()

                    _uiState.update {
                        it.copy(
                            currentUserId = user.id,
                            currentUserName = trimmedUsername
                        )
                    }

                    fetchRankings(user.id, _uiState.value.currentFilter, trimmedUsername)
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Datos de usuario vacíos")
                    }
                }
            }

            is NetworkResult.Error -> {
                _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun updateFilter(newFilter: String?) {

        if (_uiState.value.currentFilter != newFilter) {

            _uiState.update { it.copy(currentFilter = newFilter) }

            _uiState.value.currentUserId?.let { userId ->
                viewModelScope.launch {
                    fetchRankings(userId, newFilter, _uiState.value.currentUserName)
                }
            }
        }
    }

    fun update() {
        viewModelScope.launch {
            getUserProfileAndRankings()
        }
    }

    private suspend fun fetchRankings(userId: Int, filter: String?, userName: String) {
        val topDeferred = viewModelScope.async { repository.getTopRanking(filter) }
        val posDeferred = viewModelScope.async { repository.getUserPosition(userId, filter) }

        val topResult = topDeferred.await()
        val posResult = posDeferred.await()

        if (topResult is NetworkResult.Success && posResult is NetworkResult.Success) {
            val rawList = topResult.data ?: emptyList()
            val userPosData = posResult.data

            val sortedList = rawList.sortedWith(
                compareByDescending<RankingEntry> { it.totalPoints }
                    .thenByDescending { it.username }
            )

            val index = sortedList.indexOfFirst {
                it.username.trim().equals(userName, ignoreCase = true)
            }

            val posFinal = if (index != -1) (index + 1) else userPosData?.position

            _uiState.update {
                it.copy(
                    isLoading = false,
                    topUsers = sortedList,
                    userPosition = posFinal
                )
            }
        } else {
            val errorMsg = if (topResult is NetworkResult.Error) topResult.message
            else (posResult as? NetworkResult.Error)?.message ?: "Error desconocido"

            _uiState.update {
                it.copy(isLoading = false, error = errorMsg)
            }
        }
    }
}

class RankingViewModelFactory(private val repository: RankingRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RankingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RankingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}