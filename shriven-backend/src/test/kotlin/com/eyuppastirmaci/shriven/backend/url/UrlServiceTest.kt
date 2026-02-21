package com.eyuppastirmaci.shriven.backend.url

import com.eyuppastirmaci.shriven.backend.analytics.dto.ClickEvent
import com.eyuppastirmaci.shriven.backend.exception.AccessDeniedException
import com.eyuppastirmaci.shriven.backend.exception.RedisOperationException
import com.eyuppastirmaci.shriven.backend.exception.UrlExpiredException
import com.eyuppastirmaci.shriven.backend.exception.UrlNotFoundException
import com.eyuppastirmaci.shriven.backend.kafka.KafkaClient
import com.eyuppastirmaci.shriven.backend.redis.RedisClient
import com.eyuppastirmaci.shriven.backend.snowflake.Base62Encoder
import com.eyuppastirmaci.shriven.backend.snowflake.SnowflakeIdGenerator
import com.eyuppastirmaci.shriven.backend.url.dto.request.ShortenUrlRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import kotlin.test.assertEquals

class UrlServiceTest {

    private val urlRepository = mockk<UrlRepository>(relaxed = true)
    private val snowflakeIdGenerator = mockk<SnowflakeIdGenerator>()
    private val base62Encoder = mockk<Base62Encoder>()
    private val redisClient = mockk<RedisClient>(relaxed = true)
    private val kafkaClient = mockk<KafkaClient>(relaxed = true)

    private val urlService = UrlService(
        urlRepository, snowflakeIdGenerator, base62Encoder, redisClient, kafkaClient
    )

    // -- shortenUrl --

    @Test
    fun `shortenUrl creates entity and caches it`() {
        every { snowflakeIdGenerator.nextId() } returns 12345L
        every { base62Encoder.encode(12345L) } returns "abc"
        val request = ShortenUrlRequest(longUrl = "https://example.com")
        val savedEntity = UrlEntity(
            id = 12345L, shortCode = "abc", longUrl = "https://example.com",
            createdAt = Instant.now(), clickCount = 0
        )
        every { urlRepository.save(any()) } returns savedEntity

        val result = urlService.shortenUrl(request, userId = 1L)

        assertEquals("abc", result.shortCode)
        assertEquals("https://example.com", result.longUrl)
        verify { urlRepository.save(any()) }
        verify { redisClient.saveUrl("abc", "https://example.com") }
    }

    // -- getLongUrl --

    @Test
    fun `getLongUrl returns cached URL on cache hit`() {
        every { redisClient.getUrl("abc") } returns "https://example.com"

        val result = urlService.getLongUrl("abc", "Mozilla/5.0", "127.0.0.1")

        assertEquals("https://example.com", result)
        verify { kafkaClient.sendClickEvent(any()) }
        verify(exactly = 0) { urlRepository.findByShortCode(any()) }
    }

    @Test
    fun `getLongUrl falls back to DB on cache miss and populates cache`() {
        every { redisClient.getUrl("abc") } returns null
        val entity = UrlEntity(
            id = 1L, shortCode = "abc", longUrl = "https://example.com",
            createdAt = Instant.now(), clickCount = 0
        )
        every { urlRepository.findByShortCode("abc") } returns entity

        val result = urlService.getLongUrl("abc", "Mozilla/5.0", "127.0.0.1")

        assertEquals("https://example.com", result)
        verify { redisClient.saveUrl("abc", "https://example.com") }
        verify { kafkaClient.sendClickEvent(any()) }
    }

    @Test
    fun `getLongUrl throws UrlNotFoundException when shortCode not in DB`() {
        every { redisClient.getUrl("nope") } returns null
        every { urlRepository.findByShortCode("nope") } returns null

        assertThrows<UrlNotFoundException> {
            urlService.getLongUrl("nope", null, null)
        }
    }

    @Test
    fun `getLongUrl throws UrlExpiredException for expired URL`() {
        every { redisClient.getUrl("exp") } returns null
        val entity = UrlEntity(
            id = 1L, shortCode = "exp", longUrl = "https://expired.com",
            createdAt = Instant.now(), expiresAt = Instant.parse("2020-01-01T00:00:00Z"), clickCount = 0
        )
        every { urlRepository.findByShortCode("exp") } returns entity

        assertThrows<UrlExpiredException> {
            urlService.getLongUrl("exp", null, null)
        }
    }

    @Test
    fun `getLongUrl still works when Redis fails on cache populate`() {
        every { redisClient.getUrl("abc") } returns null
        val entity = UrlEntity(
            id = 1L, shortCode = "abc", longUrl = "https://example.com",
            createdAt = Instant.now(), clickCount = 0
        )
        every { urlRepository.findByShortCode("abc") } returns entity
        every { redisClient.saveUrl(any(), any()) } throws RedisOperationException("Connection refused")

        val result = urlService.getLongUrl("abc", null, null)

        assertEquals("https://example.com", result)
        verify { kafkaClient.sendClickEvent(any()) }
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

        verify { redisClient.deleteUrl("abc") }
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
