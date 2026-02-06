package com.example.reciclapp.repository

import com.example.reciclapp.database.ReciclappDatabase
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

    // LÓGICA DE ESTADO DERIVADO:
    // Balance Final = (Balance Base API) - (Total Gastado Localmente)
    val userBalance: Flow<Int> = userDao.getUser() // Flow A
        .combine(transactionDao.getTotalSpentAmount()) { user, spentAmount -> // Flow B

            val basePoints = user?.pointsBalance ?: 0
            val totalSpent = spentAmount ?: 0 // Si es null (0 compras), usamos 0

            // Retornamos el cálculo.
            // Esto asegura que nunca mostremos más puntos de los que realmente tiene disponible.
            if (basePoints - totalSpent < 0) 0 else (basePoints - totalSpent)
        }

    // Al refrescar desde la API, solo actualizamos el "Balance Base"
    suspend fun refreshUserBalance(): Result<Unit> {
        return try {
            val response = api.getUserPoints()
            if (response.isSuccessful && response.body() != null) {
                // Sobrescribimos el usuario. Esto disparará el combine de arriba automáticamente.
                val remotePoints = response.body()!!.points
                userDao.insertUser(UserEntity(id = 1, username = "Usuario", pointsBalance = remotePoints))
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error API"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun redeemItem(itemPrice: Int, itemName: String): Result<Unit> {
        return try {
            // Validamos contra el valor CALCULADO actual, no el de la DB cruda
            val currentBalance = userBalance.first()

            if (currentBalance < itemPrice) {
                return Result.failure(Exception("Saldo insuficiente"))
            }

            // Solo insertamos la compra.
            // NO actualizamos el UserEntity.
            // El Flow 'combine' detectará la nueva compra y restará el saldo en la UI solo.
            val purchase = PurchaseEntity(
                itemId = 0,
                itemName = itemName,
                cost = itemPrice
            )
            transactionDao.insertPurchase(purchase)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}