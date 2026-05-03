package com.anurag.eduai.utils

import android.content.Context
import com.anurag.eduai.debug.DebugLogger
import kotlinx.coroutines.delay
import retrofit2.Response
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
        401 to "Authentication failed. Please log in again.",
        403 to "Access denied. You don't have permission to access this resource.",
        404 to "Resource not found.",
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

    // ============ RESPONSE CODE HANDLING ============

    /**
     * Handles different HTTP response codes and determines retry strategy
     * @param resp The response object
     * @param context The application context (for token operations)
     * @param attempt Current attempt number
     * @param tokenExpiredRetries Number of token refresh retries already attempted
     * @return Triple<shouldContinue, shouldBreak, newTokenExpiredRetries>
     */
    suspend fun handleResponseCode(
        resp: Response<*>,
        context: Context,
        attempt: Int,
        tokenExpiredRetries: Int,
        maxTokenRefreshRetries: Int = 3,
        tag: String = "ErrorHandler"
    ): ResponseHandlerResult {
        val code = resp.code()
        val errBody = safeGetErrorBody(resp)
        val lastEx = IOException("HTTP $code: ${errBody ?: resp.message()}")

        return when {
            resp.isSuccessful -> ResponseHandlerResult.Success

            code in 500..599 -> {
                logError(tag, code, "Server error - will retry if transient")
                ResponseHandlerResult.ServerError(lastEx)
            }

            code == 401 -> {
                handle401Error(context, lastEx, tokenExpiredRetries, maxTokenRefreshRetries, tag)
            }

            code in listOf(403, 404) -> {
                logError(tag, code, "Client error - not retrying")
                ResponseHandlerResult.ClientError(lastEx)
            }

            code in 400..499 -> {
                ResponseHandlerResult.OtherClientError(lastEx)
            }

            else -> {
                ResponseHandlerResult.UnknownError(lastEx)
            }
        }
    }

    /**
     * Handles 401 Unauthorized errors with token refresh logic
     */
    private suspend fun handle401Error(
        context: Context,
        lastEx: Exception,
        tokenExpiredRetries: Int,
        maxTokenRefreshRetries: Int,
        tag: String
    ): ResponseHandlerResult {
        // Check if token is expired
        val isTokenExpired = TokenManager.isTokenExpiredOrExpiring(context)
        DebugLogger.debugLog(
            tag,
            "Got 401 - Token expired/expiring: $isTokenExpired, retry attempt: $tokenExpiredRetries/$maxTokenRefreshRetries"
        )

        if (isTokenExpired && tokenExpiredRetries < maxTokenRefreshRetries) {
            val newRetryCount = tokenExpiredRetries + 1
            DebugLogger.debugLog(
                tag,
                "Token is expired, attempting refresh ($newRetryCount/$maxTokenRefreshRetries)"
            )

            val refreshSuccess = TokenManager.refreshTokenSilently(context)
            if (refreshSuccess) {
                DebugLogger.debugLog(tag, "Token refreshed successfully, retrying request")
                delay(500L) // Small delay before retry
                return ResponseHandlerResult.Token401RetryAfterRefresh(newRetryCount)
            } else {
                DebugLogger.errorLog(tag, "Token refresh failed")
                return ResponseHandlerResult.Token401RetryAfterFailedRefresh(newRetryCount)
            }
        } else if (isTokenExpired && tokenExpiredRetries >= maxTokenRefreshRetries) {
            DebugLogger.errorLog(
                tag,
                "Token refresh retries exhausted ($maxTokenRefreshRetries attempts), giving up"
            )
            return ResponseHandlerResult.Token401Exhausted(lastEx)
        } else {
            DebugLogger.errorLog(tag, "Got 401 but token is not expired - authentication issue")
            return ResponseHandlerResult.Token401NotExpired(lastEx)
        }
    }

    private fun safeGetErrorBody(resp: Response<*>): String? {
        return try {
            resp.errorBody()?.string()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Sealed class representing different response handling outcomes
     */
    sealed class ResponseHandlerResult {
        object Success : ResponseHandlerResult()
        data class ServerError(val exception: Exception) : ResponseHandlerResult()
        data class ClientError(val exception: Exception) : ResponseHandlerResult()
        data class OtherClientError(val exception: Exception) : ResponseHandlerResult()
        data class UnknownError(val exception: Exception) : ResponseHandlerResult()
        data class Token401RetryAfterRefresh(val newRetryCount: Int) : ResponseHandlerResult()
        data class Token401RetryAfterFailedRefresh(val newRetryCount: Int) : ResponseHandlerResult()
        data class Token401Exhausted(val exception: Exception) : ResponseHandlerResult()
        data class Token401NotExpired(val exception: Exception) : ResponseHandlerResult()
    }

}