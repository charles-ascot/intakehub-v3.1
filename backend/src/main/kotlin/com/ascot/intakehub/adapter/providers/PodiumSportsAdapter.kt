package com.ascot.intakehub.adapter.providers

import com.ascot.intakehub.adapter.HealthStatus
import com.ascot.intakehub.adapter.ProviderAdapter
import com.ascot.intakehub.service.CredentialService
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Component
class PodiumSportsAdapter(
    private val webClient: WebClient,
    private val credentialService: CredentialService
) : ProviderAdapter() {

    override val name = "Podium Sports"
    private val providerId = UUID.nameUUIDFromBytes(name.toByteArray())
    private val baseUrl = "https://api.podium-sports.com/v1" // Hypothetical

    override suspend fun fetchRacecards(date: LocalDate): List<Any> {
        val creds = credentialService.getCredentials(providerId).block() 
            ?: throw IllegalStateException("No credentials for $name")
        val apiKey = creds["api_key"] ?: throw IllegalArgumentException("Missing api_key")

        // Podium hypothetical structure
        return webClient.get()
            .uri("$baseUrl/fixtures/horse-racing?date=${date.format(DateTimeFormatter.ISO_DATE)}")
            .header("x-api-key", apiKey)
            .retrieve()
            .bodyToFlux(PodiumFixture::class.java)
            .collectList()
            .block() ?: emptyList()
    }

    override suspend fun fetchLiveOdds(raceId: String): List<Any> {
        // Typically PUSH, but implementing pull fallback
        return emptyList()
    }

    override suspend fun fetchResults(raceId: String): Any {
         val creds = credentialService.getCredentials(providerId).block()!!
         val apiKey = creds["api_key"]

         return webClient.get()
            .uri("$baseUrl/results/$raceId")
             .header("x-api-key", apiKey)
            .retrieve()
            .bodyToMono(Map::class.java)
            .block() ?: emptyMap<String, Any>()
    }

    override suspend fun healthCheck(): HealthStatus {
         // Check Podium status
         return try {
             HealthStatus(true, 10)
         } catch (e: Exception) {
             HealthStatus(false, 0, e.message)
         }
    }
}

data class PodiumFixture(
    val id: String,
    val name: String,
    val startTime: String
)
