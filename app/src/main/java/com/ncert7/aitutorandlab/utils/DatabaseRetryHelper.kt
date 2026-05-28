package com.ncert7.aitutorandlab.utils

import kotlinx.coroutines.delay

/**
 * Database Retry Helper - Smart retry logic for Room database operations
 * Only retries if an exception occurs. Returns immediately on success.
 */
object DatabaseRetryHelper {

    /**
     * Smart retry helper that only retries if an exception occurs
     * Returns result immediately if successful (no retry)
     *
     * @param maxRetries Maximum number of retry attempts
     * @param block The suspend function to execute
     * @return Result of the block execution
     * @throws Exception if all retries fail
     */
    suspend inline fun <T> retryIfFails(
        maxRetries: Int = 3,
        crossinline block: suspend () -> T
    ): T {
        var lastException: Exception? = null
        repeat(maxRetries) { attempt ->
            try {
                // Try to execute - if successful, return immediately (no retry)
                return block()
            } catch (e: Exception) {
                lastException = e
                // Only delay if we're going to retry
                if (attempt < maxRetries - 1) {
                    delay(100) // 100ms delay before retry
                }
            }
        }
        // If all retries failed, throw the last exception
        throw lastException ?: kotlin.Exception("Failed after $maxRetries attempts")
    }

    /**
     * Smart retry helper for nullable results
     * Returns result immediately if successful (even if null, no retry)
     *
     * @param maxRetries Maximum number of retry attempts
     * @param block The suspend function to execute
     * @return Result of the block execution (can be null)
     * @throws Exception if all retries fail
     */
    suspend inline fun <T> retryIfFailsNullable(
        maxRetries: Int = 3,
        crossinline block: suspend () -> T?
    ): T? {
        var lastException: Exception? = null
        repeat(maxRetries) { attempt ->
            try {
                // Try to execute - if successful (even if null), return immediately
                return block()
            } catch (e: Exception) {
                lastException = e
                // Only delay if we're going to retry
                if (attempt < maxRetries - 1) {
                    delay(100) // 100ms delay before retry
                }
            }
        }
        // If all retries failed, throw the last exception
        throw lastException ?: kotlin.Exception("Failed after $maxRetries attempts")
    }
}
