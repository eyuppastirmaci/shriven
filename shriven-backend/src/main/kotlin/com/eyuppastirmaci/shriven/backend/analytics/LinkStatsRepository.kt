package com.eyuppastirmaci.shriven.backend.analytics

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Repository
interface LinkStatsRepository : JpaRepository<LinkStatsEntity, Long> {

    fun findByShortCodeAndClickDate(shortCode: String, clickDate: LocalDate): LinkStatsEntity?

    @Query("SELECT SUM(s.dailyClicks) FROM LinkStatsEntity s WHERE s.shortCode = :shortCode")
    fun getTotalClicks(@Param("shortCode") shortCode: String): Long?

    fun findAllByShortCodeOrderByClickDateDesc(shortCode: String): List<LinkStatsEntity>

    fun findAllByShortCodeAndClickDateBetweenOrderByClickDateAsc(
        shortCode: String,
        from: LocalDate,
        to: LocalDate
    ): List<LinkStatsEntity>

    @Modifying
    @Transactional
    @Query(
        value = """
            INSERT INTO link_stats (short_code, click_date, daily_clicks)
            VALUES (:shortCode, :clickDate, :count)
            ON CONFLICT (short_code, click_date)
            DO UPDATE SET daily_clicks = link_stats.daily_clicks + :count
        """,
        nativeQuery = true
    )
    fun upsertDailyClicks(
        @Param("shortCode") shortCode: String,
        @Param("clickDate") clickDate: LocalDate,
        @Param("count") count: Long
    )
}