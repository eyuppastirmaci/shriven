package com.eyuppastirmaci.shriven.backend.kafka

import com.eyuppastirmaci.shriven.backend.analytics.dto.ClickEvent
import com.eyuppastirmaci.shriven.backend.exception.KafkaOperationException
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component

@Component
class KafkaClient(
    private val kafkaTemplate: KafkaTemplate<String, ClickEvent>
) {
    companion object {
        private val logger = LoggerFactory.getLogger(KafkaClient::class.java)
        private const val TOPIC_URL_CLICKS = "url-clicks"
    }

    /**
     * Publishes a click event to the Kafka topic asynchronously.
     * Use the shortCode as the key to ensure ordering (if needed) and partition locality.
     *
     * @param event The click event data to send.
     * @throws KafkaOperationException if the send operation fails immediately (e.g., serialization error).
     */
    fun sendClickEvent(event: ClickEvent) {
        try {
            kafkaTemplate.send(TOPIC_URL_CLICKS, event.shortCode, event)
                .whenComplete { result: SendResult<String, ClickEvent>?, ex: Throwable? ->
                    if (ex != null) {
                        // Log async delivery failures (don't throw, as it's too late to affect the request)
                        logger.error("Failed to deliver click event for ${event.shortCode}", ex)
                    } else {
                        logger.debug("Delivered click event for ${event.shortCode} offset=[${result?.recordMetadata?.offset()}]")
                    }
                }
        } catch (e: Exception) {
            // Catch immediate failures (e.g. serialization, buffer full)
            throw KafkaOperationException("Failed to initiate click event send for: ${event.shortCode}", e)
        }
    }
}