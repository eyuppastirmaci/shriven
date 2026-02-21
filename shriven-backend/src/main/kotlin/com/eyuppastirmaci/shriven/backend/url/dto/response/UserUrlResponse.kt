package com.eyuppastirmaci.shriven.backend.url.dto.response

data class UserUrlResponse(
    val shortCode: String,
    val shortUrl: String,
    val longUrl: String,
    val clickCount: Long,
    val createdAt: String,
    val expiresAt: String?
)
