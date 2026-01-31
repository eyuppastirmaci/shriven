package com.eyuppastirmaci.shriven.backend.snowflake

import com.eyuppastirmaci.shriven.backend.properties.Base62Properties
import org.springframework.stereotype.Component

/**
 * Base62 encoder/decoder for converting numeric IDs to compact alphanumeric strings.
 *
 * Base62 uses 62 characters: 0-9, a-z, A-Z
 * This provides a good balance between compactness and URL-safety.
 *
 * Example:
 * - encode(275960112751841280) → "aB9xK2Fm"
 * - decode("aB9xK2Fm") → 275960112751841280
 */
@Component
class Base62Encoder(
    private val properties: Base62Properties
) {
    /**
     * Encodes a positive Long number to a Base62 string.
     *
     * The encoding process repeatedly divides the number by the base and maps
     * the remainder to a character in the charset.
     *
     * @param number The number to encode (must be non-negative)
     * @return The Base62-encoded string representation
     * @throws IllegalArgumentException if the number is negative
     */
    fun encode(number: Long): String {
        require(number >= 0) { "Cannot encode negative numbers: $number" }

        if (number == 0L) {
            return properties.charset[0].toString()
        }

        val result = StringBuilder()
        var num = number

        while (num > 0) {
            val remainder = (num % properties.base).toInt()
            result.append(properties.charset[remainder])
            num /= properties.base
        }

        return result.reverse().toString()
    }

    /**
     * Decodes a Base62 string back to its original Long number.
     *
     * The decoding process treats the string as a base-N number and
     * converts it back to base-10 (Long).
     *
     * @param encoded The Base62-encoded string
     * @return The original Long number
     * @throws IllegalArgumentException if the string contains invalid characters
     */
    fun decode(encoded: String): Long {
        require(encoded.isNotBlank()) { "Cannot decode empty or blank string" }

        var result = 0L

        for (char in encoded) {
            val index = properties.charset.indexOf(char)
            require(index >= 0) {
                "Invalid character '$char' in Base62 string. Valid characters: ${properties.charset}"
            }
            result = result * properties.base + index
        }

        return result
    }

    /**
     * Validates if a string is a valid Base62-encoded string.
     *
     * @param encoded The string to validate
     * @return true if all characters are in the charset, false otherwise
     */
    fun isValid(encoded: String): Boolean {
        if (encoded.isBlank()) return false
        return encoded.all { it in properties.charset }
    }
}