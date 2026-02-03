package com.example.reciclapp.database.dao

import androidx.room.*
import com.example.reciclapp.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Retorna un Flow: Si la DB cambia, la UI se actualiza sola
    @Query("SELECT * FROM user_table LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    // Usamos REPLACE para que si bajamos datos nuevos de la API, se sobrescriba
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Para descontar puntos (ejecutar dentro de una transacción)
    @Query("UPDATE user_table SET pointsBalance = :newBalance WHERE id = :userId")
    suspend fun updatePoints(userId: Int, newBalance: Int)
}