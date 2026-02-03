package com.example.reciclapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.reciclapp.database.dao.TransactionDao
import com.example.reciclapp.database.dao.UserDao
import com.example.reciclapp.database.entities.UserEntity
import com.example.reciclapp.database.entities.PurchaseEntity

// 1. Definimos las entidades y la versión del esquema
@Database(
    entities = [UserEntity::class, PurchaseEntity::class],
    version = 1,
    exportSchema = false // En producción true para controlar migraciones
)
abstract class ReciclappDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: ReciclappDatabase? = null

        fun getDatabase(context: Context): ReciclappDatabase {
            // Patrón Double-Check Locking
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReciclappDatabase::class.java,
                    "reciclapp_database"
                )
                    // Estrategia de fallback destructiva (solo para desarrollo inicial)
                    // .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}