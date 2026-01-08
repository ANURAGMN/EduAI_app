package com.anurag.eduai.data.remote


import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.utils.ErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Response
import java.io.IOException

class AgenticAIClient(
    agenticAIBaseUrl: String
) {
    val service: AgenticAIService

    private val _currentThreadId = MutableStateFlow<String?>(null)

    private val _currentSessionId = MutableStateFlow<String?>(null)

    init {
        val retrofit = RetrofitProvider.buildRetrofit(agenticAIBaseUrl)
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
                val resp = call()

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
                        }
                    }

                    resp.isSuccessful -> {
                        lastEx = IOException("Empty response body (HTTP ${resp.code()})")
                    }

                    resp.code() in 500..599 -> {
                        val errBody = safeGetErrorBody(resp)
                        lastEx = IOException("${resp.code()}: ${errBody ?: resp.message()}")

                        // Log and break immediately
                        ErrorHandler.logError(
                            "AgenticAIClient",
                            resp.code(),
                            "Server error - failing immediately"
                        )
                        break
                    }

                    resp.code() in 400..499 -> {
                        val errBody = safeGetErrorBody(resp)
                        lastEx = IOException("${resp.code()}: ${errBody ?: resp.message()}")

                        // Don't retry 401, 403, 404
                        if (resp.code() in listOf(401, 403, 404)) {
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
            if (attempt < maxAttempts && shouldRetry(lastEx)) {
                DebugLogger.debugLog(
                    "AgenticAIClient",
                    "Retrying in ${delayMs}ms (attempt $attempt/$maxAttempts)"
                )
                delay(delayMs)
                delayMs = (delayMs * factor).toLong()
            } else if (attempt < maxAttempts && !shouldRetry(lastEx)) {
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
            is PersonasListResponse -> body.success
            is TestImageResponse -> body.success
            is TestSimulationResponse -> body.success
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
            is PersonasListResponse -> body.message
            is TestImageResponse -> body.message
            is TestSimulationResponse -> body.message
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

    private fun shouldRetry(exception: Exception?): Boolean {
        return when {
            exception == null -> true
            exception.message?.contains("HTTP 5") == true -> false  // 5xx: no retry
            exception.message?.contains("HTTP 4") == true -> false  // 4xx: no retry
            else -> true  // Network errors: retry
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
        studentLevel: String
    ): Result<ContinueSessionResponse> =
        withContext(Dispatchers.IO) {
            val thread = _currentThreadId.value
                ?: return@withContext Result.failure(IOException("No active thread"))

            val req = ContinueSessionRequest(
                threadId = thread,
                userMessage = userMessage,
                clickedAutosuggestion = clickedAutosuggestion,
                studentLevel = studentLevel
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
}