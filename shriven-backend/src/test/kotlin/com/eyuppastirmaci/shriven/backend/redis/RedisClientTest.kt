package com.eyuppastirmaci.shriven.backend.redis

import com.eyuppastirmaci.shriven.backend.exception.RedisOperationException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RedisClientTest {

    private val valueOps = mockk<ValueOperations<String, String>>(relaxed = true)
    private val redisTemplate = mockk<StringRedisTemplate> {
        every { opsForValue() } returns valueOps
        every { delete(any<String>()) } returns true
    }

    private val redisClient = RedisClient(redisTemplate)

    @Test
    fun `get returns cached value on hit`() {
        every { valueOps.get("short:abc123") } returns "https://example.com"

        val result = redisClient.get("short:abc123")

        assertEquals("https://example.com", result)
    }

    @Test
    fun `get returns null on cache miss`() {
        every { valueOps.get("short:abc123") } returns null

        val result = redisClient.get("short:abc123")

        assertNull(result)
    }

    @Test
    fun `get returns null when Redis is unavailable`() {
        every { valueOps.get(any<String>()) } throws RuntimeException("Connection refused")

        val result = redisClient.get("short:abc123")

        assertNull(result)
    }

    @Test
    fun `set stores value with TTL`() {
        redisClient.set("short:abc123", "https://example.com", 86400000L)

        verify {
            valueOps.set("short:abc123", "https://example.com", 86400000L, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun `set throws RedisOperationException on failure`() {
        every { valueOps.set(any(), any(), any(), any()) } throws RuntimeException("Connection refused")

        assertThrows<RedisOperationException> {
            redisClient.set("short:abc123", "https://example.com", 86400000L)
        }
    }

    @Test
    fun `delete removes key from Redis`() {
        redisClient.delete("short:abc123")

        verify { redisTemplate.delete("short:abc123") }
    }

    @Test
    fun `delete throws RedisOperationException on failure`() {
        every { redisTemplate.delete(any<String>()) } throws RuntimeException("Connection refused")

        assertThrows<RedisOperationException> {
            redisClient.delete("short:abc123")
        }
    }
}
