package com.freeshop.billing.data.db.dao

import androidx.room.*
import com.freeshop.billing.data.db.entity.Shop
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(shop: Shop): Long

    @Update
    suspend fun update(shop: Shop)

    @Query("SELECT * FROM shops WHERE id = :id")
    suspend fun getById(id: Long): Shop?

    @Query("SELECT * FROM shops WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): Shop?

    @Query("SELECT * FROM shops WHERE id = :id")
    fun observeById(id: Long): Flow<Shop?>
}
