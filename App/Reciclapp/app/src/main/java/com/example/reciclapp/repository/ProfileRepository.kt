package com.example.reciclapp.repository

import com.example.reciclapp.database.dao.TransactionDao
import com.example.reciclapp.database.entities.PurchaseEntity
import com.example.reciclapp.network.BaseApiResponse
import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.network.ReciclappApi
import com.example.reciclapp.network.UserProfileResponse
import kotlinx.coroutines.flow.Flow

class ProfileRepository(
    private val api: ReciclappApi,
    private val transactionDao: TransactionDao
) : BaseApiResponse() {

    val purchases: Flow<List<PurchaseEntity>> = transactionDao.getAllPurchases()

    suspend fun getUserProfile(): NetworkResult<UserProfileResponse> {
        return safeApiCall { api.getUserProfile() }
    }

}