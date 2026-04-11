package com.anurag.eduai.data.remote


import android.content.Context
import com.anurag.eduai.BuildConfig
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.utils.ErrorHandler
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

        // Logging interceptor
        val logging = HttpLoggingInterceptor { msg ->
            DebugLogger.debugLog("OkHttp", msg)
        }
        logging.level = HttpLoggingInterceptor.Level.BASIC

        // Bearer token interceptor for JWT authentication
        val bearerTokenInterceptor = BearerTokenInterceptor(context)
        DebugLogger.debugLog("RetrofitProvider", "Bearer token interceptor configured for JWT authentication")

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

        // Token refresh interceptor to handle 401 errors
        val tokenRefreshInterceptor = TokenRefreshInterceptor(context)

        val client = OkHttpClient.Builder()
            .addInterceptor(bearerTokenInterceptor)
            .addInterceptor(logging)
            .addNetworkInterceptor(tokenRefreshInterceptor)
            .addNetworkInterceptor(errorLoggingInterceptor)
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
            DebugLogger.errorLog("RetrofitProvider", "Error enumerating interceptors: ${e.message}")
        }

        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}