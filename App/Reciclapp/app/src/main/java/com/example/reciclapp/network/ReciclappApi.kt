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

    @GET("api/ranking/")
    suspend fun getTopRanking(
        @Query("tipo_residuo") tipoResiduo: String? = null
    ): Response<List<RankingEntry>>

    @GET("api/ranking/posicion/")
    suspend fun getUserPosition(
        @Query("id_usuario") userId: Int,
        @Query("tipo_residuo") tipoResiduo: String? = null
    ): Response<PositionResponse>
}

