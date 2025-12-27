package com.ascot.intakehub.service

import com.ascot.intakehub.util.EncryptionUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class EncryptionUtilTest {

    private val key = com.macasaet.fernet.Key.generate().serialise()
    private val encryptionUtil = EncryptionUtil(key)

    @Test
    fun `should encrypt and decrypt correctly`() {
        val original = "super_secret_password"
        val encrypted = encryptionUtil.encrypt(original)
        
        assertNotEquals(original, encrypted)
        assertEquals(original, encryptionUtil.decrypt(encrypted))
    }
}
