package com.anurag.eduai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ==================== REQUEST MODELS ====================

@Serializable
data class SimStartSessionRequest(
    @SerialName("simulation_id") val simulationId: String,
    @SerialName("student_id") val studentId: String? = null
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
    val summary: Map<String, String>? = null
)

@Serializable
data class SimSimulationState(
    val id: String,
    val title: String,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("current_params") val currentParams: Map<String, Int>,
    @SerialName("param_change") val paramChange: SimParameterChange? = null
)

@Serializable
data class SimParameterChange(
    val parameter: String,
    val before: Int,
    val after: Int,
    val reason: String,
    @SerialName("before_url") val beforeUrl: String,
    @SerialName("after_url") val afterUrl: String
)

@Serializable
data class SimConceptsInfo(
    val total: Int,
    @SerialName("current_index") val currentIndex: Int,
    @SerialName("current_concept") val currentConcept: SimConcept? = null,
    @SerialName("all_concepts") val allConcepts: List<SimConcept> = emptyList()
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