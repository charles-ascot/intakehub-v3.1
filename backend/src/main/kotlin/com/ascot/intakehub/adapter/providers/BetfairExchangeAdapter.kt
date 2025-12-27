package com.ascot.intakehub.adapter.providers

import com.ascot.intakehub.adapter.HealthStatus
import com.ascot.intakehub.adapter.ProviderAdapter
import com.ascot.intakehub.service.BetfairSessionService
import com.ascot.intakehub.service.CredentialService
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate
import java.util.UUID

@Component
class BetfairExchangeAdapter(
    private val webClient: WebClient,
    private val credentialService: CredentialService,
    private val sessionService: BetfairSessionService
) : ProviderAdapter() {

    override val name = "Betfair Exchange"
    private val providerId = UUID.nameUUIDFromBytes(name.toByteArray())
    private val rpcUrl = "https://api.betfair.com/exchange/betting/json-rpc/v1"

    override suspend fun fetchRacecards(date: LocalDate): List<Any> {
        val token = sessionService.getSessionToken(providerId).block()!!
        val creds = credentialService.getCredentials(providerId).block()!!
        val appKey = creds["app_key"]!!

        val request = JsonRpcRequest(
            method = "SportsAPING/v1.0/listMarketCatalogue",
            params = mapOf(
                "filter" to mapOf(
                    "eventTypeIds" to listOf("7"), // Horse Racing
                    "marketStartTime" to mapOf(
                        "from" to date.atStartOfDay().toString(),
                        "to" to date.plusDays(1).atStartOfDay().toString()
                    )
                ),
                "maxResults" to 100,
                "marketProjection" to listOf("RUNNER_METADATA", "MARKET_START_TIME")
            )
        )

        return webClient.post()
            .uri(rpcUrl)
            .header("X-Application", appKey)
            .header("X-Authentication", token)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(JsonRpcResponse::class.java)
            .block()
            ?.result as? List<Any> ?: emptyList()
    }

    override suspend fun fetchLiveOdds(raceId: String): List<Any> {
        val token = sessionService.getSessionToken(providerId).block()!!
        val creds = credentialService.getCredentials(providerId).block()!!
        val appKey = creds["app_key"]!!

        val request = JsonRpcRequest(
            method = "SportsAPING/v1.0/listMarketBook",
            params = mapOf(
                "marketIds" to listOf(raceId),
                "priceProjection" to mapOf(
                    "priceData" to listOf("EX_BEST_OFFERS")
                )
            )
        )

        return webClient.post()
            .uri(rpcUrl)
            .header("X-Application", appKey)
            .header("X-Authentication", token)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(JsonRpcResponse::class.java)
            .block()
            ?.result as? List<Any> ?: emptyList()
    }

    override suspend fun fetchResults(raceId: String): Any {
         // Betfair doesn't keep historical results in Exchange API reliably without specific query
         // Would typically listMarketBook for CLOSED markets.
         return emptyMap<String, Any>()
    }

    override suspend fun healthCheck(): HealthStatus {
        return try {
            val start = System.currentTimeMillis()
            sessionService.getSessionToken(providerId).block() // Validates connectivity + auth
            HealthStatus(true, System.currentTimeMillis() - start)
        } catch (e: Exception) {
            HealthStatus(false, 0, e.message)
        }
    }
}

data class JsonRpcRequest(
    val jsonrpc: String = "2.0",
    val method: String,
    val params: Any,
    val id: Int = 1
)

data class JsonRpcResponse(
    val jsonrpc: String,
    val result: Any?,
    val error: Any?,
    val id: Int
)
