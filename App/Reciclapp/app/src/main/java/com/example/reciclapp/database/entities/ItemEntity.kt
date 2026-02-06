package com.example.reciclapp.database.entities

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item_table")
data class ItemEntity (
    @PrimaryKey(true)
    val itemId: Long = 0,
    val itemName: String,
    val cost: Int,
    val isFeatured: Boolean = false,
    val icon: ImageVector,
    val color: Color,
)
