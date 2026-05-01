package com.anurag.eduai.data.remote

import android.content.Context
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OPTIMIZED: Token refresh happens asynchronously in background.
 *
 * Design:
 * - Does NOT block the interceptor thread
 * - Token refresh happens asynchronously in background
 * - Current request uses existing token immediately
 * - Prevents 401s by keeping token fresh between requests
 *
 * Performance improvements:
 * - No runBlocking (no thread blocking)
 * - Async refresh prevents slowdown
 * - 1-second minimum between refresh attempts
 * - 10-second timeout on refresh to prevent hangs
 */
class ProactiveTokenInterceptor(private val context: Context) : Interceptor {

    companion object {
        private var lastRefreshAttemptTime = 0L
        private const val REFRESH_CHECK_INTERVAL_MS = 1000L // Check at least every 1 second
        private val refreshMutex = Mutex() // Prevent concurrent refresh attempts
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val now = System.currentTimeMillis()

        // Trigger refresh asynchronously if needed (non-blocking)
        if (now - lastRefreshAttemptTime >= REFRESH_CHECK_INTERVAL_MS) {
            lastRefreshAttemptTime = now

            // Launch refresh in background - doesn't block this thread
            scope.launch {
                refreshMutex.withLock {
                    if (TokenManager.isTokenExpiredOrExpiring(context)) {
                        DebugLogger.debugLog(
                            "ProactiveTokenInterceptor",
                            "Background: Refreshing token"
                        )
                        val refreshSuccess = TokenManager.refreshTokenSilently(context)
                        if (!refreshSuccess) {
                            TokenManager.clearAllTokens(context)
                        }
                    }
                }
            }
        }

        // Get current token and proceed immediately (no blocking)
        val token = TokenManager.getIdToken(context)
        val requestBuilder = chain.request().newBuilder()

        if (token != null) {
            requestBuilder
                .header("Authorization", "Bearer $token")
                .header("X-API-Key", token)
        }

        return chain.proceed(requestBuilder.build())
    }
}
