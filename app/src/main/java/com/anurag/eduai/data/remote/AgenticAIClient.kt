package com.anurag.eduai.data.remote


import android.content.Context
import com.anurag.eduai.BuildConfig
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.utils.ErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Response
import java.io.IOException
import okhttp3.Headers

class AgenticAIClient(
    agenticAIBaseUrl: String,
    context: Context
) {
    val service: AgenticAIService

    private val _currentThreadId = MutableStateFlow<String?>(null)

    private val _currentSessionId = MutableStateFlow<String?>(null)

    init {
        val retrofit = RetrofitProvider.buildRetrofit(agenticAIBaseUrl, context)
        service = retrofit.create(AgenticAIService::class.java)
    }

    private suspend fun <T : Any> callWithRetry(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 300L,
        factor: Double = 2.0,
        call: suspend () -> Response<T>
    ): Result<T> {
        var attempt = 0
        var lastEx: Exception? = null
        var delayMs = initialDelayMs

        while (attempt < maxAttempts) {
            attempt++
            try {
                DebugLogger.debugLog("AgenticAIClient", "Call attempt=$attempt for call (starting)")
                val resp = call()
                try {
                    // Log response basic info
                    val urlStr = resp.raw().request.url.toString()
                    val code = resp.code()
                    DebugLogger.debugLog("AgenticAIClient", "Response received: attempt=$attempt url=$urlStr code=$code")
                    // Check whether header exists in the request (mask it if present)
                    val reqHeaders: Headers = resp.raw().request.headers
                    val headerName = BuildConfig.API_KEY_HEADER_NAME.trim().ifEmpty { "X-API-Key" }
                    val hv = reqHeaders[headerName]
                    if (hv != null) {
                        val masked = if (hv.length <= 6) "****" else "****" + hv.takeLast(4)
                        DebugLogger.debugLog("AgenticAIClient", "Request contained header $headerName with value=$masked")
                    } else {
                        DebugLogger.debugLog("AgenticAIClient", "Request did not contain header $headerName")
                    }
                } catch (inner: Exception) {
                    DebugLogger.errorLog("AgenticAIClient", "Error logging response metadata: ${inner.message}")
                }

                when {
                    resp.isSuccessful && resp.body() != null -> {
                        val body = resp.body()!!
                        val isAppSuccess = isApplicationSuccess(body)

                        if (isAppSuccess) {
                            return Result.success(body)
                        } else {
                            // App returned success=false
                            val message = getServerMessage(body)
                            lastEx = IOException("Server error: ${message ?: "Unknown error"}")
                            break
                        }
                    }

                    resp.isSuccessful -> {
                        lastEx = IOException("Empty response body (HTTP ${resp.code()})")
                        break
                    }

                    resp.code() in 500..599 -> {
                        val errBody = safeGetErrorBody(resp)
                        lastEx = IOException("HTTP ${resp.code()}: ${errBody ?: resp.message()}")

                        // Log server error - will retry if network/timeout
                        ErrorHandler.logError(
                            "AgenticAIClient",
                            resp.code(),
                            "Server error - will retry if transient"
                        )
                        // Don't break - allow retry for 5xx errors as they may be transient
                    }

                    resp.code() in 400..499 -> {
                        val errBody = safeGetErrorBody(resp)
                        lastEx = IOException("HTTP ${resp.code()}: ${errBody ?: resp.message()}")

                        // Don't retry 401, 403, 404
                        if (resp.code() in listOf(401, 403, 404)) {
                            DebugLogger.errorLog(
                                "AgenticAIClient",
                                "Client error ${resp.code()} - not retrying"
                            )
                            break
                        }
                        // Retry other 4xx errors (429, etc.)
                    }

                    else -> {
                        val errBody = safeGetErrorBody(resp)
                        lastEx = IOException("HTTP ${resp.code()}: ${errBody ?: resp.message()}")
                    }
                }
            } catch (e: Exception) {
                lastEx = e
                DebugLogger.errorLog(
                    "AgenticAIClient",
                    "Attempt $attempt/$maxAttempts failed: ${e.message}"
                )
            }

            // Retry logic
            if (attempt < maxAttempts && ErrorHandler.shouldRetryException(lastEx)) {
                DebugLogger.debugLog(
                    "AgenticAIClient",
                    "Retrying in ${delayMs}ms (attempt $attempt/$maxAttempts)"
                )
                delay(delayMs)
                delayMs = (delayMs * factor).toLong()
            } else if (attempt < maxAttempts && !ErrorHandler.shouldRetryException(lastEx)) {
                break  // Don't retry
            }
        }

        return Result.failure(lastEx ?: IOException("Unknown error after $maxAttempts attempts"))
    }

    private fun isApplicationSuccess(body: Any): Boolean {
        return when (body) {
            is StartSessionResponse -> body.success
            is ContinueSessionResponse -> body.success
            is SessionStatusResponse -> body.success
            is SessionHistoryResponse -> body.success
            is SessionSummaryResponse -> body.success
            is ConceptsListResponse -> body.success
            is TestImageResponse -> body.success
            is TranslationResponse -> body.success
            is RevisionChaptersResponse -> body.success
            is RevStartSessionResponse -> body.success
            is RevContinueSessionResponse -> body.success
            is RevSessionStatusResponse -> body.success
            is RevSessionHistoryResponse -> body.success
            is SimSessionResponse -> true
            is SimSimulationsListResponse -> true
            is SimHealthResponse -> true
            is HealthResponse -> true
            else -> true
        }
    }

    private fun getServerMessage(body: Any): String? {
        return when (body) {
            is StartSessionResponse -> body.message
            is ContinueSessionResponse -> body.message
            is SessionStatusResponse -> body.message
            is SessionHistoryResponse -> body.message
            is SessionSummaryResponse -> body.message
            is ConceptsListResponse -> body.message
            is TestImageResponse -> body.message
            is TranslationResponse -> body.error
            is RevisionChaptersResponse -> body.message
            is RevStartSessionResponse -> body.message
            is RevContinueSessionResponse -> body.message
            is RevSessionStatusResponse -> body.message
            is RevSessionHistoryResponse -> body.message
            is SimSessionResponse -> null
            is SimSimulationsListResponse -> null
            is SimHealthResponse -> null
            else -> null
        }
    }

    private fun safeGetErrorBody(resp: Response<*>): String? {
        return try {
            resp.errorBody()?.string()
        } catch (_: Exception) {
            null
        }
    }


    suspend fun startSession(
        conceptTitle: String,
        studentId: String,
        personaName: String? = null,
        sessionLabel: String? = null,
        isKannada: Boolean = false,
        studentLevel: String = "medium"
    ): Result<StartSessionResponse> = withContext(Dispatchers.IO) {
        val req = StartSessionRequest(
            conceptTitle = conceptTitle,
            studentId = studentId,
            personaName = personaName,
            sessionLabel = sessionLabel,
            isKannada = isKannada,
            studentLevel=studentLevel
        )

        val res = callWithRetry { service.startSession(req) }

        // Update state only on success
        if (res.isSuccess) {
            val body = res.getOrNull()
            body?.threadId?.let { _currentThreadId.value = it }
            body?.sessionId?.let { _currentSessionId.value = it }
            DebugLogger.debugLog(
                "AgenticAIClient",
                "Session started: threadId=${body?.threadId}, sessionId=${body?.sessionId}"
            )
        }
        res
    }

    suspend fun continueSession(
        userMessage: String,
        clickedAutosuggestion: Boolean,
        studentLevel: String,
        isKannada: Boolean
    ): Result<ContinueSessionResponse> =
        withContext(Dispatchers.IO) {
            val thread = _currentThreadId.value
                ?: return@withContext Result.failure(IOException("No active thread"))

            val req = ContinueSessionRequest(
                threadId = thread,
                userMessage = userMessage,
                clickedAutosuggestion = clickedAutosuggestion,
                studentLevel = studentLevel,
                isKannada = isKannada
            )

            val res = callWithRetry { service.continueSession(req) }

            // Update threadId if it changed
            if (res.isSuccess) {
                val body = res.getOrNull()
                body?.threadId?.let {
                    if (it != _currentThreadId.value) {
                        DebugLogger.debugLog(
                            "AgenticAIClient",
                            "ThreadId updated: ${_currentThreadId.value} -> $it"
                        )
                        DebugLogger.debugLog(
                            "AgenticAIClient",
                            "Call with clickedAutosuggestion: $clickedAutosuggestion, studentLevel: $studentLevel"
                        )
                        _currentThreadId.value = it
                    }
                }
            }

            res
        }

    fun setCurrentThreadAndSession(threadId: String?, sessionId: String?) {
        _currentThreadId.value = threadId
        _currentSessionId.value = sessionId
    }
    suspend fun getSessionStatus(threadId: String): Result<SessionStatusResponse> =
        withContext(Dispatchers.IO) {
            callWithRetry { service.getSessionStatus(threadId) }
        }

    suspend fun getSessionHistory(threadId: String): Result<SessionHistoryResponse> =
        withContext(Dispatchers.IO) {
            callWithRetry { service.getSessionHistory(threadId) }
        }

    suspend fun getConceptsList(): Result<ConceptsListResponse> =
        withContext(Dispatchers.IO) {
            callWithRetry { service.getAvailableConcepts() }
        }

    // Translation methods
    suspend fun translateToKannada(text: String): Result<TranslationResponse> =
        withContext(Dispatchers.IO) {
            val req = TranslationRequest(text)
            callWithRetry { service.translateToKannada(req) }
        }

    suspend fun translateToEnglish(text: String): Result<TranslationResponse> =
        withContext(Dispatchers.IO) {
            val req = TranslationRequest(text)
            callWithRetry { service.translateToEnglish(req) }
        }

    // Revision methods
    suspend fun getRevisionChapters(): Result<RevisionChaptersResponse> =
        withContext(Dispatchers.IO) {
            callWithRetry { service.getRevisionChapters() }
        }

    suspend fun startRevisionSession(
        chapter: String,
        studentId: String? = null,
        isKannada: Boolean = false,
        sessionLabel: String? = null
    ): Result<RevStartSessionResponse> = withContext(Dispatchers.IO) {
        val req = RevStartSessionRequest(
            chapter = chapter,
            studentId = studentId,
            isKannada = isKannada,
            sessionLabel = sessionLabel
        )

        val res = callWithRetry { service.startRevisionSession(req) }

        // Update state only on success
        if (res.isSuccess) {
            val body = res.getOrNull()
            body?.threadId?.let { _currentThreadId.value = it }
            body?.sessionId?.let { _currentSessionId.value = it }
            DebugLogger.debugLog(
                "AgenticAIClient",
                "Revision session started: threadId=${body?.threadId}, sessionId=${body?.sessionId}"
            )
        }
        res
    }

    suspend fun continueRevisionSession(
        threadId: String,
        userMessage: String,
        isKannada: Boolean? = null
    ): Result<RevContinueSessionResponse> = withContext(Dispatchers.IO) {
        val req = RevContinueSessionRequest(
            threadId = threadId,
            userMessage = userMessage,
            isKannada = isKannada
        )

        val res = callWithRetry { service.continueRevisionSession(req) }

        // Update threadId if it changed
        if (res.isSuccess) {
            val body = res.getOrNull()
            body?.threadId?.let {
                if (it != _currentThreadId.value) {
                    DebugLogger.debugLog(
                        "AgenticAIClient",
                        "Revision ThreadId updated: ${_currentThreadId.value} -> $it"
                    )
                    _currentThreadId.value = it
                }
            }
        }

        res
    }

    suspend fun getRevisionSessionStatus(threadId: String): Result<RevSessionStatusResponse> =
        withContext(Dispatchers.IO) {
            callWithRetry { service.getRevisionSessionStatus(threadId) }
        }

    suspend fun getRevisionSessionHistory(threadId: String): Result<RevSessionHistoryResponse> =
        withContext(Dispatchers.IO) {
            callWithRetry { service.getRevisionSessionHistory(threadId) }
        }

    suspend fun deleteRevisionSession(threadId: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val response = service.deleteRevisionSession(threadId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errBody = safeGetErrorBody(response)
                    Result.failure(IOException("HTTP ${response.code()}: ${errBody ?: response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    // ==================== SIMULATION METHODS ====================

    suspend fun simulationHealthCheck(): Result<SimHealthResponse> =
        withContext(Dispatchers.IO) {
            callWithRetry { service.simulationHealthCheck() }
        }

    suspend fun getAvailableSimulations(): Result<SimSimulationsListResponse>  =
        withContext(Dispatchers.IO) {
            callWithRetry { service.getAvailableSimulations() }
        }

    suspend fun startSimulationSession(
        simulationId: String,
        studentId: String? = null,
        language: String? = "english"
    ): Result<SimSessionResponse> = withContext(Dispatchers.IO) {
        val req = SimStartSessionRequest(
            simulationId = simulationId,
            studentId = studentId,
            language = language
        )

        val res = callWithRetry { service.startSimulationSession(req) }

        // Update session state on success
        if (res.isSuccess) {
            val body = res.getOrNull()
            body?.sessionId?.let { _currentSessionId.value = it }
            DebugLogger.debugLog(
                "AgenticAIClient",
                "Simulation session started: sessionId=${body?.sessionId}"
            )
        }
        res
    }

    suspend fun sendSimulationResponse(
        sessionId: String,
        studentResponse: String
    ): Result<SimSessionResponse> = withContext(Dispatchers.IO) {
        val req = SimStudentResponseRequest(studentResponse = studentResponse)
        callWithRetry { service.sendSimulationResponse(sessionId, req) }
    }

    suspend fun submitSimulationQuiz(
        sessionId: String,
        answer: String
    ): Result<SimSessionResponse> = withContext(Dispatchers.IO) {
        val req = SimQuizAnswerRequest(answer = answer)
        callWithRetry { service.submitSimulationQuiz(sessionId, req) }
    }

    suspend fun getSimulationSession(sessionId: String): Result<SimSessionResponse> =
        withContext(Dispatchers.IO) {
            callWithRetry { service.getSimulationSession(sessionId) }
        }
}