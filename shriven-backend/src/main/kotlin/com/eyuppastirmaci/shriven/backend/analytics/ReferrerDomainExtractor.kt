package com.eyuppastirmaci.shriven.backend.analytics

import org.springframework.stereotype.Component
import java.net.URI

@Component
class ReferrerDomainExtractor {

    private val maxDomainLength = 255

    /**
     * Extracts a normalized domain from the Referer URL, or "direct" when null/empty/invalid.
     */
    fun extractDomain(referrer: String?): String {
        if (referrer.isNullOrBlank()) return "direct"
        return try {
            val uri = URI(referrer.trim())
            val host = uri.host ?: return "direct"
            var domain = host.lowercase()
            if (domain.startsWith("www.")) {
                domain = domain.removePrefix("www.")
            }
            domain.take(maxDomainLength)
        } catch (e: Exception) {
            "direct"
        }
    }
}
