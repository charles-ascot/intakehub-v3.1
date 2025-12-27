package com.ascot.intakehub.controller

import com.ascot.intakehub.model.Provider
import com.ascot.intakehub.repository.ProviderRepository
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1/providers")
@CrossOrigin // Dev only
class ProviderController(private val providerRepository: ProviderRepository) {

    @GetMapping
    fun getAllProviders(): Flux<Provider> {
        return providerRepository.findAll()
    }

    @PostMapping
    fun createProvider(@RequestBody provider: Provider): Mono<Provider> {
        // ID should be generated if null
        return providerRepository.save(provider.copy(id = provider.id ?: UUID.randomUUID()))
    }

    @GetMapping("/{id}")
    fun getProvider(@PathVariable id: UUID): Mono<Provider> {
        return providerRepository.findById(id)
    }
}
