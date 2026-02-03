package com.example.reciclapp.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_history")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val transactionId: Long = 0,
    val itemId: Int,
    val itemName: String,
    val cost: Int,
    val timestamp: Long = System.currentTimeMillis()
)