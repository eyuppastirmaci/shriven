package com.eyuppastirmaci.shriven.backend.kafka

import com.eyuppastirmaci.shriven.backend.analytics.LinkStatsRepository
import com.eyuppastirmaci.shriven.backend.analytics.dto.ClickEvent
import com.eyuppastirmaci.shriven.backend.url.UrlRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneOffset

@Component
class ClickEventConsumer(
    private val linkStatsRepository: LinkStatsRepository,
    private val urlRepository: UrlRepository
) {
    @KafkaListener(
        topics = ["url-clicks"],
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    fun consume(events: List<ClickEvent>) {
        if (events.isEmpty()) return

        // Aggregate by (shortCode, date) to get total count for that day
        val dailyAggregates = events
            .groupBy { event ->
                Pair(event.shortCode, event.timestamp.atZone(ZoneOffset.UTC).toLocalDate())
            }
            .mapValues { (_, grouped) -> grouped.size.toLong() }

        dailyAggregates.forEach { (key, count) ->
            val (shortCode, date) = key
            linkStatsRepository.upsertDailyClicks(shortCode, date, count)
        }

        // Increment total click_count per shortCode
        events.groupBy { it.shortCode }
            .mapValues { (_, grouped) -> grouped.size.toLong() }
            .forEach { (shortCode, count) ->
                urlRepository.incrementClickCountBy(shortCode, count)
            }
    }
}
