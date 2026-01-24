package com.anurag.eduai.data.remote

import com.anurag.eduai.data.model.SimHealthResponse
import com.anurag.eduai.data.model.SimQuizAnswerRequest
import com.anurag.eduai.data.model.SimSessionResponse
import com.anurag.eduai.data.model.SimSimulationsListResponse
import com.anurag.eduai.data.model.SimStartSessionRequest
import com.anurag.eduai.data.model.SimStudentResponseRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

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

        executeRequest(request) { body ->
            json.decodeFromString<SimHealthResponse>(body)
        }
    }

    /** Get all available simulations GET /simulation/simulations */
    suspend fun getAvailableSimulations(): SimSimulationsListResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/simulation/simulations")
            .get()
            .build()

        executeRequest(request) { body ->
            json.decodeFromString<SimSimulationsListResponse>(body)
        }
    }

    /** Start a new teaching session POST /simulation/session/start */
    suspend fun startSession(request: SimStartSessionRequest): SimSessionResponse = withContext(Dispatchers.IO) {
        val jsonString = json.encodeToString(request)
        val requestBody = jsonString.toRequestBody(JSON_MEDIA_TYPE)

        val httpRequest = Request.Builder()
            .url("$BASE_URL/simulation/session/start")
            .post(requestBody)
            .build()

        executeRequest(httpRequest) { body ->
            json.decodeFromString<SimSessionResponse>(body)
        }
    }

    /** Send student response to session POST /simulation/session/{session_id}/respond */
    suspend fun sendResponse(sessionId: String, request: SimStudentResponseRequest): SimSessionResponse =
        withContext(Dispatchers.IO) {
            val jsonString = json.encodeToString(request)
            val requestBody = jsonString.toRequestBody(JSON_MEDIA_TYPE)

            val httpRequest = Request.Builder()
                .url("$BASE_URL/simulation/session/$sessionId/respond")
                .post(requestBody)
                .build()

            executeRequest(httpRequest) { body ->
                json.decodeFromString<SimSessionResponse>(body)
            }
        }

    /** Submit quiz answer POST /simulation/session/{session_id}/submit-quiz */
    suspend fun submitQuizAnswer(sessionId: String, request: SimQuizAnswerRequest): SimSessionResponse =
        withContext(Dispatchers.IO) {
            val jsonString = json.encodeToString(request)
            val requestBody = jsonString.toRequestBody(JSON_MEDIA_TYPE)

            val httpRequest = Request.Builder()
                .url("$BASE_URL/simulation/session/$sessionId/submit-quiz")
                .post(requestBody)
                .build()

            executeRequest(httpRequest) { body ->
                json.decodeFromString<SimSessionResponse>(body)
            }
        }

    /** Get current session state GET /simulation/session/{session_id} */
    suspend fun getSession(sessionId: String): SimSessionResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/simulation/session/$sessionId")
            .get()
            .build()

        executeRequest(request) { body ->
            json.decodeFromString<SimSessionResponse>(body)
        }
    }

    private fun <T> executeRequest(request: Request, decoder: (String) -> T): T {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response code: ${response.code}")
            }

            val body = response.body?.string()
                ?: throw IOException("Empty response body")

            return decoder(body)
        }
    }

    /** Clean up resources */
    fun close() {
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }
}