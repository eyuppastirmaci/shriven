package com.eyuppastirmaci.shriven.backend.url

import com.eyuppastirmaci.shriven.backend.tag.TagEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
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
    var shortCode: String,

    @Column(name = "long_url", nullable = false, columnDefinition = "TEXT")
    val longUrl: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "expires_at")
    var expiresAt: Instant? = null,

    @Column(name = "click_count", nullable = false)
    var clickCount: Long = 0,

    @Column(name = "user_id")
    val userId: Long? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "is_custom_alias", nullable = false)
    var isCustomAlias: Boolean = false,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "url_tags",
        joinColumns = [JoinColumn(name = "url_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: MutableSet<TagEntity> = mutableSetOf()
)