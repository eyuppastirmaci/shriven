package com.eyuppastirmaci.shriven.backend.url.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class ShortenUrlRequest(
    @field:NotBlank(message = "URL cannot be empty")
    @field:Pattern(
        regexp = "^https?://.*",
        message = "URL must start with http:// or https://"
    )
    val longUrl: String,

    val customAlias: String? = null,
    val expiresAt: String? = null
)