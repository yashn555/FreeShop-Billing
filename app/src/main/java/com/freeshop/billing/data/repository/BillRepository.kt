package com.freeshop.billing.data.repository

import com.freeshop.billing.data.db.dao.BillDao
import com.freeshop.billing.data.db.entity.Bill
import kotlinx.coroutines.flow.Flow

class BillRepository(private val dao: BillDao) {
    fun observeAll(shopId: Long): Flow<List<Bill>> = dao.observeAll(shopId)
    fun search(shopId: Long, q: String): Flow<List<Bill>> = dao.search(shopId, q)
    suspend fun insert(bill: Bill): Long = dao.insert(bill)
    suspend fun update(bill: Bill) = dao.update(bill)
    suspend fun delete(bill: Bill) = dao.delete(bill)
    suspend fun isBillNoUnique(shopId: Long, billNo: String): Boolean = dao.countByBillNo(shopId, billNo) == 0
    suspend fun getById(id: Long): Bill? = dao.getById(id)
}
