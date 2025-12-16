package com.example.reciclapp.network

import android.util.Log
import retrofit2.Response
import org.json.JSONObject

abstract class BaseApiResponse {

    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
        try {
            // 1. Ejecutamos la llamada
            val response = apiCall()

            // 2. Verificamos si es un código 2xx (Éxito)
            if (response.isSuccessful) {
                val body = response.body()

                // Si hay cuerpo, lo devolvemos.
                // Si es nulo (como en Void), devolvemos null pero como Success (importante para Signup/Logout)
                return NetworkResult.Success(body)
            }
            Log.e("ERROR: ", "${response.errorBody()}")

            // 3. Si no es exitosa (400, 401, 500...), parseamos el error
            val errorBody = response.errorBody()?.string()

            val errorMessage = try {
                // Intentamos leer tu formato de ApiError (error o detail)
                val jsonObject = JSONObject(errorBody ?: "")

                val message = jsonObject.names()[0] as String
                if(jsonObject.has(message)) {
                    jsonObject.getString(message)
                        .replace("[", "")
                        .replace("]", "")
                        .replace("\"", "")
                }

                else{
                    "Error del servidor: ${response.code()}"
                }

            } catch (e: Exception) {
                "Error desconocido (${response.code()})"
            }

            return NetworkResult.Error(errorMessage)

        } catch (e: Exception) {
            // 4. Errores de red (Sin internet, Timeout, DNS)
            return NetworkResult.Error(e.message ?: "Error de conexión. Verifica tu internet.")
        }
    }
}