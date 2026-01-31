package com.eyuppastirmaci.shriven.backend.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Global exception handler for all REST controllers.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    /**
     * Handles UrlNotFoundException.
     * @return HTTP 404 NOT_FOUND
     */
    @ExceptionHandler(UrlNotFoundException::class)
    fun handleUrlNotFound(ex: UrlNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("URL not found: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                message = ex.message ?: "URL not found",
                errorCode = "URL_NOT_FOUND"
            ))
    }

    /**
     * Handles UrlExpiredException.
     * @return HTTP 410 GONE
     */
    @ExceptionHandler(UrlExpiredException::class)
    fun handleUrlExpired(ex: UrlExpiredException): ResponseEntity<ErrorResponse> {
        logger.warn("URL expired: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.GONE)
            .body(ErrorResponse(
                message = ex.message ?: "URL has expired",
                errorCode = "URL_EXPIRED"
            ))
    }

    /**
     * Handles validation errors from request body validation.
     * @return HTTP 400 BAD_REQUEST
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        logger.warn("Validation failed: {}", errors)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                message = "Validation failed: $errors",
                errorCode = "VALIDATION_ERROR"
            ))
    }

    /**
     * Handles Redis connectivity or operation errors.
     * @return HTTP 503 SERVICE_UNAVAILABLE (or 500)
     */
    @ExceptionHandler(RedisOperationException::class)
    fun handleRedisError(ex: RedisOperationException): ResponseEntity<ErrorResponse> {
        logger.error("Redis operation failed", ex)
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE) // 503 implies "Try again later"
            .body(ErrorResponse(
                message = "Cache service is currently unavailable",
                errorCode = "CACHE_ERROR"
            ))
    }

    /**
     * Handles all unexpected exceptions.
     * @return HTTP 500 INTERNAL_SERVER_ERROR
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericError(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error occurred", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                message = "An unexpected error occurred: ${ex.message}",
                errorCode = "INTERNAL_ERROR"
            ))
    }
}

