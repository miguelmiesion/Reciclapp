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
        if(ensureUserSynchronized()) {
            val userProfile = getUserProfile()
            val username = userProfile.data?.username

            if (username != null) {
                val flow = userDao.getUser(username)
                emitAll(flow)
            } else {
                emit(null)
            }
        }
        else{
            emit(null)
        }
    }

    suspend fun ensureUserSynchronized(): Boolean {
        val profileResult = getUserProfile()

        if (profileResult is NetworkResult.Success) {
            val username = profileResult.data?.username ?: return false

            val localUser = userDao.getUser(username).firstOrNull()

            if (localUser == null) {
                val pointsResult = safeApiCall { api.getUserPoints() }
                if (pointsResult is NetworkResult.Success) {
                    userDao.upsertUser(UserEntity(
                        username = username,
                        pointsBalance = pointsResult.data?.points ?: 0
                    ))
                    return true
                }
            } else {
                return true
            }
        }
        return false
    }
    suspend fun getUserProfile(): NetworkResult<UserProfileResponse> {
        return safeApiCall { api.getUserProfile() }
    }



}