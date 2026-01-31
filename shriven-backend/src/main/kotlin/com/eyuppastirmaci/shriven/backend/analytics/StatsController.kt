package com.eyuppastirmaci.shriven.backend.analytics

import com.eyuppastirmaci.shriven.backend.analytics.dto.StatsResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/stats")
class StatsController(
    private val analyticsService: AnalyticsService
) {

    @GetMapping("/{shortCode}")
    fun getStats(@PathVariable shortCode: String): ResponseEntity<StatsResponse> {
        val stats = analyticsService.getStats(shortCode)
        return ResponseEntity.ok(stats)
    }
}