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
    name = "link_referrer_stats",
    indexes = [Index(name = "idx_referrer_short_code", columnList = "short_code")],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_referrer_short_domain", columnNames = ["short_code", "referrer_domain"])
    ]
)
data class LinkReferrerStatsEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "short_code", nullable = false, length = 20)
    val shortCode: String,

    @Column(name = "referrer_domain", nullable = false, length = 255)
    val referrerDomain: String,

    @Column(name = "count", nullable = false)
    var count: Long = 0
)
