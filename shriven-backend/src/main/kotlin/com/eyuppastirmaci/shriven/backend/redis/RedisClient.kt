package com.eyuppastirmaci.shriven.backend.redis

import com.eyuppastirmaci.shriven.backend.exception.RedisOperationException
import com.eyuppastirmaci.shriven.backend.properties.CacheProperties
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedisClient(
    private val redisTemplate: StringRedisTemplate,
    private val cacheProperties: CacheProperties
) {
    companion object {
        private const val SHORT_URL_KEY_PREFIX = "short:"
    }

    /**
     * Stores the mapping of shortCode -> longUrl in Redis.
     *
     * The key is prefixed with [SHORT_URL_KEY_PREFIX] to avoid collisions.
     * The entry is set with a Time-To-Live (TTL) defined in [CacheProperties].
     *
     * @param shortCode The unique short code generated for the URL.
     * @param longUrl The original long URL to be cached.
     */
    fun saveUrl(shortCode: String, longUrl: String) {
        val key = "$SHORT_URL_KEY_PREFIX$shortCode"
        try {
            redisTemplate.opsForValue().set(
                key,
                longUrl,
                cacheProperties.ttl.toMillis(),
                TimeUnit.MILLISECONDS
            )
        } catch (e: Exception) {
            throw RedisOperationException("Failed to save URL to cache for key: $key", e)
        }
    }

    /**
     * Retrieves the original long URL associated with the given short code.
     *
     * @param shortCode The short code to look up.
     * @return The original long URL if found, or null if the key does not exist or Redis is unavailable.
     */
    fun getUrl(shortCode: String): String? {
        val key = "$SHORT_URL_KEY_PREFIX$shortCode"
        return try {
            redisTemplate.opsForValue().get(key)
        } catch (e: Exception) {
            throw RedisOperationException("Failed to retrieve URL from cache for key: $key", e)
        }
    }

    /**
     * Removes the URL mapping from the cache.
     *
     * Useful for cache invalidation or manual cleanup.
     *
     * @param shortCode The short code of the mapping to remove.
     */
    fun deleteUrl(shortCode: String) {
        val key = "$SHORT_URL_KEY_PREFIX$shortCode"
        try {
            redisTemplate.delete(key)
        } catch (e: Exception) {
            throw RedisOperationException("Failed to delete URL from cache for key: $key", e)
        }
    }
}