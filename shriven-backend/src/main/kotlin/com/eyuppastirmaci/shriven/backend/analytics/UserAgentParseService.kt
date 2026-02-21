package com.eyuppastirmaci.shriven.backend.analytics

import ua_parser.Client
import ua_parser.Parser
import org.springframework.stereotype.Service

@Service
class UserAgentParseService {

    private val parser = Parser()

    fun parse(userAgent: String?): ParsedUserAgent {
        if (userAgent.isNullOrBlank()) {
            return ParsedUserAgent(browser = "Unknown", os = "Unknown", deviceType = "Unknown")
        }
        return try {
            val client: Client = parser.parse(userAgent)
            val browser = (client.userAgent?.family ?: "Unknown").take(100)
            val os = (client.os?.family ?: "Unknown").take(100)
            val deviceType = normalizeDeviceType(client.device?.family?.take(50) ?: "Unknown")
            ParsedUserAgent(browser = browser, os = os, deviceType = deviceType)
        } catch (e: Exception) {
            ParsedUserAgent(browser = "Unknown", os = "Unknown", deviceType = "Unknown")
        }
    }

    private fun normalizeDeviceType(deviceFamily: String): String {
        val lower = deviceFamily.lowercase()
        return when {
            lower.contains("mobile") && !lower.contains("tablet") -> "Mobile"
            lower.contains("tablet") || lower.contains("ipad") -> "Tablet"
            lower.contains("tv") || lower.contains("television") -> "TV"
            lower.isBlank() || lower == "unknown" || lower == "other" -> "Unknown"
            else -> "Desktop"
        }
    }
}

data class ParsedUserAgent(
    val browser: String,
    val os: String,
    val deviceType: String
)
