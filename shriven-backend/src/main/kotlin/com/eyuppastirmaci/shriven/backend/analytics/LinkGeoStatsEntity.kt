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
    name = "link_geo_stats",
    indexes = [Index(name = "idx_geo_short_code", columnList = "short_code")],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_geo_short_code_country", columnNames = ["short_code", "country_code"])
    ]
)
data class LinkGeoStatsEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "short_code", nullable = false, length = 20)
    val shortCode: String,

    @Column(name = "country_code", nullable = false, length = 10)
    val countryCode: String,

    @Column(name = "count", nullable = false)
    var count: Long = 0
)
