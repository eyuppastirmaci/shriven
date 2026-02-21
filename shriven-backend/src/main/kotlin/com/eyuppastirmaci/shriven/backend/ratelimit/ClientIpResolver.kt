package com.eyuppastirmaci.shriven.backend.ratelimit

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

@Component
class ClientIpResolver {

    /**
     * Resolves client IP from request, considering X-Forwarded-For, X-Real-IP, and remoteAddr.
     */
    fun resolve(request: HttpServletRequest): String? {
        val forwarded = request.getHeader("X-Forwarded-For")
        if (!forwarded.isNullOrBlank()) {
            return forwarded.split(",").firstOrNull()?.trim()?.takeIf { it.isNotBlank() }
        }
        val realIp = request.getHeader("X-Real-IP")
        if (!realIp.isNullOrBlank()) return realIp.trim()
        return request.remoteAddr?.takeIf { it.isNotBlank() }
    }
}
