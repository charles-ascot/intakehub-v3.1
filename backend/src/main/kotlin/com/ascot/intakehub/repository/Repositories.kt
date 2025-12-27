package com.ascot.intakehub.repository

import com.ascot.intakehub.model.NormalizedData
import com.ascot.intakehub.model.Provider
import com.ascot.intakehub.model.RawData
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface ProviderRepository : ReactiveCrudRepository<Provider, UUID> {
    fun findByName(name: String): Mono<Provider>
    fun findByEnabledTrueOrderByPriorityAsc(): Flux<Provider>
}

@Repository
interface RawDataRepository : ReactiveCrudRepository<RawData, UUID>

@Repository
interface NormalizedDataRepository : ReactiveCrudRepository<NormalizedData, UUID>
