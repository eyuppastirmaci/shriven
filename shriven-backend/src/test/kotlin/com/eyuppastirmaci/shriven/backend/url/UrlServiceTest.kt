package com.eyuppastirmaci.shriven.backend.url

import com.eyuppastirmaci.shriven.backend.exception.AccessDeniedException
import com.eyuppastirmaci.shriven.backend.exception.DuplicateLinkException
import com.eyuppastirmaci.shriven.backend.exception.InvalidLinkPasswordException
import com.eyuppastirmaci.shriven.backend.exception.RedisOperationException
import com.eyuppastirmaci.shriven.backend.exception.RequiresPasswordException
import com.eyuppastirmaci.shriven.backend.exception.UrlExpiredException
import com.eyuppastirmaci.shriven.backend.exception.UrlNotFoundException
import com.eyuppastirmaci.shriven.backend.exception.UrlPausedException
import com.eyuppastirmaci.shriven.backend.properties.AppProperties
import com.eyuppastirmaci.shriven.backend.properties.CacheProperties
import com.eyuppastirmaci.shriven.backend.redis.RedisClient
import com.eyuppastirmaci.shriven.backend.snowflake.Base62Encoder
import com.eyuppastirmaci.shriven.backend.snowflake.SnowflakeIdGenerator
import com.eyuppastirmaci.shriven.backend.tag.TagRepository
import com.eyuppastirmaci.shriven.backend.url.dto.request.ShortenUrlRequest
import com.eyuppastirmaci.shriven.backend.url.dto.request.UpdateUrlRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UrlServiceTest {

    private val urlRepository = mockk<UrlRepository>(relaxed = true)
    private val tagRepository = mockk<TagRepository>(relaxed = true)
    private val snowflakeIdGenerator = mockk<SnowflakeIdGenerator>()
    private val base62Encoder = mockk<Base62Encoder>()
    private val redisClient = mockk<RedisClient>(relaxed = true)
    private val cacheProperties = CacheProperties(ttl = Duration.ofHours(24))
    private val appProperties = AppProperties(baseUrl = "https://sho.rt")
    private val passwordEncoder = mockk<PasswordEncoder>()

    private val urlService = UrlService(
        urlRepository, tagRepository, snowflakeIdGenerator, base62Encoder,
        redisClient, cacheProperties, appProperties, passwordEncoder
    )

    // -- shortenUrl --

    @Test
    fun `shortenUrl creates entity and caches it`() {
        every { snowflakeIdGenerator.nextId() } returns 12345L
        every { base62Encoder.encode(12345L) } returns "abc"
        every { urlRepository.findByLongUrlAndUserId(any(), any()) } returns null
        val request = ShortenUrlRequest(longUrl = "https://example.com")
        val savedEntity = UrlEntity(
            id = 12345L, shortCode = "abc", longUrl = "https://example.com",
            createdAt = Instant.now(), clickCount = 0, passwordHash = null
        )
        every { urlRepository.save(any()) } returns savedEntity

        val result = urlService.shortenUrl(request, userId = 1L)

        assertEquals("abc", result.shortCode)
        assertEquals("https://example.com", result.longUrl)
        verify { urlRepository.save(any()) }
        verify { redisClient.set("short:abc", "https://example.com", any()) }
    }

    @Test
    fun `shortenUrl with password sets passwordHash and caches sentinel`() {
        every { snowflakeIdGenerator.nextId() } returns 12345L
        every { base62Encoder.encode(12345L) } returns "abc"
        every { urlRepository.findByLongUrlAndUserId(any(), any()) } returns null
        every { passwordEncoder.encode("secret") } returns "\$2a\$10\$fakehash"
        val request = ShortenUrlRequest(longUrl = "https://example.com", password = "secret")
        val savedEntity = UrlEntity(
            id = 12345L, shortCode = "abc", longUrl = "https://example.com",
            createdAt = Instant.now(), clickCount = 0, passwordHash = "\$2a\$10\$fakehash"
        )
        every { urlRepository.save(any()) } returns savedEntity

        val result = urlService.shortenUrl(request, userId = 1L)

        assertEquals("abc", result.shortCode)
        assertTrue(result.passwordHash != null)
        verify { redisClient.set("short:abc", "__PWD__", any()) }
    }

    @Test
    fun `shortenUrl throws DuplicateLinkException when user already has a link for same URL`() {
        val existingEntity = UrlEntity(
            id = 999L, shortCode = "existing", longUrl = "https://example.com",
            createdAt = Instant.now(), clickCount = 5, userId = 1L
        )
        every { urlRepository.findByLongUrlAndUserId("https://example.com", 1L) } returns existingEntity

        assertThrows<DuplicateLinkException> {
            urlService.shortenUrl(ShortenUrlRequest(longUrl = "https://example.com"), userId = 1L)
        }
    }

    // -- getLongUrl --

    @Test
    fun `getLongUrl returns cached URL on cache hit`() {
        every { redisClient.get("short:abc") } returns "https://example.com"

        val result = urlService.getLongUrl("abc")

        assertEquals("https://example.com", result)
        verify(exactly = 0) { urlRepository.findByShortCode(any()) }
    }

    @Test
    fun `getLongUrl falls back to DB on cache miss and populates cache`() {
        every { redisClient.get("short:abc") } returns null
        val entity = UrlEntity(
            id = 1L, shortCode = "abc", longUrl = "https://example.com",
            createdAt = Instant.now(), clickCount = 0
        )
        every { urlRepository.findByShortCode("abc") } returns entity

        val result = urlService.getLongUrl("abc")

        assertEquals("https://example.com", result)
        verify { redisClient.set("short:abc", "https://example.com", any()) }
    }

    @Test
    fun `getLongUrl throws UrlNotFoundException when shortCode not in DB`() {
        every { redisClient.get("short:nope") } returns null
        every { urlRepository.findByShortCode("nope") } returns null

        assertThrows<UrlNotFoundException> {
            urlService.getLongUrl("nope")
        }
    }

    @Test
    fun `getLongUrl throws UrlExpiredException for expired URL`() {
        every { redisClient.get("short:exp") } returns null
        val entity = UrlEntity(
            id = 1L, shortCode = "exp", longUrl = "https://expired.com",
            createdAt = Instant.now(), expiresAt = Instant.parse("2020-01-01T00:00:00Z"), clickCount = 0
        )
        every { urlRepository.findByShortCode("exp") } returns entity

        assertThrows<UrlExpiredException> {
            urlService.getLongUrl("exp")
        }
    }

    @Test
    fun `getLongUrl throws UrlPausedException for paused URL`() {
        every { redisClient.get("short:paused") } returns null
        val entity = UrlEntity(
            id = 1L, shortCode = "paused", longUrl = "https://example.com",
            createdAt = Instant.now(), clickCount = 0, isActive = false
        )
        every { urlRepository.findByShortCode("paused") } returns entity

        assertThrows<UrlPausedException> {
            urlService.getLongUrl("paused")
        }
    }

    @Test
    fun `getLongUrl still works when Redis fails on cache populate`() {
        every { redisClient.get("short:abc") } returns null
        val entity = UrlEntity(
            id = 1L, shortCode = "abc", longUrl = "https://example.com",
            createdAt = Instant.now(), clickCount = 0
        )
        every { urlRepository.findByShortCode("abc") } returns entity
        every { redisClient.set(any(), any(), any()) } throws RedisOperationException("Connection refused")

        val result = urlService.getLongUrl("abc")

        assertEquals("https://example.com", result)
    }

    @Test
    fun `getLongUrl throws RequiresPasswordException when cache has sentinel`() {
        every { redisClient.get("short:abc") } returns "__PWD__"

        assertThrows<RequiresPasswordException> {
            urlService.getLongUrl("abc")
        }.also { assertEquals("abc", it.shortCode) }
    }

    @Test
    fun `getLongUrl throws RequiresPasswordException when entity has passwordHash`() {
        every { redisClient.get("short:abc") } returns null
        val entity = UrlEntity(
            id = 1L, shortCode = "abc", longUrl = "https://example.com",
            createdAt = Instant.now(), clickCount = 0, passwordHash = "\$2a\$10\$hash"
        )
        every { urlRepository.findByShortCode("abc") } returns entity

        assertThrows<RequiresPasswordException> {
            urlService.getLongUrl("abc")
        }
        verify { redisClient.set("short:abc", "__PWD__", any()) }
    }

    // -- unlock --

    @Test
    fun `unlock returns longUrl when password matches`() {
        val entity = UrlEntity(
            id = 1L, shortCode = "abc", longUrl = "https://target.com",
            createdAt = Instant.now(), clickCount = 0, passwordHash = "\$2a\$10\$hash"
        )
        every { urlRepository.findByShortCode("abc") } returns entity
        every { passwordEncoder.matches("secret", "\$2a\$10\$hash") } returns true

        val result = urlService.unlock("abc", "secret")

        assertEquals("https://target.com", result)
    }

    @Test
    fun `unlock throws InvalidLinkPasswordException when password does not match`() {
        val entity = UrlEntity(
            id = 1L, shortCode = "abc", longUrl = "https://target.com",
            createdAt = Instant.now(), clickCount = 0, passwordHash = "\$2a\$10\$hash"
        )
        every { urlRepository.findByShortCode("abc") } returns entity
        every { passwordEncoder.matches("wrong", "\$2a\$10\$hash") } returns false

        assertThrows<InvalidLinkPasswordException> {
            urlService.unlock("abc", "wrong")
        }
    }

    @Test
    fun `unlock returns longUrl when link has no password`() {
        val entity = UrlEntity(
            id = 1L, shortCode = "abc", longUrl = "https://target.com",
            createdAt = Instant.now(), clickCount = 0, passwordHash = null
        )
        every { urlRepository.findByShortCode("abc") } returns entity

        val result = urlService.unlock("abc", "any")

        assertEquals("https://target.com", result)
    }

    @Test
    fun `unlock throws UrlNotFoundException when shortCode not found`() {
        every { urlRepository.findByShortCode("nope") } returns null

        assertThrows<UrlNotFoundException> {
            urlService.unlock("nope", "secret")
        }
    }

    @Test
    fun `toUserUrlResponse sets passwordProtected true when entity has passwordHash`() {
        val entity = UrlEntity(
            id = 1L, shortCode = "abc", longUrl = "https://example.com",
            createdAt = Instant.now(), clickCount = 0, passwordHash = "\$2a\$10\$hash"
        )

        val response = urlService.toUserUrlResponse(entity)

        assertTrue(response.passwordProtected)
    }

    @Test
    fun `toUserUrlResponse sets passwordProtected false when entity has no passwordHash`() {
        val entity = UrlEntity(
            id = 1L, shortCode = "abc", longUrl = "https://example.com",
            createdAt = Instant.now(), clickCount = 0, passwordHash = null
        )

        val response = urlService.toUserUrlResponse(entity)

        assertFalse(response.passwordProtected)
    }

    // -- deleteUrl --

    @Test
    fun `deleteUrl removes entity and clears cache`() {
        val entity = UrlEntity(
            id = 1L, shortCode = "abc", longUrl = "https://example.com",
            createdAt = Instant.now(), clickCount = 0, userId = 42L
        )
        every { urlRepository.findByShortCode("abc") } returns entity

        urlService.deleteUrl("abc", 42L)

        verify { redisClient.delete("short:abc") }
        verify { urlRepository.delete(entity) }
    }

    @Test
    fun `deleteUrl throws AccessDeniedException when user does not own URL`() {
        val entity = UrlEntity(
            id = 1L, shortCode = "abc", longUrl = "https://example.com",
            createdAt = Instant.now(), clickCount = 0, userId = 42L
        )
        every { urlRepository.findByShortCode("abc") } returns entity

        assertThrows<AccessDeniedException> {
            urlService.deleteUrl("abc", 999L)
        }
    }

    @Test
    fun `deleteUrl throws UrlNotFoundException when shortCode not found`() {
        every { urlRepository.findByShortCode("nope") } returns null

        assertThrows<UrlNotFoundException> {
            urlService.deleteUrl("nope", 1L)
        }
    }
}
