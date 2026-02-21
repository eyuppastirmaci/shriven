package com.eyuppastirmaci.shriven.backend.analytics

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface LinkDeviceStatsRepository : JpaRepository<LinkDeviceStatsEntity, Long> {

    fun findAllByShortCodeOrderByCountDesc(shortCode: String): List<LinkDeviceStatsEntity>

    @Modifying
    @Transactional
    @Query(
        value = """
            INSERT INTO link_device_stats (short_code, browser, os, device_type, count)
            VALUES (:shortCode, :browser, :os, :deviceType, :delta)
            ON CONFLICT (short_code, browser, os, device_type)
            DO UPDATE SET count = link_device_stats.count + :delta
        """,
        nativeQuery = true
    )
    fun upsertIncrement(
        @Param("shortCode") shortCode: String,
        @Param("browser") browser: String,
        @Param("os") os: String,
        @Param("deviceType") deviceType: String,
        @Param("delta") delta: Long
    )
}
