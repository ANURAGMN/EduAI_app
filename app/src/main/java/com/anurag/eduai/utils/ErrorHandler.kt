package com.anurag.eduai.utils

import com.anurag.eduai.debug.DebugLogger
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Centralized error handling for all HTTP and network errors
 * Single source of truth for error messages and status codes
 */
object ErrorHandler {

    // Error message mapping - Easy to maintain and extend
    private val errorMessages = mapOf(
        501 to "Please try after some time.",
        502 to "Please try tomorrow.",
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
     * Check if error is retryable based on status code
     */
    fun isRetryable(statusCode: Int): Boolean {
        return when (statusCode) {
            in 500..599 -> true   // Server errors: should retry (may be transient)
            in 400..499 -> false  // Client errors: don't retry
            -1, -2 -> true        // Network errors: retry
            else -> true
        }
    }

    /**
     * Check if an exception should be retried
     * Centralizes retry logic used in callWithRetry
     *
     * @param exception The exception to evaluate
     * @return true if the API call should be retried, false otherwise
     */
    fun shouldRetryException(exception: Exception?): Boolean {
        return when {
            exception == null -> false  // No exception means success - don't retry

            // Network/IO errors should be retried (they may be transient)
            exception is java.net.SocketTimeoutException -> true
            exception is java.net.ConnectException -> true
            exception is java.net.UnknownHostException -> true
            exception is IOException -> {
                // For IOException, check if it contains 5xx status code (retryable)
                val statusCode = extractStatusCode(exception.message ?: "")
                when {
                    statusCode in 500..599 -> true    // Server error: retry
                    statusCode in 400..499 -> false   // Client error: don't retry
                    statusCode == 0 -> true           // No status code: network error, retry
                    else -> true                      // Unknown: retry
                }
            }

            // Unknown exceptions - don't retry by default
            else -> false
        }
    }

    /**
     * Log error with context
     */
    fun logError(tag: String, statusCode: Int, message: String) {
        DebugLogger.errorLog(tag, "HTTP $statusCode - $message")
    }

    /**
     * Handle any exception and return user-friendly error message
     * Single method to handle all error types across the app
     *
     * @param exception The exception to handle
     * @param operation Optional operation name for better logging (e.g., "start_session", "send_response")
     * @param tag Optional tag for logging context
     * @return User-friendly error message to display to user
     */
    fun handleException(
        exception: Exception,
        operation: String = "operation",
        tag: String = "ErrorHandler"
    ): String {
        val errorMessage = when (exception) {
            // Network timeouts
            is SocketTimeoutException -> {
                DebugLogger.errorLog(tag, "$operation - Connection timed out")
                "Connection timed out. Please check your internet connection."
            }

            // DNS/Host resolution failures
            is UnknownHostException -> {
                DebugLogger.errorLog(tag, "$operation - Unable to reach server")
                "Unable to reach server. Please check your internet connection."
            }

            // Retrofit HTTP exceptions (direct status code)
            is retrofit2.HttpException -> {
                val statusCode = exception.code()
                DebugLogger.errorLog(tag, "$operation - HTTP $statusCode")
                when (statusCode) {
                    501, 502 -> getErrorMessage(statusCode)  // Use centralized messages
                    404 -> getErrorMessage(404)
                    500 -> getErrorMessage(500)
                    in 500..599 -> "Server error. Please try again later."
                    in 400..499 -> "Request error. Please try again."
                    else -> "Network error ($statusCode). Please try again."
                }
            }

            // IO exceptions (includes network errors)
            is IOException -> {
                val statusCode = extractStatusCode(exception.message ?: "")
                DebugLogger.errorLog(tag, "$operation - IOException with status $statusCode: ${exception.message}")

                when {
                    statusCode == 501 || statusCode == 502 -> getErrorMessage(statusCode)
                    statusCode == 404 -> getErrorMessage(404)
                    statusCode == 500 -> getErrorMessage(500)
                    statusCode in 500..599 -> "Server error. Please try again later."
                    statusCode in 400..499 -> "Request error. Please try again."
                    else -> "Connection error. Please check your internet and try again."
                }
            }

            // Default/unknown exceptions
            else -> {
                DebugLogger.errorLog(tag, "$operation - ${exception.javaClass.simpleName}: ${exception.message}")
                "An error occurred. Please try again."
            }
        }

        return errorMessage
    }
}