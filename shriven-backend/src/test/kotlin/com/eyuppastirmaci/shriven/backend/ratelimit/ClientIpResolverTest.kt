package com.eyuppastirmaci.shriven.backend.ratelimit

import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ClientIpResolverTest {

    private val resolver = ClientIpResolver()

    @Test
    fun `resolve returns first token of X-Forwarded-For when present`() {
        val request = mockk<HttpServletRequest>()
        every { request.getHeader("X-Forwarded-For") } returns " 203.0.113.195 , 70.41.3.18 "
        every { request.getHeader("X-Real-IP") } returns "other"
        every { request.remoteAddr } returns "10.0.0.1"

        val ip = resolver.resolve(request)

        assertEquals("203.0.113.195", ip)
    }

    @Test
    fun `resolve returns X-Real-IP when X-Forwarded-For is blank`() {
        val request = mockk<HttpServletRequest>()
        every { request.getHeader("X-Forwarded-For") } returns " "
        every { request.getHeader("X-Real-IP") } returns " 192.168.1.100 "
        every { request.remoteAddr } returns "10.0.0.1"

        val ip = resolver.resolve(request)

        assertEquals("192.168.1.100", ip)
    }

    @Test
    fun `resolve returns remoteAddr when no proxy headers`() {
        val request = mockk<HttpServletRequest>()
        every { request.getHeader("X-Forwarded-For") } returns null
        every { request.getHeader("X-Real-IP") } returns null
        every { request.remoteAddr } returns "127.0.0.1"

        val ip = resolver.resolve(request)

        assertEquals("127.0.0.1", ip)
    }

    @Test
    fun `resolve returns null when all sources are blank or null`() {
        val request = mockk<HttpServletRequest>()
        every { request.getHeader("X-Forwarded-For") } returns null
        every { request.getHeader("X-Real-IP") } returns ""
        every { request.remoteAddr } returns null

        val ip = resolver.resolve(request)

        assertNull(ip)
    }
}
