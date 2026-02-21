package com.eyuppastirmaci.shriven.backend.aop

import com.eyuppastirmaci.shriven.backend.analytics.dto.ClickEvent
import com.eyuppastirmaci.shriven.backend.kafka.KafkaClient
import com.eyuppastirmaci.shriven.backend.ratelimit.ClientIpResolver
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import java.time.Instant

@Aspect
@Component
class RedirectClickPublishAspect(
    private val kafkaClient: KafkaClient,
    private val clientIpResolver: ClientIpResolver
) {

    @AfterReturning(
        pointcut = "execution(* com.eyuppastirmaci.shriven.backend.url.UrlController.redirect(..))",
        returning = "result"
    )
    fun publishClickAfterRedirect(joinPoint: JoinPoint, result: Any?) {
        val args = joinPoint.args
        if (args.size < 2) return
        val shortCode = args[0] as? String ?: return
        val request = args[1] as? HttpServletRequest ?: return

        val ip = clientIpResolver.resolve(request)
        val userAgent = request.getHeader("User-Agent")?.takeIf { it.isNotBlank() }
        val referrer = request.getHeader("Referer")?.takeIf { it.isNotBlank() }

        val event = ClickEvent(
            shortCode = shortCode,
            timestamp = Instant.now(),
            userAgent = userAgent,
            ipAddress = ip,
            referrer = referrer
        )
        kafkaClient.sendClickEvent(event)
    }
}
