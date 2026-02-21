package com.eyuppastirmaci.shriven.backend.kafka

import com.eyuppastirmaci.shriven.backend.analytics.GeoIpService
import com.eyuppastirmaci.shriven.backend.analytics.LinkDeviceStatsRepository
import com.eyuppastirmaci.shriven.backend.analytics.LinkGeoStatsRepository
import com.eyuppastirmaci.shriven.backend.analytics.LinkStatsRepository
import com.eyuppastirmaci.shriven.backend.analytics.LinkReferrerStatsRepository
import com.eyuppastirmaci.shriven.backend.analytics.ReferrerDomainExtractor
import com.eyuppastirmaci.shriven.backend.analytics.UserAgentParseService
import com.eyuppastirmaci.shriven.backend.analytics.dto.ClickEvent
import com.eyuppastirmaci.shriven.backend.url.UrlRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneOffset

@Component
class ClickEventConsumer(
    private val linkStatsRepository: LinkStatsRepository,
    private val urlRepository: UrlRepository,
    private val linkGeoStatsRepository: LinkGeoStatsRepository,
    private val linkDeviceStatsRepository: LinkDeviceStatsRepository,
    private val linkReferrerStatsRepository: LinkReferrerStatsRepository,
    private val geoIpService: GeoIpService,
    private val userAgentParseService: UserAgentParseService,
    private val referrerDomainExtractor: ReferrerDomainExtractor
) {
    @KafkaListener(
        topics = ["url-clicks"],
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    fun consume(events: List<ClickEvent>) {
        if (events.isEmpty()) return

        // Existing: aggregate by (shortCode, date) and increment url click_count
        val dailyAggregates = events
            .groupBy { event ->
                Pair(event.shortCode, event.timestamp.atZone(ZoneOffset.UTC).toLocalDate())
            }
            .mapValues { (_, grouped) -> grouped.size.toLong() }

        dailyAggregates.forEach { (key, count) ->
            val (shortCode, date) = key
            linkStatsRepository.upsertDailyClicks(shortCode, date, count)
        }

        events.groupBy { it.shortCode }
            .mapValues { (_, grouped) -> grouped.size.toLong() }
            .forEach { (shortCode, count) ->
                urlRepository.incrementClickCountBy(shortCode, count)
            }

        // GeoIP, device, referrer: enrich each event and aggregate, then upsert
        val geoAggregates = mutableMapOf<Pair<String, String>, Long>()
        val deviceAggregates = mutableMapOf<DeviceKey, Long>()
        val referrerAggregates = mutableMapOf<Pair<String, String>, Long>()

        for (event in events) {
            val countryCode = geoIpService.getCountryCode(event.ipAddress)
            geoAggregates.merge(Pair(event.shortCode, countryCode), 1L) { a, b -> a + b }

            val parsed = userAgentParseService.parse(event.userAgent)
            val deviceKey = DeviceKey(event.shortCode, parsed.browser, parsed.os, parsed.deviceType)
            deviceAggregates.merge(deviceKey, 1L) { a, b -> a + b }

            val referrerDomain = referrerDomainExtractor.extractDomain(event.referrer)
            referrerAggregates.merge(Pair(event.shortCode, referrerDomain), 1L) { a, b -> a + b }
        }

        geoAggregates.forEach { (key, count) ->
            linkGeoStatsRepository.upsertIncrement(key.first, key.second, count)
        }

        deviceAggregates.forEach { (key, count) ->
            linkDeviceStatsRepository.upsertIncrement(key.shortCode, key.browser, key.os, key.deviceType, count)
        }

        referrerAggregates.forEach { (key, count) ->
            linkReferrerStatsRepository.upsertIncrement(key.first, key.second, count)
        }
    }

    private data class DeviceKey(
        val shortCode: String,
        val browser: String,
        val os: String,
        val deviceType: String
    )
}
