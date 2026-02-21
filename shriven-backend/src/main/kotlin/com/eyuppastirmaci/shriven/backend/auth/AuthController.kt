package com.eyuppastirmaci.shriven.backend.auth

import com.eyuppastirmaci.shriven.backend.auth.dto.AuthResponse
import com.eyuppastirmaci.shriven.backend.auth.dto.LoginRequest
import com.eyuppastirmaci.shriven.backend.auth.dto.RegisterRequest
import com.eyuppastirmaci.shriven.backend.exception.InvalidCredentialsException
import com.eyuppastirmaci.shriven.backend.properties.JwtProperties
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtProperties: JwtProperties
) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest,
        response: HttpServletResponse
    ): ResponseEntity<AuthResponse> {
        val (authResponse, refreshToken) = authService.register(request)
        setRefreshCookie(response, refreshToken)
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse)
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<AuthResponse> {
        val (authResponse, refreshToken) = authService.login(request)
        setRefreshCookie(response, refreshToken)
        return ResponseEntity.ok(authResponse)
    }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(name = "refresh_token", required = false) refreshToken: String?
    ): ResponseEntity<AuthResponse> {
        if (refreshToken == null) throw InvalidCredentialsException("Refresh token is missing")
        val authResponse = authService.refresh(refreshToken)
        return ResponseEntity.ok(authResponse)
    }

    @PostMapping("/logout")
    fun logout(
        @CookieValue(name = "refresh_token", required = false) refreshToken: String?,
        response: HttpServletResponse
    ): ResponseEntity<Void> {
        if (refreshToken != null) {
            authService.logout(refreshToken)
        }
        clearRefreshCookie(response)
        return ResponseEntity.noContent().build()
    }

    private fun setRefreshCookie(response: HttpServletResponse, token: String) {
        val cookie = ResponseCookie.from("refresh_token", token)
            .httpOnly(true)
            .sameSite("Lax")
            .path("/api/auth")
            .maxAge(Duration.ofMillis(jwtProperties.refreshExpiration))
            .build()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    private fun clearRefreshCookie(response: HttpServletResponse) {
        val cookie = ResponseCookie.from("refresh_token", "")
            .httpOnly(true)
            .sameSite("Lax")
            .path("/api/auth")
            .maxAge(Duration.ZERO)
            .build()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }
}
