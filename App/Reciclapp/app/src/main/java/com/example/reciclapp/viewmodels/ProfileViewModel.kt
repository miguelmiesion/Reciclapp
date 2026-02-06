package com.example.reciclapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.database.entities.PurchaseEntity
import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userPosition: Int? = null,
    val userPoints: Int? = null,
    val userId: Int? = null,
    val purchaseditems: List<PurchaseEntity>? = null,
    val userName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            getUserProfile()
        }
    }

    private suspend fun getUserProfile() {
        _uiState.update { it.copy(isLoading = true) }
        when (val result = repository.getUserProfile()) {
            is NetworkResult.Success -> {
                val user = result.data
                if (user != null) {
                    val trimmedName = user.username.trim()

                    _uiState.update {
                        it.copy(
                            userId = user.id,
                            userName = trimmedName,
                            isLoading = false
                        )
                    }
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

    fun update() {
        viewModelScope.launch {
            getUserProfile()
        }
    }

}

class ProfileViewModelFactory(private val repository: ProfileRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}