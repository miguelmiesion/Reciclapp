package com.example.reciclapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.reciclapp.database.entities.PurchaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertPurchase(purchase: PurchaseEntity)

    @Query("SELECT * FROM purchase_history ORDER BY timestamp DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Query("SELECT SUM(cost) FROM purchase_history")
    fun getTotalSpentAmount(): Flow<Int?>

}