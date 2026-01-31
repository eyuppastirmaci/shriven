package com.eyuppastirmaci.shriven.backend.analytics.dto

import java.time.LocalDate

data class StatsResponse(
    val shortCode: String,
    val totalClicks: Long,
    val dailyStats: List<DailyStat>
)

data class DailyStat(
    val date: LocalDate,
    val clicks: Long
)