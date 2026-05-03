package com.anurag.eduai.data.remote

import android.content.Context
import com.anurag.eduai.debug.DebugLogger
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
 * 1. Attaches the current stored token to every request (Bearer + X-API-Key).
 * 2. If the token is expiring in <10 min, kicks off a background silent refresh
 *    so the NEXT request gets a fresh token — without blocking the current one.
 *
 * The interceptor NEVER blocks. Refresh is fire-and-forget in a background coroutine.
 * Duplicate refreshes are prevented by [isRefreshing] flag and [lastRefreshAttemptMs].
 */
class ProactiveTokenInterceptor(private val context: Context) : Interceptor {

    companion object {
        // Minimum gap between refresh attempts — avoids hammering on every API call
        private const val REFRESH_COOLDOWN_MS = 60_000L // 1 minute

        private val isRefreshing = AtomicBoolean(false)
        private val lastRefreshAttemptMs = AtomicLong(0L)

        // Single background scope shared across all interceptor instances
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        // ── 1. Kick off background refresh if token is expiring soon ──
        triggerBackgroundRefreshIfNeeded()

        // ── 2. Attach current token to the request (no blocking) ──
        val token = TokenManager.getIdToken(context)
        val request = chain.request().newBuilder().apply {
            if (token != null) {
                header("Authorization", "Bearer $token")
                header("X-API-Key", token)
            }
        }.build()

        return chain.proceed(request)
    }

    /**
     * Fires a background coroutine to refresh the token.
     * Guards:
     * - Only runs if token is actually expiring
     * - Only one refresh at a time ([isRefreshing])
     * - Minimum [REFRESH_COOLDOWN_MS] between attempts
     */
    private fun triggerBackgroundRefreshIfNeeded() {
        if (!TokenManager.isTokenExpiredOrExpiring(context)) return

        val now = System.currentTimeMillis()
        if (now - lastRefreshAttemptMs.get() < REFRESH_COOLDOWN_MS) return
        if (!isRefreshing.compareAndSet(false, true)) return // already refreshing

        lastRefreshAttemptMs.set(now)

        scope.launch {
            try {
                DebugLogger.debugLog("ProactiveTokenInterceptor", "Background: token expiring, refreshing silently")
                val success = TokenManager.refreshTokenSilently(context)
                if (success) {
                    DebugLogger.debugLog("ProactiveTokenInterceptor", "Background: token refreshed OK")
                } else {
                    DebugLogger.errorLog("ProactiveTokenInterceptor", "Background: token refresh failed")
                }
            } finally {
                isRefreshing.set(false)
            }
        }
    }
}