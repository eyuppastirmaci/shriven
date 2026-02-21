package com.eyuppastirmaci.shriven.backend.url

import com.eyuppastirmaci.shriven.backend.exception.AccessDeniedException
import com.eyuppastirmaci.shriven.backend.exception.AliasAlreadyTakenException
import com.eyuppastirmaci.shriven.backend.exception.DuplicateLinkException
import com.eyuppastirmaci.shriven.backend.exception.UrlExpiredException
import com.eyuppastirmaci.shriven.backend.exception.UrlNotFoundException
import com.eyuppastirmaci.shriven.backend.exception.UrlPausedException
import com.eyuppastirmaci.shriven.backend.properties.AppProperties
import com.eyuppastirmaci.shriven.backend.properties.CacheProperties
import com.eyuppastirmaci.shriven.backend.redis.RedisClient
import com.eyuppastirmaci.shriven.backend.snowflake.Base62Encoder
import com.eyuppastirmaci.shriven.backend.snowflake.SnowflakeIdGenerator
import com.eyuppastirmaci.shriven.backend.tag.TagEntity
import com.eyuppastirmaci.shriven.backend.tag.TagRepository
import com.eyuppastirmaci.shriven.backend.tag.dto.TagResponse
import com.eyuppastirmaci.shriven.backend.url.dto.request.ShortenUrlRequest
import com.eyuppastirmaci.shriven.backend.url.dto.request.UpdateUrlRequest
import com.eyuppastirmaci.shriven.backend.url.dto.response.UserUrlResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class UrlService(
    private val urlRepository: UrlRepository,
    private val tagRepository: TagRepository,
    private val snowflakeIdGenerator: SnowflakeIdGenerator,
    private val base62Encoder: Base62Encoder,
    private val redisClient: RedisClient,
    private val cacheProperties: CacheProperties,
    private val appProperties: AppProperties
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UrlService::class.java)
        private const val URL_CACHE_KEY_PREFIX = "short:"
    }

    private fun urlCacheKey(shortCode: String): String = "$URL_CACHE_KEY_PREFIX$shortCode"

    @Transactional
    fun shortenUrl(request: ShortenUrlRequest, userId: Long? = null): UrlEntity {
        // Duplicate detection: if an authenticated user already has a short link for this URL, reject
        if (userId != null) {
            val existing = urlRepository.findByLongUrlAndUserId(request.longUrl, userId)
            if (existing != null) {
                val existingResponse = toUserUrlResponse(existing)
                throw DuplicateLinkException(
                    "You already have a short link for this URL",
                    existingResponse
                )
            }
        }

        val id = snowflakeIdGenerator.nextId()
        val isCustomAlias = !request.customAlias.isNullOrBlank()

        val shortCode = if (isCustomAlias) {
            val alias = request.customAlias!!
            if (urlRepository.existsByShortCode(alias)) {
                throw AliasAlreadyTakenException("The alias '$alias' is already taken")
            }
            alias
        } else {
            base62Encoder.encode(id)
        }

        val expiresAt = request.expiresAt?.let { Instant.parse(it) }

        val tags: MutableSet<TagEntity> = if (!request.tagIds.isNullOrEmpty() && userId != null) {
            tagRepository.findAllById(request.tagIds)
                .filter { it.userId == userId }
                .toMutableSet()
        } else {
            mutableSetOf()
        }

        val entity = UrlEntity(
            id = id,
            shortCode = shortCode,
            longUrl = request.longUrl,
            createdAt = Instant.now(),
            expiresAt = expiresAt,
            clickCount = 0,
            userId = userId,
            isCustomAlias = isCustomAlias,
            tags = tags
        )

        val savedEntity = urlRepository.save(entity)
        redisClient.set(urlCacheKey(shortCode), request.longUrl, cacheProperties.ttl.toMillis())

        return savedEntity
    }

    /**
     * Implements Cache-Aside Pattern.
     * Redis is always checked first; on miss, falls back to PostgreSQL.
     * Paused links are never served from cache (cache is cleared on pause).
     * Click events are published by RedirectClickPublishAspect after successful redirect.
     */
    fun getLongUrl(shortCode: String): String {
        val cachedUrl = redisClient.get(urlCacheKey(shortCode))
        if (!cachedUrl.isNullOrEmpty()) {
            return cachedUrl
        }

        val entity = urlRepository.findByShortCode(shortCode)
            ?: throw UrlNotFoundException("Short code not found: $shortCode")

        entity.expiresAt?.let { expiresAt ->
            if (Instant.now().isAfter(expiresAt)) {
                throw UrlExpiredException("Short code has expired: $shortCode")
            }
        }

        if (!entity.isActive) {
            throw UrlPausedException("This link is currently paused: $shortCode")
        }

        try {
            redisClient.set(urlCacheKey(shortCode), entity.longUrl, cacheProperties.ttl.toMillis())
        } catch (e: Exception) {
            logger.warn("Failed to populate cache for $shortCode", e)
        }

        return entity.longUrl
    }

    @Transactional(readOnly = true)
    fun getUserUrls(userId: Long, tagId: Long? = null): List<UrlEntity> =
        if (tagId != null) {
            urlRepository.findAllByUserIdAndTagIdOrderByCreatedAtDesc(userId, tagId)
        } else {
            urlRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        }

    @Transactional(readOnly = true)
    fun isAliasAvailable(alias: String): Boolean = !urlRepository.existsByShortCode(alias)

    @Transactional
    fun updateUrl(shortCode: String, request: UpdateUrlRequest, userId: Long): UrlEntity {
        val entity = urlRepository.findByShortCode(shortCode)
            ?: throw UrlNotFoundException("Short code not found: $shortCode")

        if (entity.userId != userId) {
            throw AccessDeniedException("You do not have permission to edit this link")
        }

        val newAlias = request.customAlias
        if (!newAlias.isNullOrBlank() && newAlias != entity.shortCode) {
            if (urlRepository.existsByShortCode(newAlias)) {
                throw AliasAlreadyTakenException("The alias '$newAlias' is already taken")
            }
            redisClient.delete(urlCacheKey(entity.shortCode))
            entity.shortCode = newAlias
            entity.isCustomAlias = true
            redisClient.set(urlCacheKey(newAlias), entity.longUrl, cacheProperties.ttl.toMillis())
        }

        when {
            request.clearExpiration -> entity.expiresAt = null
            request.expiresAt != null -> entity.expiresAt = Instant.parse(request.expiresAt)
        }

        if (request.tagIds != null) {
            val ownedTags = tagRepository.findAllById(request.tagIds)
                .filter { it.userId == userId }
                .toMutableSet()
            entity.tags = ownedTags
        }

        return urlRepository.save(entity)
    }

    @Transactional
    fun toggleUrlStatus(shortCode: String, active: Boolean, userId: Long) {
        val entity = urlRepository.findByShortCode(shortCode)
            ?: throw UrlNotFoundException("Short code not found: $shortCode")

        if (entity.userId != userId) {
            throw AccessDeniedException("You do not have permission to change the status of this link")
        }

        entity.isActive = active

        if (!active) {
            // Remove from cache so the hot path falls back to DB and sees the paused state
            redisClient.delete(urlCacheKey(shortCode))
        } else {
            // Re-populate cache when reactivating
            try {
                redisClient.set(urlCacheKey(shortCode), entity.longUrl, cacheProperties.ttl.toMillis())
            } catch (e: Exception) {
                logger.warn("Failed to re-populate cache for $shortCode after reactivation", e)
            }
        }

        urlRepository.save(entity)
    }

    @Transactional
    fun deleteUrl(shortCode: String, userId: Long) {
        val entity = urlRepository.findByShortCode(shortCode)
            ?: throw UrlNotFoundException("Short code not found: $shortCode")

        if (entity.userId != userId) {
            throw AccessDeniedException("You do not have permission to delete this link")
        }

        redisClient.delete(urlCacheKey(shortCode))
        urlRepository.delete(entity)
    }

    fun toUserUrlResponse(entity: UrlEntity): UserUrlResponse = UserUrlResponse(
        shortCode = entity.shortCode,
        shortUrl = "${appProperties.baseUrl}/${entity.shortCode}",
        longUrl = entity.longUrl,
        clickCount = entity.clickCount,
        createdAt = entity.createdAt.toString(),
        expiresAt = entity.expiresAt?.toString(),
        isActive = entity.isActive,
        isCustomAlias = entity.isCustomAlias,
        tags = entity.tags.map { TagResponse(it.id, it.name, it.createdAt.toString()) }
    )
}
