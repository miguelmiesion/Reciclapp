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

    private val profileRepository = ProfileRepository(api)

    private val userDao = db.userDao()

    val user: Flow<UserEntity?> = flow {
        val userProfile = profileRepository.getUserProfile()
        val username = userProfile.data?.username

        Log.i("USER FLOW", username.toString())

        if (username != null) {
            val flow = userDao.getUser(username)
            Log.i("USER FLOW", flow.firstOrNull().toString())
            if (flow.firstOrNull() == null) {
                val result = safeApiCall { api.getUserPoints() }
                when (result) {
                    is NetworkResult.Success -> {
                        if (result.data != null) {
                            userDao.upsertUser(UserEntity(
                                username = username,
                                pointsBalance = result.data.points
                            ))
                        } else {
                            emit(null)
                        }
                    }
                    is NetworkResult.Error -> {
                        emit(null)
                    }
                }
            }
            emitAll(flow)
        } else {
            emit(null)
        }
    }

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
                        Log.i("LOCAL USER", localUser.toString())
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