package com.freeshop.billing.data

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PasswordHasher {
    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun hash(password: String, salt: String): String {
        val input = "$salt:$password"
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun verify(password: String, salt: String, storedHash: String): Boolean =
        hash(password, salt) == storedHash
}
