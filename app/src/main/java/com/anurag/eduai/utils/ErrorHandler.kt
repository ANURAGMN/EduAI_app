package com.anurag.eduai.utils

import com.anurag.eduai.debug.DebugLogger

/**
 * Centralized error handling for all HTTP and network errors
 * Single source of truth for error messages and status codes
 */
object ErrorHandler {

    // Error message mapping - Easy to maintain and extend
    private val errorMessages = mapOf(
        501 to "Please try after some time.",
        502 to "Please try tomorrow."
    )

    /**
     * Get user-friendly error message based on HTTP status code
     * @param statusCode HTTP status code
     * @return User-friendly message
     */
    fun getErrorMessage(statusCode: Int): String {
        return errorMessages[statusCode] ?: "An unexpected error occurred. Please try again."
    }

    /**
     * Extract HTTP status code from error message
     * Handles: "HTTP 501:", "SERVER_ERROR_501:", or raw numbers
     */
    fun extractStatusCode(errorMessage: String): Int {
        return try {
            val patterns = listOf(
                "HTTP (\\d{3})".toRegex(),
                "SERVER_ERROR_(\\d{3})".toRegex(),
                "(\\d{3})".toRegex()
            )

            for (pattern in patterns) {
                val match = pattern.find(errorMessage)
                if (match != null) {
                    return match.groupValues[1].toInt()
                }
            }
            0
        } catch (e: Exception) {
            DebugLogger.errorLog("ErrorHandler", "Failed to extract status code: ${e.message}")
            0
        }
    }

    /**
     * Check if error is retryable
     */
    fun isRetryable(statusCode: Int): Boolean {
        return when (statusCode) {
            in 500..599 -> false  // Server errors: no retry
            in 400..499 -> false  // Client errors: no retry
            -1, -2 -> true        // Network errors: retry
            else -> true
        }
    }

    /**
     * Log error with context
     */
    fun logError(tag: String, statusCode: Int, message: String) {
        DebugLogger.errorLog(tag, "HTTP $statusCode - $message")
    }
}