package com.eyuppastirmaci.shriven.backend.snowflake

import com.eyuppastirmaci.shriven.backend.properties.SnowflakeProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Snowflake ID Generator for distributed unique ID generation
 *
 * 64-bit ID structure:
 * - 41 bits: Timestamp (milliseconds since custom epoch)
 * - 10 bits: Node ID (supports 1024 nodes)
 * - 12 bits: Sequence number (4096 IDs per millisecond per node)
 */
@Component
class SnowflakeIdGenerator(
    private val properties: SnowflakeProperties
) {
    companion object {
        private val logger = LoggerFactory.getLogger(SnowflakeIdGenerator::class.java)
    }

    // Calculated values based on properties
    private val maxNodeId = (1 shl properties.nodeIdBits) - 1
    private val maxSequence = (1 shl properties.sequenceBits) - 1
    private val nodeIdShift = properties.sequenceBits
    private val timestampShift = properties.nodeIdBits + properties.sequenceBits

    private var lastTimestamp = -1L
    private var sequence = 0L

    /**
     * Generates the next unique Snowflake ID.
     *
     * This method is thread-safe and guarantees unique, monotonically increasing IDs
     * across multiple threads. If called within the same millisecond, it increments
     * the sequence counter. If the sequence overflows (4096 IDs per millisecond),
     * it waits for the next millisecond.
     *
     * @return A unique 64-bit Snowflake ID
     * @throws IllegalStateException if the system clock moves backwards
     */
    @Synchronized
    fun nextId(): Long {
        var timestamp = currentTimestamp()

        // Clock moved backwards - wait until it catches up
        if (timestamp < lastTimestamp) {
            val drift = lastTimestamp - timestamp
            logger.error("Clock moved backwards by {}ms. Refusing to generate ID.", drift)
            throw IllegalStateException(
                "Clock moved backwards. Refusing to generate ID for ${drift}ms"
            )
        }

        // Same millisecond - increment sequence
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) and maxSequence.toLong()

            // Sequence overflow - wait for next millisecond
            if (sequence == 0L) {
                logger.warn("Sequence overflow detected. Waiting for next millisecond.")
                timestamp = waitNextMillis(lastTimestamp)
            }
        } else {
            // New millisecond - reset sequence
            sequence = 0L
        }

        lastTimestamp = timestamp

        // Construct the ID
        return ((timestamp - properties.customEpoch) shl timestampShift) or
                (properties.nodeId.toLong() shl nodeIdShift) or
                sequence
    }

    /**
     * Parses a Snowflake ID and extracts its components.
     *
     * This method decomposes a 64-bit Snowflake ID into its constituent parts:
     * timestamp, node ID, and sequence number. This is useful for debugging,
     * analytics, or understanding when and where an ID was generated.
     *
     * @param id The Snowflake ID to parse
     * @return A [SnowflakeIdComponents] object containing the extracted components
     */
    fun parse(id: Long): SnowflakeIdComponents {
        val timestamp = ((id shr timestampShift) + properties.customEpoch)
        val nodeId = ((id shr nodeIdShift) and maxNodeId.toLong()).toInt()
        val sequence = (id and maxSequence.toLong()).toInt()

        return SnowflakeIdComponents(
            id = id,
            timestamp = timestamp,
            nodeId = nodeId,
            sequence = sequence
        )
    }

    /**
     * Get current timestamp in milliseconds
     */
    private fun currentTimestamp(): Long {
        return Instant.now().toEpochMilli()
    }

    /**
     * Wait until next millisecond
     */
    private fun waitNextMillis(lastTimestamp: Long): Long {
        var timestamp = currentTimestamp()
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimestamp()
        }
        return timestamp
    }
}