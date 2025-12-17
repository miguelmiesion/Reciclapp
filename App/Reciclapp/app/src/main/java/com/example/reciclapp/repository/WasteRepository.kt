package com.example.reciclapp.repository

import com.example.reciclapp.network.*

class WasteRepository(private val api: ReciclappApi) : BaseApiResponse() {

    suspend fun claimWaste(idWaste: String): NetworkResult<Void> {
        val request = WasteClaimRequest(idWaste = idWaste)
        return safeApiCall { api.claimWaste(request) }
    }
}