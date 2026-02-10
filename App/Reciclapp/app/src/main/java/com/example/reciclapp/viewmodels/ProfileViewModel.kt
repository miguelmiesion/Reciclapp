package com.example.reciclapp.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.database.ReciclappDatabase
import com.example.reciclapp.network.RetrofitClient
import com.example.reciclapp.repository.RewardsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
        repository.allItems,
        repository.purchaseHistory,
        repository.user
    ) { items, purchases, user ->
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
            username = user?.username ?: "Usuario",
            points = user?.pointsBalance ?: 0,
            purchaseHistory = historyUi,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState(isLoading = true)
    )
}

class ProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val db = ReciclappDatabase.getDatabase(context, kotlinx.coroutines.MainScope())
            val api = RetrofitClient.getApi(context)
            return ProfileViewModel(RewardsRepository(api, db)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}