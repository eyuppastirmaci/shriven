package com.eyuppastirmaci.shriven.backend.exception

import com.eyuppastirmaci.shriven.backend.url.dto.response.UserUrlResponse

class DuplicateLinkException(
    message: String,
    val existingUrl: UserUrlResponse
) : RuntimeException(message)
