package com.example.reciclapp.database.dao

import androidx.room.*
import com.example.reciclapp.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_table LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE user_table SET pointsBalance = :newBalance WHERE id = 1")
    suspend fun updatePoints(newBalance: Int)

}