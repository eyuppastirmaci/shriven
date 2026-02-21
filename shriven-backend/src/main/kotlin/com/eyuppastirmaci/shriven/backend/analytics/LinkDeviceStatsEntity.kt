package com.eyuppastirmaci.shriven.backend.analytics

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "link_device_stats",
    indexes = [Index(name = "idx_device_short_code", columnList = "short_code")],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_device_short_browser_os_type",
            columnNames = ["short_code", "browser", "os", "device_type"]
        )
    ]
)
data class LinkDeviceStatsEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "short_code", nullable = false, length = 20)
    val shortCode: String,

    @Column(name = "browser", nullable = false, length = 100)
    val browser: String,

    @Column(name = "os", nullable = false, length = 100)
    val os: String,

    @Column(name = "device_type", nullable = false, length = 50)
    val deviceType: String,

    @Column(name = "count", nullable = false)
    var count: Long = 0
)
