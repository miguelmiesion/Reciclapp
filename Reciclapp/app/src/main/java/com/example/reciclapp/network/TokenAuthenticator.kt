package com.example.reciclapp.network

import android.content.Context
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val context: Context,
    private val tokenManager: TokenManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = tokenManager.getRefreshToken() ?: return null

        synchronized(this) {

            val newAccessToken = tokenManager.getAccessToken()

            if (response.request.header("Authorization") != "Bearer $newAccessToken") {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            }

            val api = RetrofitClient.buildApiForRefresh()

            try {
                val refreshResponse = api.refreshToken(RefreshRequest(refreshToken)).execute()

                if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                    val newTokens = refreshResponse.body()!!

                    tokenManager.saveTokens(newTokens.access, refreshToken)

                    return response.request.newBuilder()
                        .header("Authorization", "Bearer ${newTokens.access}")
                        .build()
                } else {
                    tokenManager.clearTokens()
                    return null
                }

            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }
}