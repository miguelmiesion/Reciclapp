package com.example.reciclapp.network

import android.content.Context
import com.example.reciclapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private var apiInstance: ReciclappApi? = null

    fun getApi(context: Context): ReciclappApi {
        if (apiInstance != null) return apiInstance!!

        val tokenManager = TokenManager(context)
        val authInterceptor = AuthInterceptor(tokenManager)

        val authenticator = TokenAuthenticator(context, tokenManager)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .authenticator(authenticator)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiInstance = retrofit.create(ReciclappApi::class.java)
        return apiInstance!!
    }

    fun buildApiForRefresh(): ReciclappApi {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReciclappApi::class.java)
    }
}