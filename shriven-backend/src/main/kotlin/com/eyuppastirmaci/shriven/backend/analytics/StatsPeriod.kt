package com.eyuppastirmaci.shriven.backend.analytics

enum class StatsPeriod {
    DAILY,
    WEEKLY;

    companion object Factory {
        fun fromString(value: String): StatsPeriod =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException(
                    "Invalid period '$value'. Accepted values: ${entries.map { it.name.lowercase() }}"
                )
    }
}
