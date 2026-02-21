package com.eyuppastirmaci.shriven.backend.exception

class InvalidLinkPasswordException(
    message: String = "Incorrect password"
) : RuntimeException(message)
