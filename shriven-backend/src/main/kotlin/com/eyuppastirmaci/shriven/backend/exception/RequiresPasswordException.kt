package com.eyuppastirmaci.shriven.backend.exception

class RequiresPasswordException(
    val shortCode: String,
    message: String = "This link is password-protected"
) : RuntimeException(message)
