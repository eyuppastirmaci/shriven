package com.eyuppastirmaci.shriven.backend.ratelimit

import com.eyuppastirmaci.shriven.backend.exception.RateLimitExceededException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class RateLimitAspectTest {

    private val rateLimitService = mockk<RateLimitService>()
    private val clientIpResolver = mockk<ClientIpResolver>()
    private val aspect = RateLimitAspect(rateLimitService, clientIpResolver)

    private fun rateLimited(requestsPerMinute: Int = 10, keyPrefix: String = "shorten") =
        RateLimited(requestsPerMinute = requestsPerMinute, keyPrefix = keyPrefix)

    @Test
    fun `aroundRateLimited proceeds when no HttpServletRequest in args`() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        every { joinPoint.args } returns arrayOf("code", "body")
        every { joinPoint.proceed() } returns "result"

        val result = aspect.aroundRateLimited(joinPoint, rateLimited())

        assertEquals("result", result)
        verify(exactly = 1) { joinPoint.proceed() }
        verify(exactly = 0) { rateLimitService.checkAndIncrement(any(), any(), any(), any()) }
    }

    @Test
    fun `aroundRateLimited proceeds when IP is null`() {
        val request = mockk<HttpServletRequest>()
        val joinPoint = mockk<ProceedingJoinPoint>()
        every { joinPoint.args } returns arrayOf("abc", request)
        every { clientIpResolver.resolve(request) } returns null
        every { joinPoint.proceed() } returns Unit

        aspect.aroundRateLimited(joinPoint, rateLimited())

        verify(exactly = 1) { joinPoint.proceed() }
        verify(exactly = 0) { rateLimitService.checkAndIncrement(any(), any(), any(), any()) }
    }

    @Test
    fun `aroundRateLimited throws RateLimitExceededException when over limit`() {
        val request = mockk<HttpServletRequest>()
        val joinPoint = mockk<ProceedingJoinPoint>()
        every { joinPoint.args } returns arrayOf("abc", request)
        every { clientIpResolver.resolve(request) } returns "1.2.3.4"
        every {
            rateLimitService.checkAndIncrement("shorten", "1.2.3.4", 10, 60L)
        } returns RateLimitResult(allowed = false, retryAfterSeconds = 45)

        val ex = assertThrows<RateLimitExceededException> {
            aspect.aroundRateLimited(joinPoint, rateLimited())
        }

        assertEquals(45, ex.retryAfterSeconds)
        verify(exactly = 0) { joinPoint.proceed() }
    }

    @Test
    fun `aroundRateLimited proceeds and returns result when under limit`() {
        val request = mockk<HttpServletRequest>()
        val joinPoint = mockk<ProceedingJoinPoint>()
        every { joinPoint.args } returns arrayOf("xyz", request)
        every { clientIpResolver.resolve(request) } returns "10.0.0.1"
        every {
            rateLimitService.checkAndIncrement("redirect", "10.0.0.1", 120, 60L)
        } returns RateLimitResult(allowed = true)
        every { joinPoint.proceed() } returns "redirect-result"

        val result = aspect.aroundRateLimited(
            joinPoint,
            rateLimited(requestsPerMinute = 120, keyPrefix = "redirect")
        )

        assertEquals("redirect-result", result)
        verify(exactly = 1) { joinPoint.proceed() }
        verify(exactly = 1) {
            rateLimitService.checkAndIncrement("redirect", "10.0.0.1", 120, 60L)
        }
    }
}
