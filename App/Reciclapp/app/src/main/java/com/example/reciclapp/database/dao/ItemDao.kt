package com.example.reciclapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.reciclapp.database.entities.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Query("SELECT * FROM item_table ORDER BY isFeatured DESC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Query("SELECT cost FROM item_table WHERE itemId= :itemId")
    fun getItemCost(itemId: Long): Flow<Int?>

}