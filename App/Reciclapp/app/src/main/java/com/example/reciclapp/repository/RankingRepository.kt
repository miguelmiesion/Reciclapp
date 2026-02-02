package com.example.reciclapp.repository

import com.example.reciclapp.network.BaseApiResponse
import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.network.RankingEntry
import com.example.reciclapp.network.ReciclappApi
import com.example.reciclapp.network.UserProfileResponse
import com.example.reciclapp.network.UserPositionResponse

class RankingRepository(private val api: ReciclappApi) : BaseApiResponse() {

    suspend fun getUserProfile(): NetworkResult<UserProfileResponse> {
        return safeApiCall { api.getUserProfile() }
    }

    suspend fun getTopRanking(filter: String?): NetworkResult<List<RankingEntry>> {
        return safeApiCall { api.getTopRanking(tipoResiduo = filter) }
    }

    suspend fun getUserPosition(userId: Int, filter: String?): NetworkResult<UserPositionResponse> {
        return safeApiCall { api.getUserPosition(userId = userId, tipoResiduo = filter) }
    }
}