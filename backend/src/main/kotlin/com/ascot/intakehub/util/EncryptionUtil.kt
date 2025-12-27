package com.ascot.intakehub.util

import com.macasaet.fernet.Key
import com.macasaet.fernet.StringValidator
import com.macasaet.fernet.Token
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class EncryptionUtil(@Value("\${security.fernet.key}") val fernetKey: String) {

    private val key: Key by lazy {
        if (fernetKey.isBlank()) {
            throw IllegalStateException("Encryption key not set")
        }
        Key(fernetKey)
    }

    fun encrypt(data: String): String {
        val token = Token.generate(key, data)
        return token.serialise()
    }

    fun decrypt(encryptedData: String): String {
        val token = Token.fromString(encryptedData)
        return token.validateAndDecrypt(key, object : StringValidator {
            override fun validate(temporal: Instant, payload: String): String {
                // No expiration for stored credentials
                return payload
            }
        })
    }

    // Helper to generate a new key if needed
    fun generateKey(): String {
        return Key.generate().serialise()
    }
}
