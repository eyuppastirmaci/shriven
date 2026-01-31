package com.eyuppastirmaci.shriven.backend.exception

class RedisOperationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)