package com.example.reciclapp.repository

import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.network.ReciclappApi
import com.example.reciclapp.network.BaseApiResponse
import com.example.reciclapp.network.Station

class MapsRepository(private val api: ReciclappApi): BaseApiResponse() {
    suspend fun getStations() : NetworkResult<List<Station>> {
        return safeApiCall { api.getStations() }
    }

    suspend fun getStationById(stationId: Int) : NetworkResult<Station> {
        return safeApiCall { api.getStationById(stationId) }
    }
}
