package com.example.reciclapp.database

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.Color
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
import com.example.reciclapp.ui.theme.Gold
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserEntity::class, PurchaseEntity::class, ItemEntity::class],
    version = 1,
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
            // Patrón Double-Check Locking
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReciclappDatabase::class.java,
                    "reciclapp_database"
                )
                    // Estrategia de fallback destructiva (solo para desarrollo inicial)
                    // .fallbackToDestructiveMigration()
                    .addCallback(ReciclappDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    class ReciclappDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Insertar datos cuando la DB se crea por primera vez
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.itemDao())
                }
            }
        }

        suspend fun populateDatabase(itemDao: ItemDao) {
            val initialItems = listOf(
                ItemEntity(0, "Totebag Reciclapp", 500, true, "shopping_bag", "#444444"),
                ItemEntity(1, "Decoración Oro", 200, false, "premium", "#FFD700"),
                ItemEntity(2, "Badge Reciclador", 150, false, "eco", "#4CAF50"),
                ItemEntity(3, "Icono Premium", 300, false, "premium", "#673AB7"),
                ItemEntity(4, "Nickname de color", 100, false, "shopping_bag", "#E91E63")
            )

            itemDao.insertAll(initialItems)
        }
    }
}