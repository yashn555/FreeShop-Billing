package com.freeshop.billing.data.repository

import com.freeshop.billing.data.db.dao.CustomerDao
import com.freeshop.billing.data.db.entity.Customer
import kotlinx.coroutines.flow.Flow

class CustomerRepository(private val dao: CustomerDao) {
    fun observeAll(shopId: Long): Flow<List<Customer>> = dao.observeAll(shopId)
    suspend fun getAll(shopId: Long): List<Customer> = dao.getAll(shopId)
    suspend fun search(shopId: Long, q: String): List<Customer> = dao.search(shopId, q)
    suspend fun insert(customer: Customer): Long = dao.insert(customer)
    suspend fun delete(customer: Customer) = dao.delete(customer)
}
