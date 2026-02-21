package com.eyuppastirmaci.shriven.backend.auth

import com.eyuppastirmaci.shriven.backend.auth.dto.AuthResponse
import com.eyuppastirmaci.shriven.backend.auth.dto.LoginRequest
import com.eyuppastirmaci.shriven.backend.auth.dto.RegisterRequest
import com.eyuppastirmaci.shriven.backend.exception.EmailAlreadyExistsException
import com.eyuppastirmaci.shriven.backend.exception.InvalidCredentialsException
import com.eyuppastirmaci.shriven.backend.snowflake.SnowflakeIdGenerator
import com.eyuppastirmaci.shriven.backend.user.UserEntity
import com.eyuppastirmaci.shriven.backend.user.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val snowflakeIdGenerator: SnowflakeIdGenerator
) {

    @Transactional
    fun register(request: RegisterRequest): Pair<AuthResponse, String> {
        if (userRepository.existsByEmail(request.email)) {
            throw EmailAlreadyExistsException("Email is already registered: ${request.email}")
        }

        val id = snowflakeIdGenerator.nextId()
        val user = userRepository.save(
            UserEntity(
                id = id,
                email = request.email,
                passwordHash = passwordEncoder.encode(request.password)!!
            )
        )

        return buildAuthPair(user.id, user.email)
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): Pair<AuthResponse, String> {
        val user = userRepository.findByEmail(request.email)
            ?: throw InvalidCredentialsException("Invalid email or password")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw InvalidCredentialsException("Invalid email or password")
        }

        return buildAuthPair(user.id, user.email)
    }

    fun refresh(refreshToken: String): AuthResponse {
        val userId = refreshTokenService.validate(refreshToken)
            ?: throw InvalidCredentialsException("Invalid or expired refresh token")

        val user = userRepository.findById(userId).orElseThrow {
            InvalidCredentialsException("User not found")
        }

        return AuthResponse(
            accessToken = jwtService.generateToken(user.id, user.email),
            email = user.email
        )
    }

    fun logout(refreshToken: String) {
        refreshTokenService.revoke(refreshToken)
    }

    private fun buildAuthPair(userId: Long, email: String): Pair<AuthResponse, String> {
        val accessToken = jwtService.generateToken(userId, email)
        val refreshToken = refreshTokenService.create(userId)
        return Pair(AuthResponse(accessToken = accessToken, email = email), refreshToken)
    }
}
