package com.eyuppastirmaci.shriven.backend.tag.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateTagRequest(
    @field:NotBlank(message = "Tag name cannot be empty")
    @field:Size(max = 50, message = "Tag name must not exceed 50 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9 _-]+$",
        message = "Tag name may only contain letters, numbers, spaces, hyphens, or underscores"
    )
    val name: String
)
