package com.eyuppastirmaci.shriven.backend.snowflake

data class SnowflakeIdComponents(
    val id: Long,
    val timestamp: Long,
    val nodeId: Int,
    val sequence: Int
)