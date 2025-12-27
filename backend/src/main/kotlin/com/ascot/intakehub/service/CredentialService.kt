package com.ascot.intakehub.service

import com.ascot.intakehub.util.EncryptionUtil
import org.springframework.stereotype.Service
import org.springframework.data.annotation.Id
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.data.relational.core.mapping.Table
import reactor.core.publisher.Mono
import java.util.UUID

@Table("credentials")
data class Credential(
    @Id val id: UUID? = null,
    val providerId: UUID,
    val encryptedData: ByteArray,
    val lastRotatedAt: java.time.Instant? = null
)

interface CredentialRepository : ReactiveCrudRepository<Credential, UUID> {
    fun findByProviderId(providerId: UUID): Mono<Credential>
}

@Service
class CredentialService(
    private val encryptionUtil: EncryptionUtil,
    private val credentialRepository: CredentialRepository
) {

    fun saveCredentials(providerId: UUID, data: Map<String, String>): Mono<Credential> {
        // Serialize map to JSON then encrypt
        val json = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper().writeValueAsString(data)
        val encrypted = encryptionUtil.encrypt(json)
        
        return credentialRepository.findByProviderId(providerId)
            .flatMap { existing ->
                credentialRepository.save(existing.copy(
                    encryptedData = encrypted.toByteArray(), // Fernet produces String, we store as bytea or string? DB has BYTEA.
                    // Wait, Fernet token is URL-safe string. Storing as bytes is fine.
                    lastRotatedAt = java.time.Instant.now()
                ))
            }
            .switchIfEmpty(
                credentialRepository.save(Credential(
                    providerId = providerId,
                    encryptedData = encrypted.toByteArray(),
                    lastRotatedAt = java.time.Instant.now()
                ))
            )
    }

    fun getCredentials(providerId: UUID): Mono<Map<String, String>> {
        return credentialRepository.findByProviderId(providerId)
            .map { cred ->
                val encrypted = String(cred.encryptedData)
                val json = encryptionUtil.decrypt(encrypted)
                val typeRef = object : com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {}
                com.fasterxml.jackson.module.kotlin.jacksonObjectMapper().readValue(json, typeRef)
            }
    }
}
