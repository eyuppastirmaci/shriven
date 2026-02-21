package com.eyuppastirmaci.shriven.backend.kafka

import com.eyuppastirmaci.shriven.backend.analytics.LinkStatsRepository
import com.eyuppastirmaci.shriven.backend.analytics.dto.ClickEvent
import com.eyuppastirmaci.shriven.backend.url.UrlRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class ClickEventConsumerTest {

    private val linkStatsRepository = mockk<LinkStatsRepository>(relaxed = true)
    private val urlRepository = mockk<UrlRepository>(relaxed = true)
    private val consumer = ClickEventConsumer(linkStatsRepository, urlRepository)

    @Test
    fun `consume does nothing for empty list`() {
        consumer.consume(emptyList())

        verify(exactly = 0) { linkStatsRepository.upsertDailyClicks(any(), any(), any()) }
        verify(exactly = 0) { urlRepository.incrementClickCountBy(any(), any()) }
    }

    @Test
    fun `consume processes single event`() {
        val event = ClickEvent("abc123", Instant.parse("2026-01-15T10:00:00Z"), "Mozilla/5.0", "127.0.0.1")

        consumer.consume(listOf(event))

        verify { linkStatsRepository.upsertDailyClicks("abc123", LocalDate.of(2026, 1, 15), 1) }
        verify { urlRepository.incrementClickCountBy("abc123", 1) }
    }

    @Test
    fun `consume aggregates multiple events for same shortCode and date`() {
        val ts = Instant.parse("2026-01-15T10:00:00Z")
        val events = listOf(
            ClickEvent("abc123", ts, "Mozilla/5.0", "1.1.1.1"),
            ClickEvent("abc123", ts.plusSeconds(60), "Chrome", "2.2.2.2"),
            ClickEvent("abc123", ts.plusSeconds(120), "Safari", "3.3.3.3")
        )

        consumer.consume(events)

        verify { linkStatsRepository.upsertDailyClicks("abc123", LocalDate.of(2026, 1, 15), 3) }
        verify { urlRepository.incrementClickCountBy("abc123", 3) }
    }

    @Test
    fun `consume handles events for different shortCodes`() {
        val ts = Instant.parse("2026-01-15T10:00:00Z")
        val events = listOf(
            ClickEvent("abc123", ts, "Mozilla/5.0", "1.1.1.1"),
            ClickEvent("xyz789", ts, "Chrome", "2.2.2.2")
        )

        consumer.consume(events)

        verify { linkStatsRepository.upsertDailyClicks("abc123", LocalDate.of(2026, 1, 15), 1) }
        verify { linkStatsRepository.upsertDailyClicks("xyz789", LocalDate.of(2026, 1, 15), 1) }
        verify { urlRepository.incrementClickCountBy("abc123", 1) }
        verify { urlRepository.incrementClickCountBy("xyz789", 1) }
    }

    @Test
    fun `consume aggregates by date when events span multiple days`() {
        val events = listOf(
            ClickEvent("abc123", Instant.parse("2026-01-15T23:00:00Z"), "Mozilla/5.0", "1.1.1.1"),
            ClickEvent("abc123", Instant.parse("2026-01-16T01:00:00Z"), "Chrome", "2.2.2.2")
        )

        consumer.consume(events)

        verify { linkStatsRepository.upsertDailyClicks("abc123", LocalDate.of(2026, 1, 15), 1) }
        verify { linkStatsRepository.upsertDailyClicks("abc123", LocalDate.of(2026, 1, 16), 1) }
        verify { urlRepository.incrementClickCountBy("abc123", 2) }
    }
}
