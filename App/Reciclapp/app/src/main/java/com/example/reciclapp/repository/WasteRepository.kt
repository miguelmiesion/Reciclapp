package com.example.reciclapp.repository

import com.example.reciclapp.network.BaseApiResponse
import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.network.ReciclappApi
import com.example.reciclapp.network.WasteClaimRequest

class WasteRepository(private val api: ReciclappApi) : BaseApiResponse() {

    suspend fun claimWaste(idWaste: String): NetworkResult<Void> {
        val request = WasteClaimRequest(idWaste = idWaste)
        return safeApiCall { api.claimWaste(request) }
    }
}