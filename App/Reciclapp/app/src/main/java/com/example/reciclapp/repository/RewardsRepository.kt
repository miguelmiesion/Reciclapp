package com.example.reciclapp.repository

import androidx.room.withTransaction
import com.example.reciclapp.database.ReciclappDatabase
import com.example.reciclapp.database.entities.ItemEntity
import com.example.reciclapp.database.entities.PurchaseEntity
import com.example.reciclapp.database.entities.UserEntity
import com.example.reciclapp.network.ReciclappApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class RewardsRepository(
    private val api: ReciclappApi,
    private val db: ReciclappDatabase
) {
    private val userDao = db.userDao()
    private val transactionDao = db.transactionDao()

    private val itemDao = db.itemDao()

    val allItems: Flow<List<ItemEntity>> = itemDao.getAllItems()

    val userBalance: Flow<Int> = userDao.getUser()
        .combine(transactionDao.getTotalSpentAmount()) { user, spentAmount ->

            val basePoints = user?.pointsBalance ?: 0
            val totalSpent = spentAmount ?: 0

            if (basePoints - totalSpent < 0) 0 else (basePoints - totalSpent)
        }


    suspend fun refreshUserBalance(): Result<Unit> {
        return try {
            val response = api.getUserPoints()
            if (response.isSuccessful && response.body() != null) {

                val remotePoints = response.body()!!.puntos
                userDao.insertUser(UserEntity(id = 1, username = "Usuario", pointsBalance = remotePoints))
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error API"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun redeemItem(itemId: Long): Result<Unit> {
        return try {
            val itemCost = itemDao.getItemCost(itemId).first()
                ?: return Result.failure(Exception("El ítem no existe"))

            val currentBalance = userBalance.first()

            if (currentBalance < itemCost) {
                return Result.failure(Exception("Saldo insuficiente"))
            }

            val purchase = PurchaseEntity(
                itemId = itemId,
                timestamp = System.currentTimeMillis()
            )

            transactionDao.insertPurchase(purchase)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
}