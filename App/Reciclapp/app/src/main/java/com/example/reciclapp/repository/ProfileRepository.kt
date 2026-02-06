package com.example.reciclapp.repository

import com.example.reciclapp.network.BaseApiResponse
import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.network.ReciclappApi
import com.example.reciclapp.network.UserProfileResponse

class ProfileRepository(val api: ReciclappApi) : BaseApiResponse() {

    suspend fun getUserProfile(): NetworkResult<UserProfileResponse> {
        return safeApiCall { api.getUserProfile() }
    }

}