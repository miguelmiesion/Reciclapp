// RetrofitClient.kt
package com.example.reciclapp.network

import android.content.Context
import com.example.reciclapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Helper variable to keep the instance alive
    private var apiInstance: ReciclappApi? = null

    fun getApi(context: Context): ReciclappApi {
        // Return existing instance if available
        if (apiInstance != null) return apiInstance!!

        // 1. Setup Interceptors
        val tokenManager = TokenManager(context)
        val authInterceptor = AuthInterceptor(tokenManager)

        // Optional: Logging to see JSON in Logcat
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // 2. Build OkHttp Client
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Auto-adds "Bearer token" header [cite: 50]
            .addInterceptor(logging)
            .build()

        // 3. Build Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL) // e.g. "https://...app.github.dev/" or "http://192.168.0.142:8000/"
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiInstance = retrofit.create(ReciclappApi::class.java)
        return apiInstance!!
    }
}