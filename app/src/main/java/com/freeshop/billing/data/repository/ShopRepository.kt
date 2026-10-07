package com.freeshop.billing.data.repository

import com.freeshop.billing.data.PasswordHasher
import com.freeshop.billing.data.db.dao.ShopDao
import com.freeshop.billing.data.db.entity.Shop
import kotlinx.coroutines.flow.Flow

class ShopRepository(private val dao: ShopDao) {

    suspend fun register(name: String, email: String, password: String): Long {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(password, salt)
        val shop = Shop(name = name, email = email, passwordHash = hash, passwordSalt = salt)
        return dao.insert(shop)
    }

    suspend fun login(email: String, password: String): Shop? {
        val shop = dao.getByEmail(email) ?: return null
        return if (PasswordHasher.verify(password, shop.passwordSalt, shop.passwordHash)) shop else null
    }

    suspend fun getById(id: Long): Shop? = dao.getById(id)

    suspend fun update(shop: Shop) = dao.update(shop)

    fun observe(id: Long): Flow<Shop?> = dao.observeById(id)
}
