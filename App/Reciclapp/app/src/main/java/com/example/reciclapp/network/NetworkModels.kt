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

