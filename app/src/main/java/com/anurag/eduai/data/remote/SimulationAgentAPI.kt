package com.anurag.eduai.data.remote

import com.anurag.eduai.data.model.SimHealthResponse
import com.anurag.eduai.data.model.SimQuizAnswerRequest
import com.anurag.eduai.data.model.SimSessionResponse
import com.anurag.eduai.data.model.SimSimulationsListResponse
import com.anurag.eduai.data.model.SimStartSessionRequest
import com.anurag.eduai.data.model.SimStudentResponseRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

class SimulationAgentAPI {
    companion object {
        // Remote server URL
        private const val BASE_URL = "http://13.48.59.144:8000"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /** Health check endpoint GET /simulation */
    suspend fun healthCheck(): SimHealthResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/simulation")
            .get()
            .build()

        executeRequest(request, SimHealthResponse::class.java)
    }

    /** Get all available simulations GET /simulation/simulations */
    suspend fun getAvailableSimulations(): SimSimulationsListResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/simulation/simulations")
            .get()
            .build()

        executeRequest(request, SimSimulationsListResponse::class.java)
    }

    /** Start a new teaching session POST /simulation/session/start */
    suspend fun startSession(request: SimStartSessionRequest): SimSessionResponse = withContext(Dispatchers.IO) {
        val json = moshi.adapter(SimStartSessionRequest::class.java).toJson(request)
        val requestBody = json.toRequestBody(JSON_MEDIA_TYPE)

        val httpRequest = Request.Builder()
            .url("$BASE_URL/simulation/session/start")
            .post(requestBody)
            .build()

        executeRequest(httpRequest, SimSessionResponse::class.java)
    }

    /** Send student response to session POST /simulation/session/{session_id}/respond */
    suspend fun sendResponse(sessionId: String, request: SimStudentResponseRequest): SimSessionResponse =
        withContext(Dispatchers.IO) {
            val json = moshi.adapter(SimStudentResponseRequest::class.java).toJson(request)
            val requestBody = json.toRequestBody(JSON_MEDIA_TYPE)

            val httpRequest = Request.Builder()
                .url("$BASE_URL/simulation/session/$sessionId/respond")
                .post(requestBody)
                .build()

            executeRequest(httpRequest, SimSessionResponse::class.java)
        }

    /** Submit quiz answer POST /simulation/session/{session_id}/submit-quiz */
    suspend fun submitQuizAnswer(sessionId: String, request: SimQuizAnswerRequest): SimSessionResponse =
        withContext(Dispatchers.IO) {
            val json = moshi.adapter(SimQuizAnswerRequest::class.java).toJson(request)
            val requestBody = json.toRequestBody(JSON_MEDIA_TYPE)

            val httpRequest = Request.Builder()
                .url("$BASE_URL/simulation/session/$sessionId/submit-quiz")
                .post(requestBody)
                .build()

            executeRequest(httpRequest, SimSessionResponse::class.java)
        }

    /** Get current session state GET /simulation/session/{session_id} */
    suspend fun getSession(sessionId: String): SimSessionResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/simulation/session/$sessionId")
            .get()
            .build()

        executeRequest(request, SimSessionResponse::class.java)
    }

    private fun <T> executeRequest(request: Request, responseClass: Class<T>): T {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response code: ${response.code}")
            }

            val body = response.body?.string()
                ?: throw IOException("Empty response body")

            return moshi.adapter(responseClass).fromJson(body)
                ?: throw IOException("Failed to parse response")
        }
    }

    /** Clean up resources */
    fun close() {
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }
}