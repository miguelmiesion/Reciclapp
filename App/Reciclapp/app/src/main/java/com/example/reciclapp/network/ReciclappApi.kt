package com.example.reciclapp.network

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ReciclappApi {

    @POST("api/login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/signup/")
    suspend fun signup(@Body request: SignupRequest): Response<Void>

    @POST("api/residuo/reclamar/")
    suspend fun claimWaste(@Body request: WasteClaimRequest): Response<Void>

    @POST("api/logout/")
    suspend fun logout(@Body request: LogoutRequest): Response<LogoutResponse>

    @POST("api/token/refresh/")
    fun refreshToken(@Body request: RefreshRequest): Call<LoginResponse>

    @GET("api/ranking/")
    suspend fun getTopRanking(
        @Query("tipo_residuo") wasteType: String? = null
    ): Response<List<RankingEntry>>

    @GET("api/datos_usuario/")
    suspend fun getUserProfile(): Response<UserProfileResponse>

    @GET("api/ranking/posicion/")
    suspend fun getUserPosition(
        @Query("id_usuario") userId: Int,
        @Query("tipo_residuo") wasteType: String? = null
    ): Response<UserPositionResponse>

    @GET("api/puntos/")
    suspend fun getUserPoints(
        @Query("id_usuario") userId: Int? = null
    ): Response<UserPointsResponse>

    @GET("api/estaciones/")
    suspend fun getStations(): Response<List<Station>>

    @GET("api/estaciones/")
    suspend fun getStationById(
        @Query("id_estacion") stationId: Int
    ): Response<Station>
}

