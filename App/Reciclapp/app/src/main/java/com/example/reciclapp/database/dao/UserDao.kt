package com.example.reciclapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.reciclapp.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_table WHERE username = :username")
    fun getUser(username : String): Flow<UserEntity?>

    @Upsert
    suspend fun upsertUser(user: UserEntity)

    @Query("UPDATE user_table SET pointsBalance = :newBalance WHERE username = :username ")
    suspend fun updatePoints(username: String, newBalance: Int)

}