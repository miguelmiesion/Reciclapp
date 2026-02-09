package com.example.reciclapp.utils

import android.media.MediaPlayer
import com.example.reciclapp.R

fun playSound(context: android.content.Context) {
    try {
        val mp = MediaPlayer.create(context, R.raw.neo_geo_coin)
        mp.start()
        mp.setOnCompletionListener { it.release() }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
