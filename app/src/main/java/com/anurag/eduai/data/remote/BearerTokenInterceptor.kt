package com.anurag.eduai.data.remote

import android.content.Context
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds JWT Bearer token to every outgoing request.
 * Token is retrieved from storage if available.
 * Adds token in two ways for backend compatibility:
 * 1. Authorization: Bearer <token>
 * 2. X-API-Key: <token> (for backend that requires this header)
 */
class BearerTokenInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        // Get token from storage
        val token = TokenManager.getToken(context)

        if (token != null) {
            // Add standard Authorization Bearer header
            builder.addHeader("Authorization", "Bearer $token")

            // Also add as X-API-Key for backend compatibility
            builder.addHeader("X-API-Key", token)

            DebugLogger.debugLog("BearerTokenInterceptor", "Added JWT token. Authorization=Bearer + X-API-Key header to request ${original.url}")
        } else {
            DebugLogger.debugLog("BearerTokenInterceptor", "No JWT token found, request sent without auth headers ${original.url}")
        }

        return chain.proceed(builder.build())
    }
}
