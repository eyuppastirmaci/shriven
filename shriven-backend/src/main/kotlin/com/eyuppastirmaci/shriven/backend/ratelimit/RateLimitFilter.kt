package com.eyuppastirmaci.shriven.backend.ratelimit

import com.eyuppastirmaci.shriven.backend.exception.ErrorResponse
import com.eyuppastirmaci.shriven.backend.properties.RateLimitProperties
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RateLimitFilter(
    private val rateLimitService: RateLimitService,
    private val rateLimitProperties: RateLimitProperties,
    private val clientIpResolver: ClientIpResolver,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter(), Ordered {

    // Lower order so this filter runs first, applying rate limits before any auth or business logic.
    override fun getOrder(): Int = -200

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (rateLimitProperties.whitelistPaths.any { path -> request.requestURI.equals(path, ignoreCase = true) }) {
            filterChain.doFilter(request, response)
            return
        }

        val ip = clientIpResolver.resolve(request)
        if (ip == null) {
            filterChain.doFilter(request, response)
            return
        }

        val result = rateLimitService.checkAndIncrement(
            keyPrefix = "global",
            ip = ip,
            limit = rateLimitProperties.globalRequestsPerMinute,
            windowSeconds = rateLimitProperties.windowSeconds
        )

        if (!result.allowed) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            result.retryAfterSeconds?.let { response.setHeader("Retry-After", it.toString()) }
            val errorResponse = ErrorResponse(
                message = "Rate limit exceeded",
                errorCode = "RATE_LIMIT_EXCEEDED"
            )
            objectMapper.writeValue(response.outputStream, errorResponse)
            return
        }

        filterChain.doFilter(request, response)
    }
}
