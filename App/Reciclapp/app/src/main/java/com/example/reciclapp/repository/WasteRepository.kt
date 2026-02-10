package com.example.reciclapp.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.reciclapp.database.ReciclappDatabase
import com.example.reciclapp.database.entities.PurchaseEntity
import com.example.reciclapp.database.entities.UserEntity
import com.example.reciclapp.network.BaseApiResponse
import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.network.ReciclappApi
import com.example.reciclapp.network.WasteClaimRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class WasteRepository(private val api: ReciclappApi, private val db: ReciclappDatabase) : BaseApiResponse() {

    private val profileRepository = ProfileRepository(api, db)

    private val userDao = db.userDao()

    val user: Flow<UserEntity?> = profileRepository.user

    suspend fun claimWaste(idWaste: String, points: Int): NetworkResult<Void> {
        val request = WasteClaimRequest(idWaste = idWaste)
        val result = safeApiCall {
            api.claimWaste(request)
        }

        when (result) {
            is NetworkResult.Success -> {
                try {
                    db.withTransaction {
                        val localUser = user.firstOrNull() ?: throw Exception("No se encontró al usuario")
                        val newBalance = localUser.pointsBalance + points
                        userDao.upsertUser(UserEntity(
                            username = localUser.username,
                            pointsBalance = newBalance))
                    }
                    return NetworkResult.Success(data = null)
                } catch (e: Exception) {
                    return NetworkResult.Error(message = e.message ?: "Error inesperado en try")
                }
            }
            is NetworkResult.Error -> {
                return NetworkResult.Error(message = result.message ?: "Error inesperado en Error")
            }
        }
    }
}