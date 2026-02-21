package com.eyuppastirmaci.shriven.backend.analytics

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap

/**
 * Resolves IP addresses to country code (ISO 3166-1 alpha-2) using ip-api.com (no sign-up).
 * Results are cached in memory to stay within the free tier rate limit (45 req/min).
 * Private/localhost IPs return "Unknown" without calling the API.
 */
@Service
class GeoIpService(
    @Value("\${geoip.api-url:http://ip-api.com/json}") private val apiBaseUrl: String,
    @Value("\${geoip.cache-ttl-hours:720}") private val cacheTtlHours: Long,
    private val restTemplate: RestTemplate
) {
    companion object {
        private val logger = LoggerFactory.getLogger(GeoIpService::class.java)
        const val UNKNOWN_COUNTRY = "Unknown"
    }

    private val cache = ConcurrentHashMap<String, CachedCountry>()
    private val cacheTtlMs = cacheTtlHours * 60 * 60 * 1000

    fun getCountryCode(ipAddress: String?): String {
        if (ipAddress.isNullOrBlank()) return UNKNOWN_COUNTRY
        val ip = ipAddress.trim()
        if (isPrivateOrLocalhost(ip)) return UNKNOWN_COUNTRY

        val cached = cache[ip]
        if (cached != null && System.currentTimeMillis() < cached.expiresAt) return cached.countryCode

        return try {
            val url = "$apiBaseUrl/$ip?fields=status,countryCode,message"
            val response: ResponseEntity<JsonNode> = restTemplate.getForEntity(url, JsonNode::class.java)
            val body = response.body ?: return UNKNOWN_COUNTRY
            val status = body.path("status").asText("fail")
            if (status != "success") {
                logger.trace("GeoIP API returned status=$status for $ip: ${body.path("message").asText("")}")
                cache(ip, UNKNOWN_COUNTRY)
                return UNKNOWN_COUNTRY
            }
            val code = body.path("countryCode").asText("").take(2).uppercase()
            val country = if (code.isNotEmpty()) code else UNKNOWN_COUNTRY
            cache(ip, country)
            country
        } catch (e: Exception) {
            logger.trace("GeoIP lookup failed for $ip: ${e.message}")
            cache(ip, UNKNOWN_COUNTRY)
            UNKNOWN_COUNTRY
        }
    }

    private fun cache(ip: String, countryCode: String) {
        cache[ip] = CachedCountry(countryCode, System.currentTimeMillis() + cacheTtlMs)
    }

    private fun isPrivateOrLocalhost(ip: String): Boolean {
        return try {
            val addr = InetAddress.getByName(ip)
            addr.isLoopbackAddress || addr.isSiteLocalAddress || addr.isLinkLocalAddress
        } catch (_: Exception) {
            false
        }
    }

    private data class CachedCountry(val countryCode: String, val expiresAt: Long)
}
