package com.example.reciclapp.network

import org.json.JSONObject
import retrofit2.Response

abstract class BaseApiResponse {

    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
        try {
            val response = apiCall()

            if (response.isSuccessful) {
                val body = response.body()
                return NetworkResult.Success(body)
            }

            val errorBody = response.errorBody()?.string()

            val errorMessage = try {
                val jsonObject = JSONObject(errorBody ?: "")

                val message = jsonObject.names()[0] as String
                if (jsonObject.has(message)) {
                    jsonObject.getString(message)
                        .replace("[", "")
                        .replace("]", "")
                        .replace("\"", "")
                } else {
                    "Error del servidor: ${response.code()}"
                }

            } catch (e: Exception) {
                "Error desconocido (${response.code()})"
            }

            return NetworkResult.Error(errorMessage)

        } catch (e: Exception) {
            return NetworkResult.Error(e.message ?: "Error de conexión. Verifica tu internet.")
        }
    }
}