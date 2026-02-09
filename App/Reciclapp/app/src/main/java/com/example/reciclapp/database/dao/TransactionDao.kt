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

    @Query(
        """
    SELECT SUM(item_table.cost) 
    FROM purchase_history 
    INNER JOIN item_table ON purchase_history.itemId = item_table.itemId
"""
    )
    fun getTotalSpentAmount(): Flow<Int?>

    @Query("SELECT itemId FROM purchase_history")
    fun getPurchasedItemIds(): Flow<List<Long>>

}