package com.eyuppastirmaci.shriven.backend.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import java.time.Duration

@ConfigurationProperties(prefix = "app.cache")
data class CacheProperties @ConstructorBinding constructor(
    val ttl: Duration
)