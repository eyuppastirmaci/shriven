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

    @Test
    fun `handleRequiresPassword returns 403 with PASSWORD_REQUIRED and shortCode in data`() {
        val ex = RequiresPasswordException(shortCode = "abc123")

        val response = handler.handleRequiresPassword(ex)

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals("PASSWORD_REQUIRED", response.body?.errorCode)
        @Suppress("UNCHECKED_CAST")
        val data = response.body?.data as? Map<String, String>
        assertEquals("abc123", data?.get("shortCode"))
    }

    @Test
    fun `handleInvalidLinkPassword returns 401 with INVALID_LINK_PASSWORD`() {
        val ex = InvalidLinkPasswordException("Incorrect password")

        val response = handler.handleInvalidLinkPassword(ex)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals("INVALID_LINK_PASSWORD", response.body?.errorCode)
        assertEquals("Incorrect password", response.body?.message)
    }
}
