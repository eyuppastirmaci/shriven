package com.eyuppastirmaci.shriven.backend.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "base62")
data class Base62Properties @ConstructorBinding constructor(
    val charset: String,
    val base: Int
)