package com.eyuppastirmaci.shriven.backend.analytics

/**
 * Static mapping of ISO 3166-1 alpha-2 country codes to display names.
 * Unknown or missing codes return the code itself for display.
 */
object CountryNames {
    private val names = mapOf(
        "Unknown" to "Unknown",
        "US" to "United States", "GB" to "United Kingdom", "DE" to "Germany", "FR" to "France",
        "TR" to "Turkey", "NL" to "Netherlands", "ES" to "Spain", "IT" to "Italy", "CA" to "Canada",
        "AU" to "Australia", "BR" to "Brazil", "IN" to "India", "JP" to "Japan", "CN" to "China",
        "RU" to "Russia", "KR" to "South Korea", "PL" to "Poland", "SE" to "Sweden", "NO" to "Norway",
        "MX" to "Mexico", "AR" to "Argentina", "ZA" to "South Africa", "EG" to "Egypt", "NG" to "Nigeria",
        "ID" to "Indonesia", "TH" to "Thailand", "VN" to "Vietnam", "PH" to "Philippines", "MY" to "Malaysia",
        "SG" to "Singapore", "AE" to "United Arab Emirates", "SA" to "Saudi Arabia", "IL" to "Israel",
        "CH" to "Switzerland", "AT" to "Austria", "BE" to "Belgium", "PT" to "Portugal", "GR" to "Greece",
        "CZ" to "Czech Republic", "RO" to "Romania", "HU" to "Hungary", "FI" to "Finland", "DK" to "Denmark",
        "IE" to "Ireland", "NZ" to "New Zealand", "CL" to "Chile", "CO" to "Colombia", "PE" to "Peru"
    )

    fun getName(countryCode: String): String = names[countryCode] ?: countryCode
}
