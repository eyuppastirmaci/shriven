package com.eyuppastirmaci.shriven.backend.analytics

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface LinkGeoStatsRepository : JpaRepository<LinkGeoStatsEntity, Long> {

    fun findAllByShortCodeOrderByCountDesc(shortCode: String): List<LinkGeoStatsEntity>

    @Modifying
    @Transactional
    @Query(
        value = """
            INSERT INTO link_geo_stats (short_code, country_code, count)
            VALUES (:shortCode, :countryCode, :delta)
            ON CONFLICT (short_code, country_code)
            DO UPDATE SET count = link_geo_stats.count + :delta
        """,
        nativeQuery = true
    )
    fun upsertIncrement(
        @Param("shortCode") shortCode: String,
        @Param("countryCode") countryCode: String,
        @Param("delta") delta: Long
    )
}
