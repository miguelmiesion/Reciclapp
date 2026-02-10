package com.example.reciclapp.repository

import com.example.reciclapp.database.ReciclappDatabase
import com.example.reciclapp.database.entities.UserEntity
import com.example.reciclapp.network.BaseApiResponse
import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.network.ReciclappApi
import com.example.reciclapp.network.UserProfileResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class ProfileRepository(val api: ReciclappApi, val db: ReciclappDatabase) : BaseApiResponse() {
    private val userDao = db.userDao()

    val user: Flow<UserEntity?> = flow {
        val userProfile = getUserProfile()
        val username = userProfile.data?.username

        if (username != null) {
            val flow = userDao.getUser(username)
            val localUser = flow.firstOrNull()
            if (localUser == null) {
                val result = safeApiCall {
                    api.getUserPoints()
                }
                when (result) {
                    is NetworkResult.Success -> {
                        if (result.data != null) {
                            userDao.upsertUser(UserEntity(
                                username = username,
                                pointsBalance = result.data.points
                            ))
                            emitAll(flow)
                        } else {
                            emit(null)
                        }
                    }
                    is NetworkResult.Error -> {
                        emit(null)
                    }
                }
            } else {
                emitAll(flow)
            }
        } else {
            emit(null)
        }
    }
    suspend fun getUserProfile(): NetworkResult<UserProfileResponse> {
        return safeApiCall { api.getUserProfile() }
    }



}