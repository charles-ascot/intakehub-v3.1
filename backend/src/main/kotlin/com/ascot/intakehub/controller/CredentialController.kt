package com.ascot.intakehub.controller

import com.ascot.intakehub.service.CredentialService
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1/credentials")
@CrossOrigin
class CredentialController(private val credentialService: CredentialService) {

    @PostMapping("/{providerId}")
    fun saveCredentials(
        @PathVariable providerId: UUID,
        @RequestBody credentials: Map<String, String>
    ): Mono<String> {
        return credentialService.saveCredentials(providerId, credentials)
            .map { "Credentials saved" }
    }
    
    // NOTE: We don't expose GET credentials in API for security, backend internals only usually.
    // Or maybe we verify existence.
}
