package com.example.reciclapp.network

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val access: String,
    val refresh: String
)

data class SignupRequest(
    val username: String,
    val password: String
)

data class LogoutRequest(
    val refresh: String
)

data class LogoutResponse(
    val message: String
)

data class WasteClaimRequest(
    @SerializedName("id_residuo")
    val idWaste: String
)

data class RefreshRequest(
    val refresh: String
)

data class ApiError(
    val error: String? = null,
    val detail: String? = null
)

data class UserProfileResponse(
    val id: Int,
    val username: String
)

data class RankingEntry(
    val username: String,

    @SerializedName("total_puntos")
    val totalPoints: Int
)

data class UserPositionResponse(
    @SerializedName("puntos")
    val position: Int,
)

data class UserPointsResponse(
    @SerializedName("puntos")
    val points: Int,
)


data class Station(
    val id: Int,
    @SerializedName("nombre")
    val name: String,
    @SerializedName("latitud")
    val latitude: Double,
    @SerializedName("longitud")
    val longitude: Double
)