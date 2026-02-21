package com.eyuppastirmaci.shriven.backend.analytics

import com.eyuppastirmaci.shriven.backend.analytics.dto.DailyStat
import com.eyuppastirmaci.shriven.backend.analytics.dto.StatsResponse
import com.eyuppastirmaci.shriven.backend.analytics.dto.WeeklyStat
import com.eyuppastirmaci.shriven.backend.exception.AccessDeniedException
import com.eyuppastirmaci.shriven.backend.exception.UrlNotFoundException
import com.eyuppastirmaci.shriven.backend.url.UrlRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class AnalyticsService(
    private val linkStatsRepository: LinkStatsRepository,
    private val urlRepository: UrlRepository
) {

    @Transactional(readOnly = true)
    fun getStats(shortCode: String, period: StatsPeriod?, requestingUserId: Long?): StatsResponse {
        val urlEntity = urlRepository.findByShortCode(shortCode)
            ?: throw UrlNotFoundException("Short code not found: $shortCode")

        // If the link is owned by a user, only that user may view stats
        if (urlEntity.userId != null && urlEntity.userId != requestingUserId) {
            throw AccessDeniedException("You do not have permission to view stats for this link")
        }

        val today = LocalDate.now(ZoneOffset.UTC)

        val history = when (period) {
            StatsPeriod.DAILY -> linkStatsRepository.findAllByShortCodeAndClickDateBetweenOrderByClickDateAsc(
                shortCode, today.minusDays(6), today
            )
            StatsPeriod.WEEKLY -> linkStatsRepository.findAllByShortCodeAndClickDateBetweenOrderByClickDateAsc(
                shortCode, today.minusWeeks(3).with(DayOfWeek.MONDAY), today
            )
            null -> linkStatsRepository.findAllByShortCodeOrderByClickDateDesc(shortCode)
        }

        val dailyStats = history.map { DailyStat(it.clickDate, it.dailyClicks) }

        val weeklyStats = history
            .groupBy { it.clickDate.with(DayOfWeek.MONDAY) }
            .map { (weekStart, rows) -> WeeklyStat(weekStart, rows.sumOf { it.dailyClicks }) }
            .sortedByDescending { it.weekStart }

        return StatsResponse(
            shortCode = shortCode,
            totalClicks = urlEntity.clickCount,
            dailyStats = dailyStats,
            weeklyStats = weeklyStats
        )
    }
}
