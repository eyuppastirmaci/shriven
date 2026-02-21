package com.eyuppastirmaci.shriven.backend.ratelimit

import com.eyuppastirmaci.shriven.backend.exception.RateLimitExceededException
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class RateLimitAspect(
    private val rateLimitService: RateLimitService,
    private val clientIpResolver: ClientIpResolver
) {

    @Around("@annotation(rateLimited)")
    @Throws(Throwable::class)
    fun aroundRateLimited(joinPoint: ProceedingJoinPoint, rateLimited: RateLimited): Any? {
        val request = findRequest(joinPoint) ?: return joinPoint.proceed()

        val ip = clientIpResolver.resolve(request)
        if (ip == null) return joinPoint.proceed()

        val result = rateLimitService.checkAndIncrement(
            keyPrefix = rateLimited.keyPrefix,
            ip = ip,
            limit = rateLimited.requestsPerMinute,
            windowSeconds = 60L
        )

        if (!result.allowed) {
            throw RateLimitExceededException(
                retryAfterSeconds = result.retryAfterSeconds
            )
        }

        return joinPoint.proceed()
    }

    private fun findRequest(joinPoint: ProceedingJoinPoint): HttpServletRequest? {
        val args = joinPoint.args
        for (arg in args) {
            if (arg is HttpServletRequest) return arg
        }
        return null
    }
}
