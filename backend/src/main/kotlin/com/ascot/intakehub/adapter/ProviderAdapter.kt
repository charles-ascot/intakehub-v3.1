package com.ascot.intakehub.adapter

import java.time.LocalDate

abstract class ProviderAdapter {
    abstract val name: String
    
    // Core data fetching methods
    abstract suspend fun fetchRacecards(date: LocalDate): List<Any> // Will define models properly later
    abstract suspend fun fetchLiveOdds(raceId: String): List<Any>
    abstract suspend fun fetchResults(raceId: String): Any
    
    // Health check
    abstract suspend fun healthCheck(): HealthStatus
}

data class HealthStatus(
    val isHealthy: Boolean,
    val responseTimeMs: Long,
    val message: String? = null
)
