package com.eyuppastirmaci.shriven.backend.auth

import com.eyuppastirmaci.shriven.backend.properties.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.Date

@Service
class JwtService(private val jwtProperties: JwtProperties) {

    private val signingKey by lazy {
        Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtProperties.secret))
    }

    fun generateToken(userId: Long, email: String): String =
        Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtProperties.expiration))
            .signWith(signingKey)
            .compact()

    fun isTokenValid(token: String): Boolean = runCatching { extractAllClaims(token) }.isSuccess

    fun extractUserId(token: String): Long =
        extractAllClaims(token).subject.toLong()

    fun extractEmail(token: String): String =
        extractAllClaims(token)["email"] as String

    private fun extractAllClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
}
