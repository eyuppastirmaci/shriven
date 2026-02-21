package com.eyuppastirmaci.shriven.backend.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "app.rate-limit")
data class RateLimitProperties @ConstructorBinding constructor(
    val globalRequestsPerMinute: Int,
    val windowSeconds: Long,
    val whitelistPaths: List<String>
)
