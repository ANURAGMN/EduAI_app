package com.anurag.eduai.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Minimal request/response data classes for the example endpoint
data class StartSessionPayload(
    val concept_title: String,
    val student_id: String,
    val persona_name: String? = null,
    val session_label: String? = null,
    val is_kannada: Boolean = false,
    val student_level: String = "medium"
)

data class StartSessionResult(
    val success: Boolean = false,
    val session_id: String? = null,
    val thread_id: String? = null,
    val user_id: String? = null,
    val agent_response: String? = null,
    val message: String? = null
)

interface ApiService {
    @POST("/session/start")
    suspend fun startSession(@Body payload: StartSessionPayload): Response<StartSessionResult>
}
