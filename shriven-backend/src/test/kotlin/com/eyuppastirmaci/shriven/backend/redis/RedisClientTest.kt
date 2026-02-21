package com.eyuppastirmaci.shriven.backend.redis

import com.eyuppastirmaci.shriven.backend.exception.RedisOperationException
import com.eyuppastirmaci.shriven.backend.properties.CacheProperties
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RedisClientTest {

    private val valueOps = mockk<ValueOperations<String, String>>(relaxed = true)
    private val redisTemplate = mockk<StringRedisTemplate> {
        every { opsForValue() } returns valueOps
        every { delete(any<String>()) } returns true
    }
    private val cacheProperties = CacheProperties(ttl = Duration.ofHours(24))

    private val redisClient = RedisClient(redisTemplate, cacheProperties)

    @Test
    fun `getUrl returns cached value on hit`() {
        every { valueOps.get("short:abc123") } returns "https://example.com"

        val result = redisClient.getUrl("abc123")

        assertEquals("https://example.com", result)
    }

    @Test
    fun `getUrl returns null on cache miss`() {
        every { valueOps.get("short:abc123") } returns null

        val result = redisClient.getUrl("abc123")

        assertNull(result)
    }

    @Test
    fun `getUrl returns null when Redis is unavailable`() {
        every { valueOps.get("short:abc123") } throws RuntimeException("Connection refused")

        val result = redisClient.getUrl("abc123")

        assertNull(result)
    }

    @Test
    fun `saveUrl stores value with TTL`() {
        redisClient.saveUrl("abc123", "https://example.com")

        verify {
            valueOps.set(
                "short:abc123",
                "https://example.com",
                Duration.ofHours(24).toMillis(),
                TimeUnit.MILLISECONDS
            )
        }
    }

    @Test
    fun `saveUrl throws RedisOperationException on failure`() {
        every { valueOps.set(any(), any(), any(), any()) } throws RuntimeException("Connection refused")

        assertThrows<RedisOperationException> {
            redisClient.saveUrl("abc123", "https://example.com")
        }
    }

    @Test
    fun `deleteUrl removes key from Redis`() {
        redisClient.deleteUrl("abc123")

        verify { redisTemplate.delete("short:abc123") }
    }

    @Test
    fun `deleteUrl throws RedisOperationException on failure`() {
        every { redisTemplate.delete(any<String>()) } throws RuntimeException("Connection refused")

        assertThrows<RedisOperationException> {
            redisClient.deleteUrl("abc123")
        }
    }
}
