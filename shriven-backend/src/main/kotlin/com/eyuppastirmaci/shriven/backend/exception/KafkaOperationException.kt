package com.eyuppastirmaci.shriven.backend.exception

class KafkaOperationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)