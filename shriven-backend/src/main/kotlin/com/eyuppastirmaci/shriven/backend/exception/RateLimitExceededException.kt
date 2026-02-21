package com.eyuppastirmaci.shriven.backend.exception

class RateLimitExceededException(
    message: String = "Rate limit exceeded",
    val retryAfterSeconds: Int? = null
) : RuntimeException(message)
