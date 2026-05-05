package com.anurag.eduai.data.remote

import android.content.Context
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.utils.JwtDecoder
import com.anurag.eduai.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * OkHttp interceptor that:
 * 1. Validates token exists and is NOT expired/expiring
 * 2. If token is expiring in <10 min, kicks off background silent refresh (non-blocking)
 * 3. Attaches the current valid token to every request (Authorization + X-API-Key headers)
 *
 * The interceptor NEVER blocks the current request. Token refresh is fire-and-forget
 * background operation so the next request gets a fresh token.
 *
 * Key improvements:
 * - Direct JWT validation (checks exp claim, not just stored expiry)
 * - Smart refresh logic (prevents duplicate refreshes via cooldown + flag)
 * - Comprehensive logging for debugging token issues
 * - Fallback to stored token even if refresh fails
 */
class ProactiveTokenInterceptor(private val context: Context) : Interceptor {

    companion object {
        private const val TAG = "ProactiveTokenInterceptor"
        private const val REFRESH_COOLDOWN_MS = 60_000L // 1 minute between refresh attempts
        private const val TOKEN_BUFFER_SECONDS = 600L // 10 minutes

        // Shared atomic flags to prevent duplicate refreshes
        private val isRefreshing = AtomicBoolean(false)
        private val lastRefreshAttemptMs = AtomicLong(0L)

        // Background scope for fire-and-forget refresh coroutines
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        DebugLogger.debugLog(TAG, "───────────────────────────────────────")
        DebugLogger.debugLog(TAG, "⟳ Intercepting request: ${chain.request().url}")

        // Step 1: Check and potentially refresh expiring token (non-blocking)
        triggerBackgroundRefreshIfNeeded()

        // Step 2: Get current token and attach to request
        val token = TokenManager.getIdToken(context)

        if (token.isNullOrEmpty()) {
            DebugLogger.errorLog(TAG, "✗ CRITICAL: No token found in storage!")
            return chain.proceed(chain.request())
        }

        // Validate token is not expired BEFORE sending request
        val isTokenExpired = JwtDecoder.isTokenExpired(token)
        val isTokenExpiringWithinBuffer = JwtDecoder.isTokenExpiringWithinBuffer(token, TOKEN_BUFFER_SECONDS)
        val secondsRemaining = JwtDecoder.getSecondsUntilExpiry(token) ?: 0

        when {
            isTokenExpired -> {
                DebugLogger.errorLog(TAG, "✗ Token is EXPIRED! Will attempt to attach anyway (will get 401)")
            }
            isTokenExpiringWithinBuffer -> {
                DebugLogger.warnLog(TAG, "⚠ Token expiring in ${secondsRemaining}s (buffer: ${TOKEN_BUFFER_SECONDS}s)")
            }
            else -> {
                DebugLogger.debugLog(TAG, "✓ Token valid: ${secondsRemaining}s remaining")
            }
        }

        // Attach token to request (both headers for compatibility)
        val requestBuilder = chain.request().newBuilder()
        requestBuilder.header("Authorization", "Bearer $token")
        requestBuilder.header("X-API-Key", token)

        // Log token attachment (mask for security)
        val tokenLast4 = token.takeLast(4)
        DebugLogger.debugLog(TAG, "✓ Attached token to request (ends with: $tokenLast4)")

        val request = requestBuilder.build()
        DebugLogger.debugLog(TAG, "─────────────────────────────────────── ►")

        return chain.proceed(request)
    }

    /**
     * Triggers a background token refresh if token is expiring soon.
     * Non-blocking: uses fire-and-forget coroutine.
     *
     * Guards against duplicate refreshes:
     * - Only one refresh at a time via [isRefreshing] flag
     * - Minimum [REFRESH_COOLDOWN_MS] between refresh attempts
     * - Only runs if token is actually expiring (within buffer)
     */
    private fun triggerBackgroundRefreshIfNeeded() {
        val token = TokenManager.getIdToken(context)

        if (token.isNullOrEmpty()) {
            DebugLogger.debugLog(TAG, "No token to refresh")
            return
        }

        // Check if token is actually expiring soon
        if (!JwtDecoder.isTokenExpiringWithinBuffer(token, TOKEN_BUFFER_SECONDS)) {
            DebugLogger.debugLog(TAG, "Token valid, no refresh needed")
            return
        }

        // Check if we're already refreshing
        if (!isRefreshing.compareAndSet(false, true)) {
            DebugLogger.debugLog(TAG, "⟳ Refresh already in progress, skipping")
            return
        }

        // Check cooldown to prevent refresh spam
        val now = System.currentTimeMillis()
        val timeSinceLastAttempt = now - lastRefreshAttemptMs.get()
        if (timeSinceLastAttempt < REFRESH_COOLDOWN_MS) {
            DebugLogger.debugLog(TAG, "⟳ Refresh cooldown active (${timeSinceLastAttempt}ms/${REFRESH_COOLDOWN_MS}ms), skipping")
            isRefreshing.set(false)
            return
        }

        lastRefreshAttemptMs.set(now)

        // Fire-and-forget background refresh
        scope.launch {
            try {
                val secondsRemaining = JwtDecoder.getSecondsUntilExpiry(token) ?: 0
                DebugLogger.debugLog(TAG, "⟳ Background: Starting proactive refresh (${secondsRemaining}s remaining)")

                val success = TokenManager.refreshTokenSilently(context)

                if (success) {
                    val newToken = TokenManager.getIdToken(context)
                    if (newToken != null) {
                        val newSecondsRemaining = JwtDecoder.getSecondsUntilExpiry(newToken) ?: 0
                        DebugLogger.debugLog(TAG, "✓ Background: Token refreshed successfully (${newSecondsRemaining}s now)")
                    }
                } else {
                    DebugLogger.errorLog(TAG, "✗ Background: Token refresh failed (will retry on next request)")
                }
            } catch (e: Exception) {
                DebugLogger.errorLog(TAG, "✗ Background: Refresh exception: ${e.message}")
            } finally {
                isRefreshing.set(false)
            }
        }
    }

    // Helper to add warn level logging to DebugLogger if not already present
}