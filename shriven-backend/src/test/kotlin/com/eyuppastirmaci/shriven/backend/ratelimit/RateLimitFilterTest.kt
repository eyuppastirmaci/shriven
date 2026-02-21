package com.eyuppastirmaci.shriven.backend.ratelimit

import com.eyuppastirmaci.shriven.backend.properties.RateLimitProperties
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateLimitFilterTest {

    private val objectMapper = ObjectMapper()
    private val rateLimitService = mockk<RateLimitService>()
    private val clientIpResolver = mockk<ClientIpResolver>()
    private val rateLimitProperties = RateLimitProperties(
        globalRequestsPerMinute = 200,
        windowSeconds = 60L,
        whitelistPaths = listOf("/actuator/health", "/actuator/info")
    )

    private val filter = RateLimitFilter(
        rateLimitService,
        rateLimitProperties,
        clientIpResolver,
        objectMapper
    )

    @Test
    fun `doFilterInternal passes through for whitelisted path without checking rate limit`() {
        val request = MockHttpServletRequest().apply { requestURI = "/actuator/health" }
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        filter.doFilter(request, response, chain)

        verify(exactly = 1) { chain.doFilter(request, response) }
        verify(exactly = 0) { rateLimitService.checkAndIncrement(any(), any(), any(), any()) }
    }

    @Test
    fun `doFilterInternal passes through when IP cannot be resolved`() {
        val request = MockHttpServletRequest().apply { requestURI = "/api/shorten" }
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)
        every { clientIpResolver.resolve(request) } returns null

        filter.doFilter(request, response, chain)

        verify(exactly = 1) { chain.doFilter(request, response) }
        verify(exactly = 0) { rateLimitService.checkAndIncrement(any(), any(), any(), any()) }
    }

    @Test
    fun `doFilterInternal returns 429 with body and Retry-After when over limit`() {
        val request = MockHttpServletRequest().apply { requestURI = "/api/shorten" }
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)
        every { clientIpResolver.resolve(request) } returns "10.0.0.1"
        every {
            rateLimitService.checkAndIncrement("global", "10.0.0.1", 200, 60L)
        } returns RateLimitResult(allowed = false, retryAfterSeconds = 30)

        filter.doFilter(request, response, chain)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.status)
        assertEquals("30", response.getHeader("Retry-After"))
        assertTrue(response.contentAsString.contains("RATE_LIMIT_EXCEEDED"))
        assertTrue(response.contentAsString.contains("Rate limit exceeded"))
        verify(exactly = 0) { chain.doFilter(any(), any()) }
    }

    @Test
    fun `doFilterInternal calls chain when under limit`() {
        val request = MockHttpServletRequest().apply { requestURI = "/api/urls" }
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)
        every { clientIpResolver.resolve(request) } returns "192.168.1.1"
        every {
            rateLimitService.checkAndIncrement("global", "192.168.1.1", 200, 60L)
        } returns RateLimitResult(allowed = true)

        filter.doFilter(request, response, chain)

        verify(exactly = 1) { chain.doFilter(request, response) }
        assertEquals(200, response.status)
    }
}
