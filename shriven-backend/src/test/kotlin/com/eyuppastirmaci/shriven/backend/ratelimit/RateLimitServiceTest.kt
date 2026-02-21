package com.eyuppastirmaci.shriven.backend.ratelimit

import com.eyuppastirmaci.shriven.backend.redis.RedisClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RateLimitServiceTest {

    private val redisClient = mockk<RedisClient>(relaxed = true)
    private val rateLimitService = RateLimitService(redisClient)

    @Test
    fun `checkAndIncrement returns allowed when under limit`() {
        every { redisClient.increment("rate_limit:global:192.168.1.1") } returns 1L
        every { redisClient.expire("rate_limit:global:192.168.1.1", 60L) } returns true

        val result = rateLimitService.checkAndIncrement(
            keyPrefix = "global",
            ip = "192.168.1.1",
            limit = 200,
            windowSeconds = 60L
        )

        assertTrue(result.allowed)
        assertNull(result.retryAfterSeconds)
        verify { redisClient.increment("rate_limit:global:192.168.1.1") }
        verify { redisClient.expire("rate_limit:global:192.168.1.1", 60L) }
    }

    @Test
    fun `checkAndIncrement returns not allowed when over limit and sets retryAfterSeconds`() {
        every { redisClient.increment("rate_limit:shorten:10.0.0.1") } returns 11L
        every { redisClient.getExpireSeconds("rate_limit:shorten:10.0.0.1") } returns 45L

        val result = rateLimitService.checkAndIncrement(
            keyPrefix = "shorten",
            ip = "10.0.0.1",
            limit = 10,
            windowSeconds = 60L
        )

        assertEquals(false, result.allowed)
        assertEquals(45, result.retryAfterSeconds)
        verify { redisClient.increment("rate_limit:shorten:10.0.0.1") }
    }

    @Test
    fun `checkAndIncrement uses windowSeconds for retryAfter when getExpireSeconds returns null`() {
        every { redisClient.increment("rate_limit:redirect:127.0.0.1") } returns 121L
        every { redisClient.getExpireSeconds("rate_limit:redirect:127.0.0.1") } returns null

        val result = rateLimitService.checkAndIncrement(
            keyPrefix = "redirect",
            ip = "127.0.0.1",
            limit = 120,
            windowSeconds = 60L
        )

        assertEquals(false, result.allowed)
        assertEquals(60, result.retryAfterSeconds)
    }

    @Test
    fun `checkAndIncrement fails open when Redis increment returns null`() {
        every { redisClient.increment(any()) } returns null

        val result = rateLimitService.checkAndIncrement(
            keyPrefix = "global",
            ip = "1.2.3.4",
            limit = 200,
            windowSeconds = 60L
        )

        assertTrue(result.allowed)
        assertNull(result.retryAfterSeconds)
        verify(exactly = 1) { redisClient.increment(any()) }
        verify(exactly = 0) { redisClient.expire(any(), any()) }
    }

    @Test
    fun `checkAndIncrement does not call expire when count is greater than 1`() {
        every { redisClient.increment("rate_limit:global:1.1.1.1") } returns 5L

        rateLimitService.checkAndIncrement(
            keyPrefix = "global",
            ip = "1.1.1.1",
            limit = 200,
            windowSeconds = 60L
        )

        verify(exactly = 0) { redisClient.expire(any(), any()) }
    }
}
