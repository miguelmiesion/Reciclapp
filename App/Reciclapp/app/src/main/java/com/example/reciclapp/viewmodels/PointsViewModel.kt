package com.example.reciclapp.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.database.ReciclappDatabase
import com.example.reciclapp.network.ReciclappApi
import com.example.reciclapp.network.RetrofitClient
import com.example.reciclapp.repository.RewardsRepository
import com.example.reciclapp.views.StoreItem
import com.example.reciclapp.views.toStoreItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
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
        repository.user,
        repository.allItems,
        repository.ownedItemIds,
        _isLoading,
        _error
    ) { user, entities, ownedIds, loading, err ->

        val uiItems = entities.map { entity ->
            entity.toStoreItem(isOwned = ownedIds.contains(entity.itemId))
        }

        PointsUiState(
            userBalance = user?.pointsBalance ?: 0,
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

class PointsViewModelFactory(private val context: Context, private val api: ReciclappApi) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PointsViewModel::class.java)) {

            val db = ReciclappDatabase.getDatabase(context, kotlinx.coroutines.MainScope())
            return PointsViewModel(RewardsRepository(api, db)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}