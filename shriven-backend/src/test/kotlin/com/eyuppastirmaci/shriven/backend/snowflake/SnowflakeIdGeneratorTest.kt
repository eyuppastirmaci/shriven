package com.eyuppastirmaci.shriven.backend.snowflake

import com.eyuppastirmaci.shriven.backend.properties.SnowflakeProperties
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SnowflakeIdGeneratorTest {

    private fun createGenerator(nodeId: Int): SnowflakeIdGenerator {
        val properties = SnowflakeProperties(
            nodeId = nodeId,
            customEpoch = 1704067200000L,
            nodeIdBits = 10,
            sequenceBits = 12
        )
        return SnowflakeIdGenerator(properties)
    }

    @Test
    fun `should generate unique IDs`() {
        val generator = createGenerator(nodeId = 1)

        val id1 = generator.nextId()
        val id2 = generator.nextId()
        val id3 = generator.nextId()

        assertNotEquals(id1, id2)
        assertNotEquals(id2, id3)
        assertNotEquals(id1, id3)
    }

    @Test
    fun `should generate IDs in ascending order`() {
        val generator = createGenerator(nodeId = 1)

        val ids = (1..100).map { generator.nextId() }

        // Check if IDs are in ascending order
        for (i in 0 until ids.size - 1) {
            assertTrue(ids[i] < ids[i + 1], "ID at index $i should be less than ID at index ${i + 1}")
        }
    }

    @Test
    fun `should generate many unique IDs quickly`() {
        val generator = createGenerator(nodeId = 1)

        val ids = (1..10000).map { generator.nextId() }.toSet()

        assertEquals(10000, ids.size, "All generated IDs should be unique")
    }

    @Test
    fun `should parse ID correctly`() {
        val generator = createGenerator(nodeId = 42)

        val id = generator.nextId()
        val components = generator.parse(id)

        assertEquals(id, components.id)
        assertEquals(42, components.nodeId)
        assertTrue(components.timestamp > 0)
        assertTrue(components.sequence >= 0)
    }

    @Test
    fun `should handle different node IDs`() {
        val generator1 = createGenerator(nodeId = 1)
        val generator2 = createGenerator(nodeId = 2)

        val id1 = generator1.nextId()
        val id2 = generator2.nextId()

        val components1 = generator1.parse(id1)
        val components2 = generator2.parse(id2)

        assertEquals(1, components1.nodeId)
        assertEquals(2, components2.nodeId)
    }

    @Test
    fun `should handle sequence overflow within same millisecond`() {
        val generator = createGenerator(nodeId = 1)

        // Generate many IDs quickly (might span multiple milliseconds)
        val ids = (1..5000).map { generator.nextId() }

        // All should be unique
        assertEquals(5000, ids.toSet().size)
    }
}