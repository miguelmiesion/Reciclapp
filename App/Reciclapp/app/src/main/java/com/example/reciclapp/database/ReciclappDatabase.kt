package com.example.reciclapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.reciclapp.database.dao.ItemDao
import com.example.reciclapp.database.dao.TransactionDao
import com.example.reciclapp.database.dao.UserDao
import com.example.reciclapp.database.entities.UserEntity
import com.example.reciclapp.database.entities.PurchaseEntity
import com.example.reciclapp.database.entities.ItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserEntity::class, PurchaseEntity::class, ItemEntity::class],
    // CAMBIO 1: Subimos a versión 3 para borrar la lista vieja y poner la nueva
    version = 4,
    exportSchema = false
)
abstract class ReciclappDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: ReciclappDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ReciclappDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReciclappDatabase::class.java,
                    "reciclapp_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                scope.launch(Dispatchers.IO) {
                    populateDatabase(instance.itemDao())
                }

                instance
            }
        }

        suspend fun populateDatabase(itemDao: ItemDao) {
            val initialItems = listOf(
                ItemEntity(1, "Totebag Reciclapp", 500, true, "shopping_bag", "#444444"),
                ItemEntity(2, "Decoración Oro", 200, false, "oro", "#FFD700"),
                ItemEntity(3, "Badge Reciclador", 150, false, "eco", "#4CAF50"),
                ItemEntity(4, "Icono Premium", 300, false, "premium", "#673AB7"),
                ItemEntity(5, "Plantar un Árbol", 400, false, "tree", "#2E7D32")
            )

            try {
                itemDao.insertAll(initialItems)
            } catch (e: Exception) {
            }
        }
    }
}