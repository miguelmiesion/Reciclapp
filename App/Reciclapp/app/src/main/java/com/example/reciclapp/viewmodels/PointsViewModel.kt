package com.example.reciclapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.database.entities.ItemEntity
import com.example.reciclapp.repository.RewardsRepository
import com.example.reciclapp.views.StoreItem
import com.example.reciclapp.views.toStoreItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


data class PointsUiState(
    val userBalance: Int = 0,
    val storeItems: List<StoreItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PointsViewModel(
    private val repository: RewardsRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PointsUiState> = combine(
        repository.userBalance,
        repository.allItems,
        repository.ownedItemIds,
        _isLoading,
        _error
    ) { balance, entities, ownedIds, loading, err ->

        val uiItems = entities.map { entity ->
            entity.toStoreItem(isOwned = ownedIds.contains(entity.itemId))
        }

        PointsUiState(
            userBalance = balance,
            storeItems = uiItems,
            isLoading = loading,
            error = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PointsUiState(isLoading = true)
    )

    init {
        refreshData()
    }

    fun update() = refreshData()

    private fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.refreshUserBalance()
            _error.value = result.exceptionOrNull()?.message
            _isLoading.value = false
        }
    }

    fun redeemItem(itemId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.redeemItem(itemId)

            result.onSuccess {
                refreshData()
                onSuccess()
            }.onFailure { e ->
                onError(e.message ?: "Error en el canje")
            }
            _isLoading.value = false
        }
    }
}