package com.anurag.eduai.data.remote

import android.content.Context
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.utils.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Proactively ensures token is fresh BEFORE making API requests.
 * This prevents 401 errors by refreshing tokens before they expire.
 *
 * Flow:
 * 1. Check if token is expiring (within 10-min buffer)
 * 2. If expiring: Silently refresh token
 * 3. Add fresh token to request
 * 4. Send request
 *
 * Advantages:
 * - Prevents 401 errors (no failed requests)
 * - No unnecessary retries
 * - Seamless user experience
 * - Faster overall (single attempt instead of retry)
 */
class ProactiveTokenInterceptor(private val context: Context) : Interceptor {

    companion object {
        private var lastRefreshAttemptTime = 0L
        private const val REFRESH_CHECK_INTERVAL_MS = 1000 // Check at least every 1 second
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return runBlocking {
            // Proactively check and refresh token if needed
            val now = System.currentTimeMillis()

            // Check if enough time has passed since last refresh attempt
            // This prevents hammering the refresh endpoint
            if (now - lastRefreshAttemptTime >= REFRESH_CHECK_INTERVAL_MS) {
                lastRefreshAttemptTime = now

                // Check if token is expired or expiring
                if (TokenManager.isTokenExpiredOrExpiring(context)) {
                    DebugLogger.debugLog(
                        "ProactiveTokenInterceptor",
                        "Token is expiring/expired, triggering proactive refresh"
                    )

                    // Silently refresh the token
                    val refreshSuccess = TokenManager.refreshTokenSilently(context)

                    if (!refreshSuccess) {
                        DebugLogger.debugLog(
                            "ProactiveTokenInterceptor",
                            "Proactive token refresh failed, clearing tokens"
                        )
                        TokenManager.clearAllTokens(context)
                    } else {
                        DebugLogger.debugLog(
                            "ProactiveTokenInterceptor",
                            "Token refreshed successfully before API call"
                        )
                    }
                } else {
                    DebugLogger.debugLog(
                        "ProactiveTokenInterceptor",
                        "Token is still valid, no refresh needed"
                    )
                }
            }

            // Get the (potentially refreshed) token and add it to request
            val token = TokenManager.getIdToken(context)
            val requestBuilder = chain.request().newBuilder()

            if (token != null) {
                DebugLogger.debugLog(
                    "ProactiveTokenInterceptor",
                    "Adding fresh token to request headers"
                )
                requestBuilder
                    .header("Authorization", "Bearer $token")
                    .header("X-API-Key", token)
            } else {
                DebugLogger.debugLog(
                    "ProactiveTokenInterceptor",
                    "WARNING: No token available, proceeding without auth headers"
                )
            }

            // Proceed with request using fresh token
            chain.proceed(requestBuilder.build())
        }
    }
}
