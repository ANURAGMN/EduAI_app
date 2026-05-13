package com.ncert7.aitutorandlab.data.remote


import android.content.Context
import com.ncert7.aitutorandlab.BuildConfig
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.utils.ErrorHandler
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.google.gson.GsonBuilder

object RetrofitProvider {
    fun buildRetrofit(agenticAIBaseUrl: String, context: Context): Retrofit {
        val buildConfigUrl = BuildConfig.AGENTIC_AI_BASE_URL.trim()
        val base = buildConfigUrl.ifEmpty { agenticAIBaseUrl.trim() }
        val normalized = base.trimEnd('/').ifEmpty {
            DebugLogger.errorLog("RetrofitProvider", "API base URL is empty.")
            throw IllegalArgumentException("API base URL required")
        } + "/"

        // Logging interceptor - use BODY level for debugging request/response bodies
        val logging = HttpLoggingInterceptor { msg ->
            DebugLogger.debugLog("OkHttp", msg)
        }
        logging.level = HttpLoggingInterceptor.Level.BODY

        // Proactive token interceptor to ensure valid token BEFORE API calls
        val proactiveTokenInterceptor = ProactiveTokenInterceptor(context)
        DebugLogger.debugLog("RetrofitProvider", "Proactive token interceptor configured - refreshes before API calls")

        val errorLoggingInterceptor = Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)

            // Log HTTP status codes
            if (!response.isSuccessful) {
                val statusCode = response.code
                val message = response.message
                ErrorHandler.logError(
                    "HttpError",
                    statusCode,
                    "$message - ${request.url}"
                )
            }
            response
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(proactiveTokenInterceptor)      // Ensure fresh token BEFORE request
            .addInterceptor(logging)                        //  Log request with fresh token
            .addNetworkInterceptor(errorLoggingInterceptor) //  Network-level error logging
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val gson = GsonBuilder()
            .serializeNulls()
            .create()

        DebugLogger.debugLog("RetrofitProvider", "Retrofit base url: $normalized")

        // After client built, log attached interceptors for easier troubleshooting
        try {
            val interceptorNames = client.interceptors.map { it.javaClass.simpleName }
            val networkInterceptorNames = client.networkInterceptors.map { it.javaClass.simpleName }
            DebugLogger.debugLog("RetrofitProvider", "OkHttp interceptors=${interceptorNames}, networkInterceptors=${networkInterceptorNames}")
        } catch (e: Exception) {
            DebugLogger.errorLog("RetrofitProvider","Error enumerating interceptors: ${e.message}")
        }

        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}