package com.anurag.eduai.data.remote


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
    fun buildRetrofit(agenticAIBaseUrl: String): Retrofit {
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
            .addInterceptor(logging)
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

        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}