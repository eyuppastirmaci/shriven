package com.eyuppastirmaci.shriven.backend.exception

data class ErrorResponse(
    val message: String,
    val errorCode: String,
    val timestamp: Long = System.currentTimeMillis()
)