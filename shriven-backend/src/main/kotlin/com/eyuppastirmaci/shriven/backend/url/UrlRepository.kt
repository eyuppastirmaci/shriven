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

    @Modifying
    @Transactional
    @Query("UPDATE UrlEntity u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    fun incrementClickCount(@Param("shortCode") shortCode: String)
}