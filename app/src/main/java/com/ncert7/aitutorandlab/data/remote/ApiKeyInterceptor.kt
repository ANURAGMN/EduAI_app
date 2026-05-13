package com.ncert7.aitutorandlab.data.remote

import com.ncert7.aitutorandlab.BuildConfig
import com.ncert7.aitutorandlab.debug.DebugLogger
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds an API key header to every outgoing request.
 * Header name and value come from BuildConfig fields (populated from local.properties).
 */
class ApiKeyInterceptor : Interceptor {
    private val apiKey: String = BuildConfig.API_KEYS.trim()
    private val apiKeyHeader: String = BuildConfig.API_KEY_HEADER_NAME.trim().ifEmpty { "X-API-Key" }

    private fun maskKey(key: String): String {
        if (key.isEmpty()) return ""
        return if (key.length <= 6) "****" else "****" + key.takeLast(4)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
        if (apiKey.isNotEmpty()) {
            builder.addHeader(apiKeyHeader, apiKey)
            DebugLogger.debugLog("ApiKeyInterceptor", "Added header '$apiKeyHeader' with value='${maskKey(apiKey)}' to request ${original.url}")
        } else {
            DebugLogger.debugLog("ApiKeyInterceptor", "No API key configured; header not added for request ${original.url}")
        }
        return chain.proceed(builder.build())
    }
}
