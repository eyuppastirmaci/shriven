package com.eyuppastirmaci.shriven.backend.analytics

import com.eyuppastirmaci.shriven.backend.analytics.dto.DailyStat
import com.eyuppastirmaci.shriven.backend.analytics.dto.StatsResponse
import com.eyuppastirmaci.shriven.backend.exception.UrlNotFoundException
import com.eyuppastirmaci.shriven.backend.url.UrlRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AnalyticsService(
    private val linkStatsRepository: LinkStatsRepository,
    private val urlRepository: UrlRepository
) {

    @Transactional(readOnly = true)
    fun getStats(shortCode: String): StatsResponse {
        // Verify link exists
        val urlEntity = urlRepository.findByShortCode(shortCode)
            ?: throw UrlNotFoundException("Short code not found: $shortCode")

        // Fetch historical data
        val history = linkStatsRepository.findAllByShortCodeOrderByClickDateDesc(shortCode)

        // Map to DTO
        val dailyStats = history.map {
            DailyStat(it.clickDate, it.dailyClicks)
        }

        return StatsResponse(
            shortCode = shortCode,
            totalClicks = urlEntity.clickCount, // Uses the eventually consistent total
            dailyStats = dailyStats
        )
    }
}