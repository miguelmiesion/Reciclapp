package com.example.reciclapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.repository.RewardsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Modelo de UI para mostrar en el historial (el "Join" resuelto)
data class HistoryUiModel(
    val purchaseId: Long,
    val itemName: String,
    val iconName: String,
    val colorHex: String,
    val cost: Int,
    val date: Long
)

data class ProfileUiState(
    val userId: Int = 0,
    val username: String = "",
    val points: Int = 0,
    val purchaseHistory: List<HistoryUiModel> = emptyList(),
    val isLoading: Boolean = false
)

class ProfileViewModel(
    private val repository: RewardsRepository
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        repository.userBalance,
        repository.allItems,
        repository.purchaseHistory,
        repository.currentUser
    ) { balance, items, purchases, userEntity ->
        val historyUi = purchases.mapNotNull { purchase ->
            val itemDetails = items.find { it.itemId == purchase.itemId }
            itemDetails?.let { item ->
                HistoryUiModel(
                    purchaseId = purchase.transactionId,
                    itemName = item.itemName,
                    iconName = item.icon,
                    colorHex = item.color,
                    cost = item.cost,
                    date = purchase.timestamp
                )
            }
        }.sortedByDescending { it.date }

        ProfileUiState(
            userId = userEntity?.id ?: 0,
            username = userEntity?.username ?: "Cargando...",
            points = balance,
            purchaseHistory = historyUi,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState(isLoading = true)
    )

    fun refreshUserData() {
        viewModelScope.launch {
            repository.refreshUserBalance()
        }
    }
}