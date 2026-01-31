package com.eyuppastirmaci.shriven.backend.analytics

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface LinkStatsRepository : JpaRepository<LinkStatsEntity, Long> {

    fun findByShortCodeAndClickDate(shortCode: String, clickDate: LocalDate): LinkStatsEntity?

    @Query("SELECT SUM(s.dailyClicks) FROM LinkStatsEntity s WHERE s.shortCode = :shortCode")
    fun getTotalClicks(@Param("shortCode") shortCode: String): Long?

    fun findAllByShortCodeOrderByClickDateDesc(shortCode: String): List<LinkStatsEntity>
}