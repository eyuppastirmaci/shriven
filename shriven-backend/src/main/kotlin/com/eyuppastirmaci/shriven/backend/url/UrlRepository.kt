package com.eyuppastirmaci.shriven.backend.url

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface UrlRepository : JpaRepository<UrlEntity, Long> {

    fun findByShortCode(shortCode: String): UrlEntity?

    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<UrlEntity>

    fun findByShortCodeAndUserId(shortCode: String, userId: Long): UrlEntity?

    fun findByLongUrlAndUserId(longUrl: String, userId: Long): UrlEntity?

    fun existsByShortCode(shortCode: String): Boolean

    @Query("SELECT u FROM UrlEntity u JOIN u.tags t WHERE u.userId = :userId AND t.id = :tagId ORDER BY u.createdAt DESC")
    fun findAllByUserIdAndTagIdOrderByCreatedAtDesc(
        @Param("userId") userId: Long,
        @Param("tagId") tagId: Long
    ): List<UrlEntity>

    @Modifying
    @Transactional
    @Query("UPDATE UrlEntity u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    fun incrementClickCount(@Param("shortCode") shortCode: String)

    @Modifying
    @Transactional
    @Query("UPDATE UrlEntity u SET u.clickCount = u.clickCount + :delta WHERE u.shortCode = :shortCode")
    fun incrementClickCountBy(
        @Param("shortCode") shortCode: String,
        @Param("delta") delta: Long
    )
}