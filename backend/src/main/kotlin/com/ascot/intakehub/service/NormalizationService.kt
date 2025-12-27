package com.ascot.intakehub.service

import com.ascot.intakehub.model.NormalizedData
import com.ascot.intakehub.model.RawData
import com.ascot.intakehub.repository.NormalizedDataRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class NormalizationService(
    private val normalizedDataRepository: NormalizedDataRepository,
    private val objectMapper: ObjectMapper
) {

    fun normalizeAndSave(rawData: RawData): Mono<NormalizedData> {
        // In a real system, this would have a strategy pattern for each provider's payload structure.
        // For MVP, we'll wrap the raw payload in a "Common" envelope.
        
        val normalizedPayload = mapOf(
            "original_checksum" to rawData.checksum,
            "data" to objectMapper.readTree(rawData.rawPayload), // Ensure valid JSON
            "normalized_schema_version" to "1.0"
        )
        
        val entityId = rawData.externalId ?: UUID.randomUUID().toString()
        val entityType = rawData.dataType

        val normalized = NormalizedData(
            sourceProviderId = rawData.providerId,
            entityId = entityId,
            entityType = entityType,
            normalizedPayload = objectMapper.writeValueAsString(normalizedPayload)
        )
        // Upsert logic would go here (or rely on DB unique key constraints handling via R2DBC manual queries or error handling)
        // R2DBC save is new or update.
        // Assuming ID is null, it tries insert. If unique constraint violation, we should handle it.
        // Simplified:
        return normalizedDataRepository.save(normalized)
            .onErrorResume { e ->
                // Log and ignore duplicate or update existing?
                println("Normalization Duplicate/Error: ${e.message}")
                Mono.empty()
            }
    }
}
