package com.example.reciclapp.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.reciclapp.database.ReciclappDatabase
import com.example.reciclapp.database.entities.ItemEntity
import com.example.reciclapp.database.entities.PurchaseEntity
import com.example.reciclapp.database.entities.UserEntity
import com.example.reciclapp.network.BaseApiResponse
import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.network.ReciclappApi
import com.example.reciclapp.network.TokenManager
import com.example.reciclapp.network.UserProfileResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toCollection

class RewardsRepository(
    private val api: ReciclappApi,
    private val db: ReciclappDatabase,
) : BaseApiResponse() {
    private val profileRepository = ProfileRepository(api, db)

    private val userDao = db.userDao()
    private val transactionDao = db.transactionDao()
    private val itemDao = db.itemDao()
    val allItems: Flow<List<ItemEntity>> = itemDao.getAllItems()

    val purchaseHistory: Flow<List<PurchaseEntity>> = transactionDao.getAllPurchases()

    val ownedItemIds: Flow<List<Long>> = transactionDao.getPurchasedItemIds()

    val user: Flow<UserEntity?> = profileRepository.user

    suspend fun redeemItem(itemId: Long): Result<Unit> {

        if(!profileRepository.ensureUserSynchronized()) return Result.failure(Exception("Falló la api"))

        return try {
            db.withTransaction {
                val localUser = user.firstOrNull() ?: throw Exception("Error inesperado")

                val itemCost = itemDao.getItemCost(itemId).firstOrNull()
                    ?: throw Exception("El ítem no existe")
                val currentBalance = localUser.pointsBalance
                if (currentBalance < itemCost) {
                    throw Exception("Saldo insuficiente")
                }

                val newBalance = localUser.pointsBalance - itemCost

                userDao.upsertUser(UserEntity(
                    username = localUser.username,
                    pointsBalance = newBalance
                ))

                val purchase = PurchaseEntity(
                    itemId = itemId,
                    username = localUser.username,
                    timestamp = System.currentTimeMillis()
                )
                transactionDao.insertPurchase(purchase)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}