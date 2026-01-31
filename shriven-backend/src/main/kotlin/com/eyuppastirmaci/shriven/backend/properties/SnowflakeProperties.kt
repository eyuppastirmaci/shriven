package com.eyuppastirmaci.shriven.backend.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "snowflake")
data class SnowflakeProperties @ConstructorBinding constructor(
    val nodeId: Int,
    val customEpoch: Long,
    val nodeIdBits: Int,
    val sequenceBits: Int
)