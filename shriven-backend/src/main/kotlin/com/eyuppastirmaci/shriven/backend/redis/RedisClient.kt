package com.eyuppastirmaci.shriven.backend.redis

import com.eyuppastirmaci.shriven.backend.exception.RedisOperationException
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * Central Redis client used by URL cache and rate limiting.
 * Exposes only generic key-value operations; callers build keys and pass TTL as needed.
 */
@Component
class RedisClient(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private val logger = LoggerFactory.getLogger(RedisClient::class.java)
    }

    /**
     * Gets the value for [key]. Returns null if the key does not exist or Redis is unavailable.
     */
    fun get(key: String): String? = try {
        redisTemplate.opsForValue().get(key)
    } catch (e: Exception) {
        logger.warn("Redis get failed for key: $key", e)
        null
    }

    /**
     * Sets [key] to [value] with TTL [ttlMillis] in milliseconds.
     * @throws RedisOperationException on failure
     */
    fun set(key: String, value: String, ttlMillis: Long) {
        try {
            redisTemplate.opsForValue().set(key, value, ttlMillis, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            throw RedisOperationException("Failed to set key: $key", e)
        }
    }

    /**
     * Deletes [key].
     * @throws RedisOperationException on failure
     */
    fun delete(key: String) {
        try {
            redisTemplate.delete(key)
        } catch (e: Exception) {
            throw RedisOperationException("Failed to delete key: $key", e)
        }
    }

    /**
     * Increments the value at [key] by 1. Returns the new value, or null on Redis failure.
     */
    fun increment(key: String): Long? = try {
        redisTemplate.opsForValue().increment(key) ?: 1L
    } catch (e: Exception) {
        logger.warn("Redis increment failed for key: $key", e)
        null
    }

    /**
     * Sets TTL for [key] to [seconds]. Returns true on success, false on failure.
     */
    fun expire(key: String, seconds: Long): Boolean = try {
        redisTemplate.expire(key, seconds, TimeUnit.SECONDS)
    } catch (e: Exception) {
        logger.warn("Redis expire failed for key: $key", e)
        false
    }

    /**
     * Returns the remaining TTL of [key] in seconds, or null if key does not exist or has no expiry.
     */
    fun getExpireSeconds(key: String): Long? = try {
        val ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS)
        if (ttl != null && ttl > 0) ttl else null
    } catch (e: Exception) {
        logger.warn("Redis getExpire failed for key: $key", e)
        null
    }
}