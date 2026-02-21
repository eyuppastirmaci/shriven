package com.eyuppastirmaci.shriven.backend.exception

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleRateLimitExceeded returns 429 with Retry-After header when retryAfterSeconds set`() {
        val ex = RateLimitExceededException(retryAfterSeconds = 42)

        val response = handler.handleRateLimitExceeded(ex)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.statusCode)
        assertEquals("42", response.headers["Retry-After"]?.firstOrNull())
        assertEquals("Rate limit exceeded", response.body?.message)
        assertEquals("RATE_LIMIT_EXCEEDED", response.body?.errorCode)
    }

    @Test
    fun `handleRateLimitExceeded returns 429 without Retry-After when retryAfterSeconds null`() {
        val ex = RateLimitExceededException(retryAfterSeconds = null)

        val response = handler.handleRateLimitExceeded(ex)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.statusCode)
        assertNull(response.headers["Retry-After"]?.firstOrNull())
        assertEquals("RATE_LIMIT_EXCEEDED", response.body?.errorCode)
    }
}
