package com.eyuppastirmaci.shriven.backend.url.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UnlockRequest(
    @field:NotBlank(message = "Password is required")
    @field:Size(max = 128, message = "Password must not exceed 128 characters")
    val password: String
)
