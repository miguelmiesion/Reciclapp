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

        sharedPreferences = try {
            createSharedPreferences()
        } catch (e: Exception) {
            e.printStackTrace()

            deleteSharedPreferences()

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

            val packageName = context.packageName
            val prefFile = File("/data/data/$packageName/shared_prefs/secure_prefs.xml")
            if (prefFile.exists()) {
                prefFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


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