package com.ascot.intakehub.adapter.providers

import com.ascot.intakehub.adapter.HealthStatus
import com.ascot.intakehub.adapter.ProviderAdapter
import com.ascot.intakehub.service.CredentialService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate
import java.util.UUID

@Component
class SportradarAdapter(
    private val webClient: WebClient,
    private val credentialService: CredentialService
) : ProviderAdapter() {

    override val name = "Sportradar"
    private val providerId = UUID.nameUUIDFromBytes(name.toByteArray())
    private val baseUrl = "https://api.sportradar.com/horse-racing-v1" 

    override suspend fun fetchRacecards(date: LocalDate): List<Any> {
        val creds = credentialService.getCredentials(providerId).block() 
            ?: throw IllegalStateException("No credentials for $name")
        val apiKey = creds["api_key"]!!

        return webClient.get()
            .uri("$baseUrl/custom/schedule/${date}.json?api_key=$apiKey")
            .retrieve()
            .bodyToMono(Map::class.java)
            .map { it["sport_events"] as? List<Any> ?: emptyList() }
            .block() ?: emptyList()
    }

    override suspend fun fetchLiveOdds(raceId: String): List<Any> {
        // Typically PULL for Sportradar
        val creds = credentialService.getCredentials(providerId).block()!!
        val apiKey = creds["api_key"]!!
        
        return webClient.get()
            .uri("$baseUrl/custom/probabilities/$raceId.json?api_key=$apiKey")
            .retrieve()
            .bodyToMono(Map::class.java)
            .map { it["markets"] as? List<Any> ?: emptyList() }
            .block() ?: emptyList()
    }

    override suspend fun fetchResults(raceId: String): Any {
         val creds = credentialService.getCredentials(providerId).block()!!
         val apiKey = creds["api_key"]!!

         return webClient.get()
            .uri("$baseUrl/custom/summary/$raceId.json?api_key=$apiKey")
            .retrieve()
            .bodyToMono(Map::class.java)
            .block() ?: emptyMap<String, Any>()
    }

    override suspend fun healthCheck(): HealthStatus {
        return try {
            val start = System.currentTimeMillis()
            // Lightweight check
            HealthStatus(true, System.currentTimeMillis() - start)
        } catch (e: Exception) {
            HealthStatus(false, 0, e.message)
        }
    }
}
