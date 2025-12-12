package com.example.reciclapp.network

import retrofit2.Response
import retrofit2.http.*

interface ReciclappApi {

    @POST("api/login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    /*
    Sugerencias de Gemini para otros endpoints:

    @POST("api/signup/")
    suspend fun signup(@Body request: SignupRequest): Response<Void>


    @POST("api/logout/")
    suspend fun logout(): Response<Void>
    // Note: Logout usually sends the Refresh Token to be blacklisted, 
    // check your Swagger docs if this needs a body like RefreshTokenRequest.

    @POST("api/token/refresh/")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<LoginResponse>

    // --- WASTE OPERATIONS ---

    @POST("api/residuos/reclamar/")
    suspend fun claimWaste(@Body request: WasteClaimRequest): Response<Void>

    @GET("api/residuos/")
    suspend fun getGlobalStats(): Response<WasteStatsResponse>

    @GET("api/residuos/{id_usuario}")
    suspend fun getUserStats(@Path("id_usuario") userId: Int): Response<WasteStatsResponse>

    // --- RANKING ---

    @GET("api/ranking/")
    suspend fun getRanking(
        @Query("tipo_residuo") filter: String? = null
    ): Response<List<RankingUserResponse>>

    @GET("api/ranking/posicion/")
    suspend fun getUserPosition(
        @Query("id_usuario") userId: Int,
        @Query("tipo_residuo") filter: String? = null
    ): Response<Map<String, Int>> // Or use RankingPositionResponse if the JSON matches exactly

    // --- STATIONS ---

    @GET("api/estaciones/")
    suspend fun getStations(): Response<List<StationResponse>>

    @GET("api/estaciones/{id_estacion}/")
    suspend fun getStationDetail(@Path("id_estacion") stationId: Int): Response<StationResponse>
     */
}

