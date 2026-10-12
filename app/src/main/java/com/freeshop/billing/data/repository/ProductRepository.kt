package com.freeshop.billing.data.repository

import com.freeshop.billing.data.db.dao.ProductDao
import com.freeshop.billing.data.db.entity.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val dao: ProductDao) {
    fun observeAll(shopId: Long): Flow<List<Product>> = dao.observeAll(shopId)
    suspend fun getAll(shopId: Long): List<Product> = dao.getAll(shopId)
    suspend fun search(shopId: Long, q: String): List<Product> = dao.search(shopId, q)
    suspend fun insert(product: Product): Long = dao.insert(product)
    suspend fun delete(product: Product) = dao.delete(product)
}
