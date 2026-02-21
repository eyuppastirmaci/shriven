package com.eyuppastirmaci.shriven.backend.auth

import com.eyuppastirmaci.shriven.backend.properties.JwtProperties
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

@Service
class RefreshTokenService(
    private val redisTemplate: StringRedisTemplate,
    private val jwtProperties: JwtProperties
) {
    private fun key(token: String) = "refresh:$token"

    fun create(userId: Long): String {
        val token = UUID.randomUUID().toString()
        redisTemplate.opsForValue().set(
            key(token),
            userId.toString(),
            Duration.ofMillis(jwtProperties.refreshExpiration)
        )
        return token
    }

    fun validate(token: String): Long? =
        redisTemplate.opsForValue().get(key(token))?.toLongOrNull()

    fun revoke(token: String) {
        redisTemplate.delete(key(token))
    }
}
