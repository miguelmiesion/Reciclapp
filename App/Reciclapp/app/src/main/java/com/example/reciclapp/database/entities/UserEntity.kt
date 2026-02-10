package com.example.reciclapp.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey val username: String,
    val pointsBalance: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)