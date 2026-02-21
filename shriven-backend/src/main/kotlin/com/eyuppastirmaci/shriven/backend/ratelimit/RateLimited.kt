package com.eyuppastirmaci.shriven.backend.ratelimit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimited(
    val requestsPerMinute: Int,
    val keyPrefix: String
)
