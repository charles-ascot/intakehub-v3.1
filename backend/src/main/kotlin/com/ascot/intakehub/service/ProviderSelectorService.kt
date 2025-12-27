package com.ascot.intakehub.service

import com.ascot.intakehub.adapter.ProviderAdapter
import com.ascot.intakehub.model.Provider
import com.ascot.intakehub.repository.ProviderRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ProviderSelectorService(
    private val providerRepository: ProviderRepository,
    private val adapters: List<ProviderAdapter>
) {
    // Map of adapter name to instance
    private val adapterMap by lazy {
        adapters.associateBy { it.name }
    }

    /*
     * Selects available providers in order of priority.
     * Real-world logic would check health status here too.
     */
    fun getPrioritizedProviders(): Flux<ProviderAdapter> {
        return providerRepository.findByEnabledTrueOrderByPriorityAsc()
            .flatMap { provider ->
                val adapter = adapterMap[provider.name]
                if (adapter != null) {
                    Mono.just(adapter)
                } else {
                    Mono.empty()
                }
            }
    }
    
    fun getAdapter(name: String): ProviderAdapter? {
        return adapterMap[name]
    }
}
