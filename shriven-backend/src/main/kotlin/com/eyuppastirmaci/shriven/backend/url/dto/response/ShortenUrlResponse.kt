package com.eyuppastirmaci.shriven.backend.url.dto.response

data class ShortenUrlResponse(
    val shortUrl: String,
    val longUrl: String,
    val shortCode: String,
    val createdAt: String,
    val expiresAt: String?
)