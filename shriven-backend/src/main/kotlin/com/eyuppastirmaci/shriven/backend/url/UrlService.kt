package com.eyuppastirmaci.shriven.backend.url

import com.eyuppastirmaci.shriven.backend.analytics.dto.ClickEvent
import com.eyuppastirmaci.shriven.backend.exception.UrlExpiredException
import com.eyuppastirmaci.shriven.backend.exception.UrlNotFoundException
import com.eyuppastirmaci.shriven.backend.kafka.KafkaClient
import com.eyuppastirmaci.shriven.backend.redis.RedisClient
import com.eyuppastirmaci.shriven.backend.snowflake.Base62Encoder
import com.eyuppastirmaci.shriven.backend.snowflake.SnowflakeIdGenerator
import com.eyuppastirmaci.shriven.backend.url.dto.request.ShortenUrlRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class UrlService(
    private val urlRepository: UrlRepository,
    private val snowflakeIdGenerator: SnowflakeIdGenerator,
    private val base62Encoder: Base62Encoder,
    private val redisClient: RedisClient,
    private val kafkaClient: KafkaClient
) {

    /**
     * Creates a shortened URL from a long URL.
     *
     * Implements Write-Through Caching:
     * Generates a unique ID and short code.
     * Persists the entity to PostgreSQL.
     * Immediately caches the result in Redis to ensure subsequent reads are fast.
     *
     * @param request The request object containing the long URL and optional expiration.
     * @return The persisted UrlEntity.
     */
    @Transactional
    fun shortenUrl(request: ShortenUrlRequest): UrlEntity {
        // Generate unique ID and encode to short code
        val id = snowflakeIdGenerator.nextId()
        val shortCode = base62Encoder.encode(id)

        // Parse expiration date if provided
        val expiresAt = request.expiresAt?.let { Instant.parse(it) }

        // Create and save entity
        val entity = UrlEntity(
            id = id,
            shortCode = shortCode,
            longUrl = request.longUrl,
            createdAt = Instant.now(),
            expiresAt = expiresAt,
            clickCount = 0
        )

        val savedEntity = urlRepository.save(entity)

        // Write-Through: Cache immediately
        redisClient.saveUrl(shortCode, request.longUrl)

        return savedEntity
    }

    /**
     * Retrieves the original long URL for a given short code.
     *
     * Implements Cache-Aside Pattern:
     * Checks Redis cache first.
     * If missing, queries PostgreSQL.
     * Validates expiration (if applicable).
     * Populates Redis cache for future requests.
     *
     * @param shortCode The short code to resolve.
     * @return The original long URL.
     * @throws UrlNotFoundException if the short code does not exist.
     * @throws UrlExpiredException if the link has passed its expiration date.
     */
    fun getLongUrl(shortCode: String, userAgent: String?, ipAddress: String?): String {
        // Check Cache
        val cachedUrl = redisClient.getUrl(shortCode)
        if (!cachedUrl.isNullOrEmpty()) {
            // FIRE AND FORGET: Send event to Kafka
            kafkaClient.sendClickEvent(ClickEvent(shortCode, Instant.now(), userAgent, ipAddress))
            return cachedUrl
        }

        // Cache Miss -> DB Lookup
        val entity = urlRepository.findByShortCode(shortCode)
            ?: throw UrlNotFoundException("Short code not found: $shortCode")

        // Check Expiry
        entity.expiresAt?.let { expiresAt ->
            if (Instant.now().isAfter(expiresAt)) {
                redisClient.deleteUrl(shortCode)
                throw UrlExpiredException("Short code has expired: $shortCode")
            }
        }

        // Cache-Aside
        redisClient.saveUrl(shortCode, entity.longUrl)

        // Analytics (Async)
        kafkaClient.sendClickEvent(ClickEvent(shortCode, Instant.now(), userAgent, ipAddress))

        return entity.longUrl
    }
}