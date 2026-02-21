package com.eyuppastirmaci.shriven.backend.analytics

import com.eyuppastirmaci.shriven.backend.analytics.dto.DeviceStatItem
import com.eyuppastirmaci.shriven.backend.analytics.dto.GeoStatItem
import com.eyuppastirmaci.shriven.backend.analytics.dto.ReferrerStatItem
import com.eyuppastirmaci.shriven.backend.analytics.dto.StatsResponse
import com.eyuppastirmaci.shriven.backend.auth.AuthPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/stats")
class StatsController(
    private val analyticsService: AnalyticsService
) {

    @GetMapping("/{shortCode}")
    fun getStats(
        @PathVariable shortCode: String,
        @RequestParam(required = false) period: StatsPeriod?,
        @AuthenticationPrincipal principal: AuthPrincipal?
    ): ResponseEntity<StatsResponse> {
        val stats = analyticsService.getStats(shortCode, period, principal?.userId)
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/{shortCode}/geo")
    fun getGeoStats(
        @PathVariable shortCode: String,
        @AuthenticationPrincipal principal: AuthPrincipal?
    ): ResponseEntity<List<GeoStatItem>> {
        val list = analyticsService.getGeoStats(shortCode, principal?.userId)
        return ResponseEntity.ok(list)
    }

    @GetMapping("/{shortCode}/devices")
    fun getDeviceStats(
        @PathVariable shortCode: String,
        @AuthenticationPrincipal principal: AuthPrincipal?
    ): ResponseEntity<List<DeviceStatItem>> {
        val list = analyticsService.getDeviceStats(shortCode, principal?.userId)
        return ResponseEntity.ok(list)
    }

    @GetMapping("/{shortCode}/referrers")
    fun getReferrerStats(
        @PathVariable shortCode: String,
        @AuthenticationPrincipal principal: AuthPrincipal?
    ): ResponseEntity<List<ReferrerStatItem>> {
        val list = analyticsService.getReferrerStats(shortCode, principal?.userId)
        return ResponseEntity.ok(list)
    }
}
