package com.example.reciclapp.network

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit
import java.io.File

class TokenManager(private val context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private var sharedPreferences: SharedPreferences

    init {
        // Intentamos crear la instancia segura
        sharedPreferences = try {
            createSharedPreferences()
        } catch (e: Exception) {
            e.printStackTrace()
            // SI FALLA: Borramos el archivo XML corrupto manualmente
            deleteSharedPreferences()
            // E intentamos crearla de nuevo limpia
            createSharedPreferences()
        }
    }

    private fun createSharedPreferences(): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @SuppressLint("SdCardPath")
    private fun deleteSharedPreferences() {
        try {
            // Truco para borrar SharedPreferences corruptas
            val packageName = context.packageName
            val prefFile = File("/data/data/$packageName/shared_prefs/secure_prefs.xml")
            if (prefFile.exists()) {
                prefFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ... (El resto de tus métodos saveTokens, getAccessToken, etc. siguen igual)
    fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit {
            putString("ACCESS_TOKEN", accessToken)
            putString("REFRESH_TOKEN", refreshToken)
        }
    }

    fun getAccessToken(): String? = sharedPreferences.getString("ACCESS_TOKEN", null)
    fun getRefreshToken(): String? = sharedPreferences.getString("REFRESH_TOKEN", null)
    fun clearTokens() = sharedPreferences.edit { clear() }
}