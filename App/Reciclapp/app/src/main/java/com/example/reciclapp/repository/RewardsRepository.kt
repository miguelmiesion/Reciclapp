package com.example.reciclapp.repository

import com.example.reciclapp.network.ReciclappApi

class RewardsRepository(
    private val api: ReciclappApi
) {
    suspend fun getUserBalance(): Result<Int> {
        return try {
            val response = api.getUserPoints()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.points)
            } else {
                Result.failure(Exception("Error de API"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}