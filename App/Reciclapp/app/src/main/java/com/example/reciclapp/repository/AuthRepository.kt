package com.example.reciclapp.repository

import com.example.reciclapp.network.BaseApiResponse
import com.example.reciclapp.network.LoginRequest
import com.example.reciclapp.network.LoginResponse
import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.network.ReciclappApi
import com.example.reciclapp.network.SignupRequest

class AuthRepository(private val api: ReciclappApi) : BaseApiResponse() {

    suspend fun login(request: LoginRequest): NetworkResult<LoginResponse> {
        return safeApiCall { api.login(request) }
    }

    suspend fun signup(request: SignupRequest): NetworkResult<Void> {
        return safeApiCall { api.signup(request) }
    }

    suspend fun logout(): NetworkResult<Void> {
        return safeApiCall { api.logout() }
    }
}