package com.eyuppastirmaci.shriven.backend.ratelimit

import com.eyuppastirmaci.shriven.backend.redis.RedisClient
import org.springframework.stereotype.Component

@Component
class RateLimitService(
    private val redisClient: RedisClient
) {
    companion object {
        private const val KEY_PREFIX = "rate_limit:"
    }

    /**
     * Checks the current count for the key and increments it. Uses fixed-window rate limiting.
     * On Redis failure, fails open (allows the request).
     *
     * @param keyPrefix e.g. "global", "shorten", "redirect"
     * @param ip client IP
     * @param limit max requests allowed in the window
     * @param windowSeconds window duration in seconds
     * @return RateLimitResult with allowed flag and optional retryAfterSeconds when not allowed
     */
    fun checkAndIncrement(
        keyPrefix: String,
        ip: String,
        limit: Int,
        windowSeconds: Long
    ): RateLimitResult {
        val key = "$KEY_PREFIX$keyPrefix:$ip"
        val count = redisClient.increment(key) ?: return RateLimitResult(allowed = true)
        if (count == 1L) {
            redisClient.expire(key, windowSeconds)
        }
        val overLimit = count > limit
        val retryAfter = if (overLimit) {
            (redisClient.getExpireSeconds(key) ?: windowSeconds).toInt()
        } else null
        return RateLimitResult(allowed = !overLimit, retryAfterSeconds = retryAfter)
    }
}
