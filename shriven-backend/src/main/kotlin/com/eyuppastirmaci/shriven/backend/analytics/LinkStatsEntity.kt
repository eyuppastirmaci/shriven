package com.eyuppastirmaci.shriven.backend.analytics

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

@Entity
@Table(
    name = "link_stats",
    indexes = [
        Index(name = "idx_stats_short_code", columnList = "short_code"),
        Index(name = "idx_stats_date", columnList = "click_date")
    ],
    uniqueConstraints = [
        // Ensure only one row per link per day
        UniqueConstraint(name = "uk_short_code_date", columnNames = ["short_code", "click_date"])
    ]
)
data class LinkStatsEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "short_code", nullable = false, length = 20)
    val shortCode: String,

    @Column(name = "click_date", nullable = false)
    val clickDate: LocalDate,

    @Column(name = "daily_clicks", nullable = false)
    var dailyClicks: Long = 0
)