package com.anurag.eduai.data.remote

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AgenticAIService {
    //core session Endpoints
    @POST("/session/start")
    suspend fun startSession(@Body request: StartSessionRequest): Response<StartSessionResponse>

    @POST("/session/continue")
    suspend fun continueSession(@Body request: ContinueSessionRequest): Response<ContinueSessionResponse>

    @GET("/session/status/{thread_id}")
    suspend fun getSessionStatus(@Path("thread_id") threadId: String): Response<SessionStatusResponse>

    @GET("/session/history/{thread_id}")
    suspend fun getSessionHistory(@Path("thread_id") threadId: String): Response<SessionHistoryResponse>

    @GET("/session/summary/{thread_id}")
    suspend fun getSessionSummary(@Path("thread_id") threadId: String): Response<SessionSummaryResponse>

    @GET("/concepts")
    suspend fun getAvailableConcepts(): Response<ConceptsListResponse>

    //Utility Endpoints
    @GET("/health")
    suspend fun healthCheck(): Response<HealthResponse>

    //test endpoints
    @POST("/test/image")
    suspend fun getTestImage(@Body request: TestImageRequest): Response<TestImageResponse>

    //translation endpoints
    @POST("/translate/to-kannada")
    suspend fun translateToKannada(@Body request: TranslationRequest): Response<TranslationResponse>

    @POST("/translate/to-english")
    suspend fun translateToEnglish(@Body request: TranslationRequest): Response<TranslationResponse>

    //revision endpoints
    @GET("/revision/chapters")
    suspend fun getRevisionChapters(): Response<RevisionChaptersResponse>

    @POST("/revision/session/start")
    suspend fun startRevisionSession(@Body request: RevStartSessionRequest): Response<RevStartSessionResponse>

    @POST("/revision/session/continue")
    suspend fun continueRevisionSession(@Body request: RevContinueSessionRequest): Response<RevContinueSessionResponse>

    @GET("/revision/session/status/{thread_id}")
    suspend fun getRevisionSessionStatus(@Path("thread_id") threadId: String): Response<RevSessionStatusResponse>

    @GET("/revision/session/history/{thread_id}")
    suspend fun getRevisionSessionHistory(@Path("thread_id") threadId: String): Response<RevSessionHistoryResponse>

    @DELETE("/revision/session/{thread_id}")
    suspend fun deleteRevisionSession(@Path("thread_id") threadId: String): Response<String>

    // ==================== SIMULATION ENDPOINTS ====================
    @GET("/simulation")
    suspend fun simulationHealthCheck(): Response<SimHealthResponse>

    @GET("/simulation/simulations")
    suspend fun getAvailableSimulations(): Response<SimSimulationsListResponse>

    @POST("/simulation/session/start")
    suspend fun startSimulationSession(@Body request: SimStartSessionRequest): Response<SimSessionResponse>

    @POST("/simulation/session/{session_id}/respond")
    suspend fun sendSimulationResponse(
        @Path("session_id") sessionId: String,
        @Body request: SimStudentResponseRequest
    ): Response<SimSessionResponse>

    @POST("/simulation/session/{session_id}/submit-quiz")
    suspend fun submitSimulationQuiz(
        @Path("session_id") sessionId: String,
        @Body request: SimQuizAnswerRequest
    ): Response<SimSessionResponse>

    @GET("/simulation/session/{session_id}")
    suspend fun getSimulationSession(@Path("session_id") sessionId: String): Response<SimSessionResponse>
}

//All Data Classes
data class StartSessionRequest(
    @SerializedName("concept_title") val conceptTitle: String,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("persona_name") val personaName: String? = null,
    @SerializedName("session_label") val sessionLabel: String? = null,
    @SerializedName("is_kannada") val isKannada: Boolean = false,
    @SerializedName("student_level") val studentLevel: String = "medium"
)

data class ContinueSessionRequest(
    @SerializedName("thread_id") val threadId: String,
    @SerializedName("user_message") val userMessage: String,
    @SerializedName("clicked_autosuggestion") val clickedAutosuggestion: Boolean? = false,
    @SerializedName("is_kannada") val isKannada: Boolean = false,
    @SerializedName("student_level") val studentLevel: String? = null
)


data class TestImageRequest(
    @SerializedName("concept_title") val conceptTitle: String,
    @SerializedName("definition_context") val definitionContext: String = ""
)

data class TranslationRequest(
    @SerializedName("text") val text: String,
)
data class SessionMetadata(
    @SerializedName("show_simulation") val showSimulation: Boolean? = false,
    @SerializedName("simulation_config") val simulationConfig: Map<String,Any>? = emptyMap(),
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("image_description") val imageDescription : String? = null,
    @SerializedName("image_node") val imageNode: String? = null,
    @SerializedName("video_url") val videoUrl: String? = null,
    @SerializedName("video_node") val videoNode: String? = null,
    @SerializedName("quiz_score") val quizScore: Float? = null,
    @SerializedName("retrieval_score") val retrievalScore: Float? = null,
    @SerializedName("sim_concepts") val simConcepts: List<String>? = null,
    @SerializedName("sim_current_idx") val simCurrentIdx: Int? = null,
    @SerializedName("sim_total_concepts") val simTotalConcepts: Int? = null,
    @SerializedName("misconception_detected") val misconceptionDetected: Boolean? = false,
    @SerializedName("last_correction") val lastCorrection: String? = "",
    @SerializedName("node_transitions") val nodeTransitions: List<Map<String,Any>> = emptyList()
)

data class StartSessionResponse(
    val success: Boolean = false,
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("thread_id") val threadId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("agent_response") val agentResponse: String,
    @SerializedName("current_state") val currentState: String? = null,
    @SerializedName("concept_title") val conceptTitle: String? = null,
    val message: String? = "Session started successfully",
    val metadata: SessionMetadata = SessionMetadata(),
    val autosuggestions: List<String> = emptyList()
)

data class ContinueSessionResponse(
    val success: Boolean = false,
    @SerializedName("thread_id") val threadId: String? = null,
    @SerializedName("agent_response") val agentResponse: String? = null,
    @SerializedName("current_state") val currentState: String? = null,
    val metadata: SessionMetadata = SessionMetadata(),
    val message: String? = "Response generated successfully",
    val autosuggestions: List<String> = emptyList()
)

data class TestImageResponse(
    val success: Boolean = false,
    val concept: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("image_description") val imageDescription: String? = null,
    val message: String? = null
)

data class ConceptsListResponse(
    val success: Boolean = true,
    val concepts: List<String> = emptyList(),
    val total: Int = 0,
    val message: String? = "Available concepts retrieved successfully"
)

data class TranslationResponse(
    @SerializedName("original") val original: String,
    @SerializedName("translated") val translated: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("error") val error: String? = null
)

data class HealthResponse(
    val status: String,
    val version: String,
    val persistence: String,
    @SerializedName("agent_type") val agentType: String,
    @SerializedName("available_endpoints") val availableEndpoints: List<String>
)

data class SessionStatusResponse(
    val success: Boolean = false,
    @SerializedName("thread_id") val threadId: String? = null,
    val exists: Boolean = false,
    @SerializedName("current_state") val currentState: String? = null,
    val progress: Map<String, Any>? = null,
    @SerializedName("concept_title") val conceptTitle: String? = null,
    val message: String? = "Status retrieved successfully"
)
data class SessionHistoryResponse(
    val success: Boolean = false,
    @SerializedName("thread_id") val threadId: String? = null,
    val exists: Boolean = false,
    val messages: List<Map<String, Any>> = emptyList(),
    @SerializedName("node_transitions") val nodeTransitions: List<Map<String, Any>> = emptyList(),
    @SerializedName("concept_title") val conceptTitle: String? = null,
    val message: String? = "History retrieved successfully")

data class SessionSummaryResponse(
    val success: Boolean = false,
    @SerializedName("thread_id") val threadId: String? = null,
    val exists: Boolean = false,
    val summary: Map<String, Any>? = null,
    @SerializedName("quiz_score") val quizScore: Float? = null,
    @SerializedName("transfer_success") val transferSuccess: Boolean? = null,
    @SerializedName("misconception_detected") val misconceptionDetected: Boolean? = null,
    @SerializedName("definition_echoed") val definitionEchoed: Boolean? = null,
    val message: String? = "Summary retrieved successfully")

// Revision endpoints data classes
data class RevisionChaptersResponse(
    val success: Boolean = true,
    val chapters: List<String> = emptyList(),
    val total: Int = 0,
    val message: String? = "Available chapters retrieved successfully."
)

data class RevStartSessionRequest(
    @SerializedName("chapter") val chapter: String,
    @SerializedName("student_id") val studentId: String? = null,
    @SerializedName("is_kannada") val isKannada: Boolean = false,
    @SerializedName("session_label") val sessionLabel: String? = null
)

data class RevStartSessionResponse(
    val success: Boolean = false,
    @SerializedName("thread_id") val threadId: String,
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("chapter") val chapter: String,
    @SerializedName("agent_response") val agentResponse: String,
    @SerializedName("current_state") val currentState: String,
    val message: String? = "Revision session started successfully"
)

data class RevContinueSessionRequest(
    @SerializedName("thread_id") val threadId: String,
    @SerializedName("user_message") val userMessage: String,
    @SerializedName("is_kannada") val isKannada: Boolean? = null
)

data class RevContinueSessionResponse(
    val success: Boolean = false,
    @SerializedName("thread_id") val threadId: String,
    @SerializedName("agent_response") val agentResponse: String,
    @SerializedName("current_state") val currentState: String,
    val message: String? = "Revision response generated successfully"
)

data class RevSessionStatusResponse(
    val success: Boolean = false,
    @SerializedName("thread_id") val threadId: String,
    val exists: Boolean = false,
    @SerializedName("current_state") val currentState: String? = null,
    @SerializedName("chapter") val chapter: String? = null,
    val progress: Map<String, Any>? = null,
    val message: String? = "Revision status retrieved successfully"
)

data class RevSessionHistoryResponse(
    val success: Boolean = false,
    @SerializedName("thread_id") val threadId: String,
    val exists: Boolean = false,
    val messages: List<Map<String, Any>>? = null,
    @SerializedName("node_transitions") val nodeTransitions: List<Map<String, Any>>? = null,
    @SerializedName("chapter") val chapter: String? = null,
    val message: String? = "Revision history retrieved successfully"
)

//Simulation data classes

@Serializable
data class SimStartSessionRequest(
    @SerialName("simulation_id") val simulationId: String,
    @SerialName("student_id") val studentId: String? = null,
    @SerialName("language") val language:String?="english"
)

@Serializable
data class SimStudentResponseRequest(@SerialName("student_response") val studentResponse: String)

@Serializable
data class SimQuizAnswerRequest(val answer: String)

// ==================== RESPONSE MODELS ====================

@Serializable
data class SimHealthResponse(
    val status: String,
    val service: String,
    val version: String,
    @SerialName("available_simulations") val availableSimulations: List<String>
)

@Serializable
data class SimSimulationsListResponse(
    val simulations: List<SimSimulationMetadata>
)

@Serializable
data class SimSimulationMetadata(
    val id: String,
    val title: String,
    val description: String,
    @SerialName("concept_count") val conceptCount: Int? = null,
    val tags: List<String>? = null
)

@Serializable
data class SimSessionResponse(
    @SerialName("session_id") val sessionId: String,
    val simulation: SimSimulationState,
    val concepts: SimConceptsInfo,
    @SerialName("teacher_message") val teacherMessage: SimTeacherMessage,
    @SerialName("learning_state") val learningState: SimLearningState,
    @SerialName("language")val language: String?="english",
    val summary: Map<String, String>? = null
)

@Serializable
data class SimSimulationState(
    val id: String,
    val title: String,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("current_params") val currentParams: Map<String, JsonElement>,
    @SerialName("param_change") val paramChange: SimParameterChange? = null
)

@Serializable
data class SimParameterChange(
    val parameter: String,
    val before: JsonElement? = null,
    val after: JsonElement? = null,
    val reason: String? = null,
    @SerialName("before_url") val beforeUrl: String? = null,
    @SerialName("after_url") val afterUrl: String? = null
)

@Serializable
data class SimConceptsInfo(
    val total: Int,
    @SerialName("current_index") val currentIndex: Int,
    @SerialName("current_concept") val currentConcept: SimConcept? = null,
    @SerialName("all_concepts") val allConcepts: List<SimConcept> = emptyList(),
    @SerialName("all_completed") val allCompleted: Boolean? = false,
    @SerialName("previous_concept") val previousConcept: SimPreviousConcept? = null
)

@Serializable
data class SimConcept(
    val id: Int,
    val title: String,
    val description: String,
    @SerialName("key_insight") val keyInsight: String,
    @SerialName("related_params") val relatedParams: List<String>
)

@Serializable
data class SimPreviousConcept(
    val id: Int,
    val title: String,
    val completed: Boolean
)

@Serializable
data class SimTeacherMessage(
    val text: String,
    val timestamp: String,
    @SerialName("requires_response") val requiresResponse: Boolean,
    @SerialName("correction_made") val correctionMade: Boolean? = false,
    @SerialName("asks_for_reasoning") val asksForReasoning: Boolean? = false,
    @SerialName("concept_transition") val conceptTransition: Boolean? = false,
    @SerialName("session_ending") val sessionEnding: Boolean? = false
)

@Serializable
data class SimLearningState(
    @SerialName("understanding_level") val understandingLevel: String,
    @SerialName("understanding_reasoning") val understandingReasoning: String? = null,
    @SerialName("exchange_count") val exchangeCount: Int,
    @SerialName("concept_complete") val conceptComplete: Boolean,
    @SerialName("session_complete") val sessionComplete: Boolean,
    val strategy: String,
    @SerialName("teacher_mode") val teacherMode: String,
    @SerialName("trajectory_status") val trajectoryStatus: String? = null,
    @SerialName("needs_deeper") val needsDeeper: Boolean? = false
)