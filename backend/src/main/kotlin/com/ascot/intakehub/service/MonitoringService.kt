package com.ascot.intakehub.service

import com.ascot.intakehub.service.ProviderSelectorService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class MonitoringService(
    private val providerSelectorService: ProviderSelectorService
    // Should inject HealthRepo to save history
) {

    private val latestStatus = ConcurrentHashMap<String, Boolean>()

    @Scheduled(fixedDelay = 60000)
    fun checkHealth() {
        providerSelectorService.getPrioritizedProviders()
            .flatMap { adapter -> // Flux
                kotlinx.coroutines.reactor.mono {
                    val health = adapter.healthCheck()
                    latestStatus[adapter.name] = health.isHealthy
                    println("Health Check [${adapter.name}]: ${if(health.isHealthy) "UP" else "DOWN"} (${health.responseTimeMs}ms)")
                    // Save to DB
                }
            }
            .subscribe()
    }

    fun getHealth(providerName: String): Boolean {
        return latestStatus[providerName] ?: false
    }
    
    fun getAllHealth(): Map<String, Boolean> {
        return latestStatus.toMap()
    }
}
