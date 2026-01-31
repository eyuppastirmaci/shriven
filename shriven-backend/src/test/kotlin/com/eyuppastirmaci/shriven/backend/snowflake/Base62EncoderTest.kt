package com.eyuppastirmaci.shriven.backend.snowflake

import com.eyuppastirmaci.shriven.backend.properties.Base62Properties
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Base62EncoderTest {

    private fun createEncoder(): Base62Encoder {
        val properties = Base62Properties(
            charset = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
            base = 62
        )
        return Base62Encoder(properties)
    }

    @Test
    fun `should encode and decode round trip`() {
        val encoder = createEncoder()
        val testCases = listOf(
            0L,
            1L,
            62L,
            1234567890L,
            275960112751841280L,
            Long.MAX_VALUE
        )

        testCases.forEach { original ->
            val encoded = encoder.encode(original)
            val decoded = encoder.decode(encoded)
            assertEquals(original, decoded, "Round trip failed for $original")
        }
    }

    @Test
    fun `should reject negative numbers`() {
        val encoder = createEncoder()
        assertThrows<IllegalArgumentException> {
            encoder.encode(-1)
        }
    }

    @Test
    fun `should reject invalid characters`() {
        val encoder = createEncoder()
        assertThrows<IllegalArgumentException> {
            encoder.decode("abc!@#")
        }

        assertThrows<IllegalArgumentException> {
            encoder.decode("hello world")
        }
    }

    @Test
    fun `should validate Base62 strings correctly`() {
        val encoder = createEncoder()
        assertTrue(encoder.isValid("abc123XYZ"))
        assertFalse(encoder.isValid("abc!@#"))
        assertFalse(encoder.isValid(""))
    }
}