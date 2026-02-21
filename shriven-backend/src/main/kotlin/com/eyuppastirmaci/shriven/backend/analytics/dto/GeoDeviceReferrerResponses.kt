package com.eyuppastirmaci.shriven.backend.analytics.dto

data class GeoStatItem(
    val countryCode: String,
    val countryName: String?,
    val count: Long
)

data class DeviceStatItem(
    val browser: String,
    val os: String,
    val deviceType: String,
    val count: Long
)

data class ReferrerStatItem(
    val referrerDomain: String,
    val count: Long
)
