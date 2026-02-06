package com.example.reciclapp.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchase_history",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["itemId"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemId")]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val transactionId: Long = 0,
    val itemId: Long,
    val timestamp: Long = System.currentTimeMillis()
)