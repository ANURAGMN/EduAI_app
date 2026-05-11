package com.anurag.eduai.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.anurag.eduai.debug.DebugLogger
import java.util.Date

/**
 * Utility to decode and validate JWT tokens, especially Google ID tokens.
 * Handles expiry checking with proper buffer times.
 */
object JwtDecoder {

    private const val TAG = "JwtDecoder"
    private const val DEFAULT_BUFFER_SECONDS = 600L // 10 minutes

    /**
     * Safely decode a JWT token and extract the exp claim
     * @param token The JWT token string
     * @return The decoded JWT object, or null if decoding fails
     */
    private fun decodeToken(token: String): com.auth0.jwt.interfaces.DecodedJWT? {
        return try {
            if (token.isBlank()) {
                DebugLogger.errorLog(TAG, "Token is blank/empty")
                return null
            }
            JWT.decode(token)
        } catch (e: JWTDecodeException) {
            DebugLogger.errorLog(TAG, "JWT decode error: ${e.message}")
            null
        } catch (e: Exception) {
            DebugLogger.errorLog(
                TAG,
                "Unexpected error decoding JWT: ${e.javaClass.simpleName} - ${e.message}"
            )
            null
        }
    }

    /**
     * Extracts the expiry time (exp claim) from a JWT token in seconds since epoch
     * @param token The JWT token
     * @return The expiry time in seconds since epoch (Unix timestamp), or null if cannot be extracted
     */
    fun getExpiryTimeInSeconds(token: String): Long? {
        val decoded = decodeToken(token) ?: return null
        return try {
            val expiresAt: Date? = decoded.expiresAt
            if (expiresAt != null) {
                val expirySeconds = expiresAt.time / 1000
                DebugLogger.debugLog(
                    TAG,
                    " Token expiry extracted: ${expiresAt.time}ms = $expirySeconds seconds"
                )
                expirySeconds
            } else {
                DebugLogger.errorLog(TAG, " Token has no expiry (exp) claim")
                null
            }
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Failed to extract expiry seconds: ${e.message}")
            null
        }
    }

    /**
     * Extracts the expiry time from JWT and returns it as milliseconds for compatibility
     * with SharedPreferences storage (which uses Long in milliseconds).
     * @param token The JWT token
     * @return Expiry time in milliseconds since epoch, or null if cannot be extracted
     */
    fun getExpiryTimeInMillis(token: String): Long? {
        val expirySeconds = getExpiryTimeInSeconds(token) ?: return null
        return expirySeconds * 1000
    }

    /**
     * Checks if a token is already expired (current time >= exp time)
     * @param token The JWT token
     * @return true if token is expired, false if valid
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            val decoded = decodeToken(token) ?: return true

            // Use the expiresAt date directly instead of decoded.isExpired
            // because isExpired may have issues with timezone or system clock
            val expiresAt: Date? = decoded.expiresAt
            if (expiresAt == null) {
                DebugLogger.errorLog(TAG, " Token has no expiry (exp) claim - assuming expired")
                return true
            }

            val currentTimeMs = System.currentTimeMillis()
            val expiryTimeMs = expiresAt.time
            val isExpired = currentTimeMs >= expiryTimeMs

            if (isExpired) {
                val diffSeconds = (currentTimeMs - expiryTimeMs) / 1000
                DebugLogger.debugLog(TAG, " Token is expired (${diffSeconds}s ago)")
            } else {
                val diffSeconds = (expiryTimeMs - currentTimeMs) / 1000
                DebugLogger.debugLog(TAG, " Token is not yet expired (${diffSeconds}s remaining)")
            }
            isExpired
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error checking token expiry: ${e.message}")
            true // Assume expired if we can't verify
        }
    }

    /**
     * Checks if token is expiring soon (within buffer time from now)
     * Useful for proactive refresh before token becomes unusable.
     *
     * @param token The JWT token
     * @param bufferSeconds Buffer time in seconds (default 10 minutes)
     * @return true if token expires within buffer time, false if still valid
     */
    fun isTokenExpiringWithinBuffer(token: String, bufferSeconds: Long = DEFAULT_BUFFER_SECONDS): Boolean {
        return try {
            val decoded = decodeToken(token) ?: return true

            // Get expiry date directly
            val expiresAt: Date? = decoded.expiresAt
            if (expiresAt == null) {
                DebugLogger.errorLog(TAG, " Token has no expiry (exp) claim")
                return true // No expiry = assume expiring
            }

            val currentTimeMs = System.currentTimeMillis()
            val expiryTimeMs = expiresAt.time
            val secondsUntilExpiry = (expiryTimeMs - currentTimeMs) / 1000

            val isExpiringWithinBuffer = secondsUntilExpiry <= bufferSeconds

            if (isExpiringWithinBuffer) {
                DebugLogger.debugLog(
                    TAG,
                    " Token expiring within buffer: ${secondsUntilExpiry}s remaining (buffer: ${bufferSeconds}s)"
                )
            } else {
                DebugLogger.debugLog(
                    TAG,
                    " Token valid: ${secondsUntilExpiry}s remaining (buffer: ${bufferSeconds}s)"
                )
            }
            isExpiringWithinBuffer
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error checking token expiring: ${e.message}")
            true // Assume expiring if we can't verify
        }
    }

    /**
     * Gets the remaining time until token expiry in seconds
     * @param token The JWT token
     * @return Time in seconds until expiry, or null if cannot be determined
     */
    fun getSecondsUntilExpiry(token: String): Long? {
        return try {
            val decoded = decodeToken(token) ?: return null

            val expiresAt: Date? = decoded.expiresAt
            if (expiresAt == null) {
                DebugLogger.errorLog(TAG, "✗ Token has no expiry (exp) claim")
                return null
            }

            val currentTimeMs = System.currentTimeMillis()
            val expiryTimeMs = expiresAt.time
            val secondsRemaining = (expiryTimeMs - currentTimeMs) / 1000

            secondsRemaining
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error calculating seconds until expiry: ${e.message}")
            null
        }
    }

    /**
     * Gets email claim from token (useful for logging/debugging)
     */
    fun getEmailFromToken(token: String): String? {
        return try {
            val decoded = decodeToken(token) ?: return null
            decoded.getClaim("email").asString()
        } catch (e: Exception) {
            DebugLogger.debugLog(TAG, "Could not extract email: ${e.message}")
            null
        }
    }

    /**
     * Gets name claim from token (useful for logging/debugging)
     */
    fun getNameFromToken(token: String): String? {
        return try {
            val decoded = decodeToken(token) ?: return null
            decoded.getClaim("name").asString()
        } catch (e: Exception) {
            DebugLogger.debugLog(TAG, "Could not extract name: ${e.message}")
            null
        }
    }

    /**
     * Gets the issued-at time (iat claim) in seconds since epoch
     */
    fun getIssuedAtInSeconds(token: String): Long? {
        return try {
            val decoded = decodeToken(token) ?: return null
            val issuedAt: Date? = decoded.issuedAt
            issuedAt?.time?.div(1000)
        } catch (e: Exception) {
            null
        }
    }
}
