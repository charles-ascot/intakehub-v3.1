package com.ascot.intakehub.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

enum class AuthType {
    API_KEY_HEADER, API_KEY_QUERY, HTTP_BASIC, OAUTH2, CUSTOM, CERT_AUTH
}

@Table("providers")
data class Provider(
    @Id val id: UUID? = null,
    val name: String,
    val baseUrl: String,
    val authType: AuthType,
    val rateLimitRequests: Int?,
    val rateLimitWindowSeconds: Int?,
    val cloudflareTunnel: Boolean = false,
    val enabled: Boolean = true,
    val priority: Int = 0,
    val createdAt: Instant = Instant.now()
)

@Table("raw_data")
data class RawData(
    @Id val id: UUID? = null,
    val providerId: UUID,
    val dataType: String, // "RACECARD", "ODDS", "RESULT"
    val externalId: String?,
    val rawPayload: String, // JSON stored as string for R2DBC
    val fetchedAt: Instant = Instant.now(),
    val checksum: String?
)

@Table("normalized_data")
data class NormalizedData(
    @Id val id: UUID? = null,
    val sourceProviderId: UUID?,
    val entityId: String,
    val entityType: String,
    val normalizedPayload: String,
    val normalizedAt: Instant = Instant.now()
)
