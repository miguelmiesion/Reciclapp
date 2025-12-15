package com.example.reciclapp.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val context: Context,
    private val tokenManager: TokenManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = tokenManager.getRefreshToken()

        if (refreshToken == null) {
            return null
        }

        // 2. Sincronizamos para evitar múltiples llamadas de refresh al mismo tiempo
        synchronized(this) {

            // Verificamos si otro hilo ya actualizó el token mientras esperábamos
            val newAccessToken = tokenManager.getAccessToken()

            // Si el token en la petición fallida es DIFERENTE al que tenemos guardado ahora,
            // significa que alguien más ya lo refrescó. Solo reintentamos con el nuevo.
            if (response.request.header("Authorization") != "Bearer $newAccessToken") {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            }

            // 3. Si nadie lo ha refrescado, nos toca a nosotros llamar a la API
            // TRUCO: Creamos una instancia "tonta" de la API solo para esto
            // para evitar dependencias circulares con RetrofitClient.
            val api = RetrofitClient.buildApiForRefresh(context)

            try {
                Log.e("TOKEN REFRESH", "TOKEN REFRESH")
                val refreshResponse = api.refreshToken(RefreshRequest(refreshToken)).execute()

                if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                    Log.e("TOKEN REFRESH SUCCESSFUL", "TOKEN REFRESH SUCCESSFUL")
                    val newTokens = refreshResponse.body()!!

                    Log.e("TOKEN REFRESH SUCCESSFUL", "$newTokens")
                    // 4. ¡ÉXITO! Guardamos los nuevos tokens
                    tokenManager.saveTokens(newTokens.access, refreshToken)

                    Log.e("TOKEN REFRESH SUCCESSFUL", "${tokenManager.getAccessToken()}")
                    // 5. Retornamos la petición original con el header nuevo.
                    // OkHttp la reintentará automáticamente.
                    return response.request.newBuilder()
                        .header("Authorization", "Bearer ${newTokens.access}")
                        .build()
                } else {
                    tokenManager.clearTokens()
                    return null // Esto detiene los reintentos
                }

            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }
}