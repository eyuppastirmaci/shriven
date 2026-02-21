package com.eyuppastirmaci.shriven.backend.ratelimit

data class RateLimitResult(
    val allowed: Boolean,
    val retryAfterSeconds: Int? = null
)
