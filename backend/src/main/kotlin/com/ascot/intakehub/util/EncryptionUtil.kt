package com.ascot.intakehub.util

import com.macasaet.fernet.Key
import com.macasaet.fernet.Token
import com.macasaet.fernet.Validator
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.nio.charset.StandardCharsets
import java.util.function.Function

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
        return token.validateAndDecrypt(key, object : Validator<String> {

            
            override fun getTransformer(): Function<ByteArray, String> {
                return Function { bytes -> String(bytes, StandardCharsets.UTF_8) }
            }
        })
    }

    // Helper to generate a new key if needed
    fun generateKey(): String {
        return Key.generateKey().serialise()
    }
}
