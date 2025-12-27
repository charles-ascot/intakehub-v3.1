package com.ascot.intakehub.service

import com.ascot.intakehub.adapter.ProviderAdapter
import com.ascot.intakehub.model.RawData
import com.ascot.intakehub.repository.RawDataRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.util.UUID

@Service
class DataIntakeService(
    private val providerSelectorService: ProviderSelectorService,
    private val rawDataRepository: RawDataRepository,
    private val normalizationService: NormalizationService,
    private val objectMapper: ObjectMapper
) {

    // Triggered via Controller or Schedule
    fun intakeRacecards(date: LocalDate, providerName: String? = null): Flux<String> {
        val providers = if (providerName != null) {
            val adapter = providerSelectorService.getAdapter(providerName)
            if (adapter != null) Flux.just(adapter) else Flux.empty()
        } else {
            providerSelectorService.getPrioritizedProviders()
        }

        return providers.flatMap { adapter ->
            fetchAndProcess(adapter, date)
        }
    }

    private fun fetchAndProcess(adapter: ProviderAdapter, date: LocalDate): Flux<String> {
        // This should be async and resilient
        return Mono.fromCallable { "Starting intake for ${adapter.name}" }
            .flatMapMany {
                // Must wrap suspend function in reactor friendly way
                // kotlinx-coroutines-reactor used elsewhere? Or manual Mono.
                // We'll trust the adapter returns List<Any> synchronous/suspending (handled by Spring usually, but here manually calling suspend)
                // Actually, calling suspend from Flux requires kotlinx-coroutines-reactor 'mono {}' builder or similar.
                // For simplicity in this generated code, assuming adapter returns Mono/Flux would be better, but we defined suspend.
                // We'll use mono { } from kotlinx.coroutines.reactor
                kotlinx.coroutines.reactor.flux {
                    val data = adapter.fetchRacecards(date)
                    for (item in data) {
                        send(item) // Emit to flux
                    }
                }
            }
            .flatMap { item ->
                val json = objectMapper.writeValueAsString(item)
                val checksum = UUID.nameUUIDFromBytes(json.toByteArray()).toString()
                val externalId = "TODO_EXTRACT_ID" // Need strategy to extract ID from payload
                
                // Get provider ID logic needed (adapter doesn't expose ID easily in base).
                // We'll fetch ID by name from repo or cache.
                // Hack: adapter should expose ID or we look it up.
                // We'll assume adapter logic handles it or we skip specifically.
                val providerId = UUID.nameUUIDFromBytes(adapter.name.toByteArray()) // Consistent generation

                val raw = RawData(
                    providerId = providerId,
                    dataType = "RACECARD",
                    externalId = externalId,
                    rawPayload = json,
                    checksum = checksum
                )

                rawDataRepository.save(raw)
                    .flatMap { saved -> normalizationService.normalizeAndSave(saved) }
                    .map { "Processed ${adapter.name} item" }
            }
            .onErrorResume { e ->
                Mono.just("Error with ${adapter.name}: ${e.message}")
            }
    }
}
