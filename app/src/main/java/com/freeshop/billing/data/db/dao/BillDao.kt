package com.freeshop.billing.data.db.dao

import androidx.room.*
import com.freeshop.billing.data.db.entity.Bill
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(bill: Bill): Long

    @Update
    suspend fun update(bill: Bill)

    @Delete
    suspend fun delete(bill: Bill)

    @Query("SELECT * FROM bills WHERE shopId = :shopId ORDER BY createdAt DESC")
    fun observeAll(shopId: Long): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE shopId = :shopId AND (billNo LIKE '%' || :q || '%' OR customerName LIKE '%' || :q || '%') ORDER BY createdAt DESC")
    fun search(shopId: Long, q: String): Flow<List<Bill>>

    @Query("SELECT COUNT(*) FROM bills WHERE shopId = :shopId AND billNo = :billNo")
    suspend fun countByBillNo(shopId: Long, billNo: String): Int

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getById(id: Long): Bill?
}
