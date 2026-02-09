package com.example.reciclapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.network.LoginRequest
import com.example.reciclapp.network.LogoutRequest
import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.network.SignupRequest
import com.example.reciclapp.network.TokenManager
import com.example.reciclapp.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val isLogoutSuccess: Boolean = false,
    val isRegisterSuccess: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(
    private val repository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    var uiState = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val request = LoginRequest(username.lowercase(), password)

            when (val result = repository.login(request)) {
                is NetworkResult.Success -> {
                    if (result.data != null) {
                        tokenManager.saveTokens(
                            accessToken = result.data.access,
                            refreshToken = result.data.refresh
                        )
                        _uiState.update { it.copy(isLoginSuccess = true, isLoading = false) }
                    } else {
                        _uiState.update { it.copy(error = result.message, isLoading = false) }
                    }
                }

                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            error = result.message ?: "Error desconocido",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val request = SignupRequest(username, password)

            when (val result = repository.signup(request)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isRegisterSuccess = true, isLoading = false) }
                }

                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            error = result.message ?: "Error desconocido",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val request = LogoutRequest(tokenManager.getRefreshToken()!!)

            when (val result = repository.logout(request)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLogoutSuccess = true, isLoading = false) }
                }

                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            error = result.message ?: "Error de cierre de sesión",
                            isLoading = false
                        )
                    }
                }
            }
            tokenManager.clearTokens()
        }
    }

}

class AuthViewModelFactory(
    private val repository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}