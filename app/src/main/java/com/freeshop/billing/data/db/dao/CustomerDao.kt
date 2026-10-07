package com.freeshop.billing.data.db.dao

import androidx.room.*
import com.freeshop.billing.data.db.entity.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(customer: Customer): Long

    @Delete
    suspend fun delete(customer: Customer)

    @Query("SELECT * FROM customers WHERE shopId = :shopId ORDER BY name ASC")
    fun observeAll(shopId: Long): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE shopId = :shopId ORDER BY name ASC")
    suspend fun getAll(shopId: Long): List<Customer>

    @Query("SELECT * FROM customers WHERE shopId = :shopId AND name LIKE '%' || :q || '%' ORDER BY name ASC")
    suspend fun search(shopId: Long, q: String): List<Customer>
}
