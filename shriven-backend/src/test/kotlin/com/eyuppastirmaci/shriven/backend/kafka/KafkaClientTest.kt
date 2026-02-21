package com.eyuppastirmaci.shriven.backend.kafka

import com.eyuppastirmaci.shriven.backend.analytics.dto.ClickEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.kafka.core.KafkaTemplate
import java.time.Instant
import java.util.concurrent.CompletableFuture

class KafkaClientTest {

    private val kafkaTemplate = mockk<KafkaTemplate<String, ClickEvent>>()
    private val kafkaClient = KafkaClient(kafkaTemplate)

    private val event = ClickEvent(
        shortCode = "abc123",
        timestamp = Instant.parse("2026-01-15T10:00:00Z"),
        userAgent = "Mozilla/5.0",
        ipAddress = "127.0.0.1"
    )

    @Test
    fun `sendClickEvent publishes to Kafka topic`() {
        every { kafkaTemplate.send("url-clicks", "abc123", event) } returns CompletableFuture()

        kafkaClient.sendClickEvent(event)

        verify { kafkaTemplate.send("url-clicks", "abc123", event) }
    }

    @Test
    fun `sendClickEvent swallows exception on immediate failure`() {
        every { kafkaTemplate.send(any<String>(), any(), any()) } throws RuntimeException("Buffer full")

        assertDoesNotThrow {
            kafkaClient.sendClickEvent(event)
        }
    }
}
