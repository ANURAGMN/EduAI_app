package com.anurag.eduai.utils

import android.content.Context
import android.widget.Toast
import com.anurag.eduai.debug.DebugLogger
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Centralized error handling for all HTTP and network errors.
 * Single source of truth for error messages and status codes.
 */
object ErrorHandler {

    // Error message mapping — easy to maintain and extend
    private val errorMessages = mapOf(
        501 to "Please try after some time.",
        502 to "Please try tomorrow.",
    )

    fun getErrorMessage(statusCode: Int): String {
        return errorMessages[statusCode] ?: "An unexpected error occurred. Please try again."
    }

    fun extractStatusCode(errorMessage: String): Int {
        return try {
            val patterns = listOf(
                "HTTP (\\d{3})".toRegex(),
                "SERVER_ERROR_(\\d{3})".toRegex(),
                "(\\d{3})".toRegex()
            )
            for (pattern in patterns) {
                val match = pattern.find(errorMessage)
                if (match != null) return match.groupValues[1].toInt()
            }
            0
        } catch (e: Exception) {
            DebugLogger.errorLog("ErrorHandler", "Failed to extract status code: ${e.message}")
            0
        }
    }

    fun isRetryable(statusCode: Int): Boolean {
        return when (statusCode) {
            in 500..599 -> true
            in 400..499 -> false
            -1, -2 -> true
            else -> true
        }
    }

    fun shouldRetryException(exception: Exception?): Boolean {
        return when {
            exception == null -> false
            exception is SocketTimeoutException -> true
            exception is java.net.ConnectException -> true
            exception is UnknownHostException -> true
            exception is IOException -> {
                val statusCode = extractStatusCode(exception.message ?: "")
                when {
                    statusCode in 500..599 -> true
                    statusCode in 400..499 -> false
                    statusCode == 0 -> true
                    else -> true
                }
            }
            else -> false
        }
    }

    fun logError(tag: String, statusCode: Int, message: String) {
        DebugLogger.errorLog(tag, "HTTP $statusCode - $message")
    }

    fun handleException(
        exception: Exception,
        operation: String = "operation",
        tag: String = "ErrorHandler"
    ): String {
        return when (exception) {
            is SocketTimeoutException -> {
                DebugLogger.errorLog(tag, "$operation - Connection timed out")
                "Connection timed out. Please check your internet connection."
            }
            is UnknownHostException -> {
                DebugLogger.errorLog(tag, "$operation - Unable to reach server")
                "Unable to reach server. Please check your internet connection."
            }
            is retrofit2.HttpException -> {
                val statusCode = exception.code()
                DebugLogger.errorLog(tag, "$operation - HTTP $statusCode")
                when (statusCode) {
                    501, 502 -> getErrorMessage(statusCode)
                    404 -> getErrorMessage(404)
                    500 -> getErrorMessage(500)
                    in 500..599 -> "Server error. Please try again later."
                    in 400..499 -> "Request error. Please try again."
                    else -> "Network error ($statusCode). Please try again."
                }
            }
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
            else -> {
                DebugLogger.errorLog(tag, "$operation - ${exception.javaClass.simpleName}: ${exception.message}")
                "An error occurred. Please try again."
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Token / Auth helpers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Shows a toast when background token refresh fails because the device is offline.
     * Call this from any thread — it safely posts to the main thread.
     *
     * NOTE: TokenManager.refreshTokenSilently() already calls this internally,
     * so you only need to call this manually if you trigger a refresh from
     * a place outside TokenManager.
     */
    fun showOfflineRefreshToast(context: Context) {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            Toast.makeText(
                context.applicationContext,
                "Connect to the internet to refresh your session.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}