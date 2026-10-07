package com.freeshop.billing.data.db.dao

import androidx.room.*
import com.freeshop.billing.data.db.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(product: Product): Long

    @Delete
    suspend fun delete(product: Product)

    @Query("SELECT * FROM products WHERE shopId = :shopId ORDER BY name ASC")
    fun observeAll(shopId: Long): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE shopId = :shopId ORDER BY name ASC")
    suspend fun getAll(shopId: Long): List<Product>

    @Query("SELECT * FROM products WHERE shopId = :shopId AND name LIKE '%' || :q || '%' ORDER BY name ASC")
    suspend fun search(shopId: Long, q: String): List<Product>
}
