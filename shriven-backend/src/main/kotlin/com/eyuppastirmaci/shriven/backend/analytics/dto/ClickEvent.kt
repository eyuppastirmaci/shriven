package com.eyuppastirmaci.shriven.backend.analytics.dto

import java.time.Instant

data class ClickEvent(
    val shortCode: String,
    val timestamp: Instant,
    val userAgent: String?,
    val ipAddress: String?
)