package com.example.reciclapp.network

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File

class TokenManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
    }

    private val masterKey = MasterKey.Builder(context.applicationContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private var sharedPreferences: SharedPreferences

    init {

        val appContext = context.applicationContext

        sharedPreferences = try {
            createSharedPreferences(appContext)
        } catch (e: Exception) {
            e.printStackTrace()

            deleteSharedPreferences(appContext)

            createSharedPreferences(appContext)
        }
    }

    private fun createSharedPreferences(context : Context): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @SuppressLint("SdCardPath")
    private fun deleteSharedPreferences(context : Context) {
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