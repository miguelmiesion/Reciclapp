package com.example.reciclapp.network

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ReciclappApi {

    @POST("api/login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/signup/")
    suspend fun signup(@Body request: SignupRequest): Response<Void>

    @POST("api/residuo/reclamar/")
    suspend fun claimWaste(@Body request: WasteClaimRequest): Response<Void>

    @POST("api/logout/")
    suspend fun logout(): Response<Void>

    @POST("api/token/refresh/")
    fun refreshToken(@Body request: RefreshRequest): Call<LoginResponse>
}

