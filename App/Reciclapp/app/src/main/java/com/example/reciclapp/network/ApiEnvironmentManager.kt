package com.example.reciclapp.network

import com.example.reciclapp.BuildConfig

object ApiEnvironmentManager{

    @Volatile
    var currentBaseUrl: String = BuildConfig.API_URL

}