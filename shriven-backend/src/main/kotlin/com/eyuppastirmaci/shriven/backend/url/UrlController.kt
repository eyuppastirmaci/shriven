package com.eyuppastirmaci.shriven.backend.url

import com.eyuppastirmaci.shriven.backend.auth.AuthPrincipal
import com.eyuppastirmaci.shriven.backend.properties.AppProperties
import com.eyuppastirmaci.shriven.backend.snowflake.Base62Encoder
import com.eyuppastirmaci.shriven.backend.url.dto.request.ShortenUrlRequest
import com.eyuppastirmaci.shriven.backend.url.dto.response.ShortenUrlResponse
import com.eyuppastirmaci.shriven.backend.url.dto.response.UserUrlResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class UrlController(
    private val urlService: UrlService,
    private val appProperties: AppProperties,
    private val base62Encoder: Base62Encoder
) {

    @PostMapping("/api/shorten")
    fun shortenUrl(
        @Valid @RequestBody request: ShortenUrlRequest,
        @AuthenticationPrincipal principal: AuthPrincipal?
    ): ResponseEntity<ShortenUrlResponse> {
        val entity = urlService.shortenUrl(request, principal?.userId)

        val response = ShortenUrlResponse(
            shortUrl = "${appProperties.baseUrl}/${entity.shortCode}",
            longUrl = entity.longUrl,
            shortCode = entity.shortCode,
            createdAt = entity.createdAt.toString(),
            expiresAt = entity.expiresAt?.toString()
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{shortCode}")
    fun redirect(
        @PathVariable shortCode: String,
        request: HttpServletRequest
    ): ResponseEntity<Void> {
        if (!base62Encoder.isValid(shortCode)) {
            return ResponseEntity.badRequest().build()
        }

        val userAgent = request.getHeader("User-Agent")
        val ipAddress = request.remoteAddr

        val longUrl = urlService.getLongUrl(shortCode, userAgent, ipAddress)

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(longUrl))
            .build()
    }

    @GetMapping("/api/urls")
    fun getUserUrls(
        @AuthenticationPrincipal principal: AuthPrincipal
    ): ResponseEntity<List<UserUrlResponse>> {
        val urls = urlService.getUserUrls(principal.userId).map { entity ->
            UserUrlResponse(
                shortCode = entity.shortCode,
                shortUrl = "${appProperties.baseUrl}/${entity.shortCode}",
                longUrl = entity.longUrl,
                clickCount = entity.clickCount,
                createdAt = entity.createdAt.toString(),
                expiresAt = entity.expiresAt?.toString()
            )
        }
        return ResponseEntity.ok(urls)
    }

    @DeleteMapping("/api/urls/{shortCode}")
    fun deleteUrl(
        @PathVariable shortCode: String,
        @AuthenticationPrincipal principal: AuthPrincipal
    ): ResponseEntity<Void> {
        urlService.deleteUrl(shortCode, principal.userId)
        return ResponseEntity.noContent().build()
    }
}
