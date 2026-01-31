package com.eyuppastirmaci.shriven.backend.url

import com.eyuppastirmaci.shriven.backend.properties.AppProperties
import com.eyuppastirmaci.shriven.backend.url.dto.request.ShortenUrlRequest
import com.eyuppastirmaci.shriven.backend.url.dto.response.ShortenUrlResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class UrlController(
    private val urlService: UrlService,
    private val appProperties: AppProperties
) {

    @PostMapping("/api/shorten")
    fun shortenUrl(@Valid @RequestBody request: ShortenUrlRequest): ResponseEntity<ShortenUrlResponse> {
        val entity = urlService.shortenUrl(request)

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

        val userAgent = request.getHeader("User-Agent")
        val ipAddress = request.remoteAddr

        val longUrl = urlService.getLongUrl(shortCode, userAgent, ipAddress)

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(longUrl))
            .build()
    }
}