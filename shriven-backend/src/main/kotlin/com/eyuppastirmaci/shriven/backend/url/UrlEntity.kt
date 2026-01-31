package com.eyuppastirmaci.shriven.backend.url

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "urls",
    indexes = [
        Index(name = "idx_short_code", columnList = "short_code"),
        Index(name = "idx_expires_at", columnList = "expires_at"),
        Index(name = "idx_created_at", columnList = "created_at")
    ]
)
data class UrlEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: Long,

    @Column(name = "short_code", nullable = false, unique = true, length = 20)
    val shortCode: String,

    @Column(name = "long_url", nullable = false, columnDefinition = "TEXT")
    val longUrl: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "expires_at")
    val expiresAt: Instant? = null,

    @Column(name = "click_count", nullable = false)
    var clickCount: Long = 0
)