package com.tayseer.app.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object SecurityHelper {
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hashPassword(password: String, salt: String): String {
        val combined = password + salt
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val computed = hashPassword(password, salt)
        return computed.equals(expectedHash, ignoreCase = true)
    }

    fun generateAccessCode(length: Int = 6): String {
        val random = SecureRandom()
        val digits = (1..length).map { random.nextInt(10) }
        return digits.joinToString("")
    }
}
