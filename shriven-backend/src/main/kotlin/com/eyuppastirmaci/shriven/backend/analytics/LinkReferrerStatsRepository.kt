package com.eyuppastirmaci.shriven.backend.analytics

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface LinkReferrerStatsRepository : JpaRepository<LinkReferrerStatsEntity, Long> {

    fun findAllByShortCodeOrderByCountDesc(shortCode: String): List<LinkReferrerStatsEntity>

    @Modifying
    @Transactional
    @Query(
        value = """
            INSERT INTO link_referrer_stats (short_code, referrer_domain, count)
            VALUES (:shortCode, :referrerDomain, :delta)
            ON CONFLICT (short_code, referrer_domain)
            DO UPDATE SET count = link_referrer_stats.count + :delta
        """,
        nativeQuery = true
    )
    fun upsertIncrement(
        @Param("shortCode") shortCode: String,
        @Param("referrerDomain") referrerDomain: String,
        @Param("delta") delta: Long
    )
}
