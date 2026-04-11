package com.anurag.eduai.data.remote

import android.content.Context
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Network interceptor that handles 401 responses by clearing expired tokens and retrying.
 * On 401: clears token, retries request once. If still fails, returns 401 to app.
 */
class TokenRefreshInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var response = chain.proceed(chain.request())

        // Handle 401 Unauthorized responses
        if (response.code == 401) {
            val requestUrl = chain.request().url
            val currentToken = TokenManager.getToken(context)

            DebugLogger.debugLog("TokenRefreshInterceptor", "Received 401 Unauthorized for $requestUrl")
            if (currentToken != null) {
                DebugLogger.debugLog("TokenRefreshInterceptor", "Current token: $currentToken")
            } else {
                DebugLogger.debugLog("TokenRefreshInterceptor", "No token available at 401 response")
            }

            // Clear the expired token
            TokenManager.clearToken(context)
            DebugLogger.debugLog("TokenRefreshInterceptor", "Expired token cleared. Retrying request without auth header")

            // Retry the request (new request will not have Authorization header since token is cleared)
            response.close()
            response = chain.proceed(chain.request())

            if (response.code == 401) {
                DebugLogger.debugLog("TokenRefreshInterceptor", "Still 401 unauthorized after token clear. User needs to re-login")
            } else {
                DebugLogger.debugLog("TokenRefreshInterceptor", "Retry successful after token clear. Response code: ${response.code}")
            }
        }

        return response
    }
}
