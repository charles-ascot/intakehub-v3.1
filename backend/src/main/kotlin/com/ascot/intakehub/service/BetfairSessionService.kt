package com.ascot.intakehub.service

import com.ascot.intakehub.service.CredentialService
import io.netty.handler.ssl.SslContextBuilder
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.io.ByteArrayInputStream
import java.security.KeyFactory
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import java.util.UUID

@Service
class BetfairSessionService(
    private val credentialService: CredentialService
) {

    private val authUrl = "https://identitysso-cert.betfair.com/api/certlogin"
    // Helper to cache session token per providerId? 
    // For now, simple fetch-on-demand with basic caching strategy could be added.
    // We'll return Mono<String>

    fun getSessionToken(providerId: UUID): Mono<String> {
        return credentialService.getCredentials(providerId)
            .flatMap { creds ->
                val appKey = creds["app_key"] ?: throw IllegalArgumentException("Missing app_key")
                val username = creds["username"] ?: throw IllegalArgumentException("Missing username")
                val password = creds["password"] ?: throw IllegalArgumentException("Missing password")
                val certPem = creds["cert_pem"] ?: throw IllegalArgumentException("Missing cert_pem")
                val keyPem = creds["key_pem"] ?: throw IllegalArgumentException("Missing key_pem")

                // 1. Prepare SSL Context
                val keyStore = createKeyStore(certPem, keyPem)
                
                // 2. Create Custom WebClient with SSL
                val sslContext = SslContextBuilder.forClient()
                    .keyManager(getKeyManagerFactory(keyStore, ""))
                    .build()
                
                val httpClient = HttpClient.create()
                    .secure { t -> t.sslContext(sslContext) }
                
                val authClient = WebClient.builder()
                    .clientConnector(ReactorClientHttpConnector(httpClient))
                    .build()
                
                // 3. Call Auth Endpoint
                authClient.post()
                    .uri(authUrl)
                    .header("X-Application", appKey)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue("username=$username&password=$password")
                    .retrieve()
                    .bodyToMono(Map::class.java)
                    .map { response -> 
                        response["sessionToken"] as? String 
                            ?: throw IllegalStateException("No session token in response: $response")
                    }
                    .doOnError { e -> println("Betfair Auth Failed: ${e.message}") }
            }
    }

    private fun createKeyStore(certPem: String, keyPem: String): KeyStore {
        // Strip headers
        val certClean = cleanPem(certPem)
        val keyClean = cleanPem(keyPem)

        val certFactory = CertificateFactory.getInstance("X.509")
        val cert = certFactory.generateCertificate(ByteArrayInputStream(Base64.getDecoder().decode(certClean)))

        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyClean))
        val privateKey = keyFactory.generatePrivate(keySpec)

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        keyStore.setKeyEntry("client", privateKey, "".toCharArray(), arrayOf(cert))
        return keyStore
    }
    
    private fun getKeyManagerFactory(keyStore: KeyStore, password: String): javax.net.ssl.KeyManagerFactory {
        val kmf = javax.net.ssl.KeyManagerFactory.getInstance(javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(keyStore, password.toCharArray())
        return kmf
    }

    private fun cleanPem(pem: String): String {
        return pem.replace("-----BEGIN CERTIFICATE-----", "")
            .replace("-----END CERTIFICATE-----", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----BEGIN RSA PRIVATE KEY-----", "") // Just in case
            .replace("-----END RSA PRIVATE KEY-----", "")
            .replace("\\s+".toRegex(), "")
    }
}
