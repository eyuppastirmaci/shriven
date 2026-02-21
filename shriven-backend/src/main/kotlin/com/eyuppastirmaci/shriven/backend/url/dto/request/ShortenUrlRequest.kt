package com.eyuppastirmaci.shriven.backend.url.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ShortenUrlRequest(
    @field:NotBlank(message = "URL cannot be empty")
    @field:Size(max = 2048, message = "URL must not exceed 2048 characters")
    @field:Pattern(
        regexp = "^https?://.*",
        message = "URL must start with http:// or https://"
    )
    val longUrl: String,

    @field:Pattern(
        regexp = "^[a-zA-Z0-9_-]{3,30}$",
        message = "Custom alias must be 3–30 characters and contain only letters, numbers, hyphens, or underscores"
    )
    val customAlias: String? = null,

    val expiresAt: String? = null,

    val tagIds: List<Long>? = null,

    @field:Size(min = 1, max = 128, message = "Password must be 1–128 characters")
    val password: String? = null
)