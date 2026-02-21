package com.eyuppastirmaci.shriven.backend.tag

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "tags",
    indexes = [Index(name = "idx_tags_user_id", columnList = "user_id")]
)
data class TagEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: Long,

    @Column(name = "name", nullable = false, length = 50)
    val name: String,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
