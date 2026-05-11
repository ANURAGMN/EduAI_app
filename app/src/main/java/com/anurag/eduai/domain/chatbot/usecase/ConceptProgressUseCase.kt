package com.anurag.eduai.domain.chatbot.usecase

import com.anurag.eduai.data.remote.SessionMetadata
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.domain.progress.ProgressEventTracker
import com.anurag.eduai.repository.ConceptRepository
import javax.inject.Inject

/**
 * Chatbot Concept Progress Tracking Use Case
 *
 * Handles concept progress based on chatbot session flow.
 * Integrates directly with ProgressEventTracker for all progress updates.
 *
 * Flow:
 * - Session starts → markConceptInProgress()
 * - Reaches END node → markConceptCompleted()
 * - Next concept unlocked automatically
 */
class ConceptProgressUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val tracker: ProgressEventTracker
) {

    // Cumulative progress mapping (APK→CI→SIM_CC→GE→AR→TC→END)
    private val conceptLearningProgressMap = mapOf(
        "APK" to 15,
        "CI" to 30,
        "SIM_CC" to 45,
        "GE" to 65,
        "AR" to 75,
        "TC" to 100,
        "END" to 100
    )

    /**
     * Calculate progress percentage from current state
     */
    fun calculateProgressPercentage(currentState: String?, metadata: SessionMetadata?): Int {
        if (currentState.isNullOrBlank()) {
            DebugLogger.debugLog("ConceptProgressUseCase", "calculateProgressPercentage: no currentState")
            return 0
        }

        val normalizedState = currentState.uppercase().trim()
        val cumulativeProgress = conceptLearningProgressMap[normalizedState] ?: 0

        if (metadata != null && metadata.nodeTransitions.isNotEmpty()) {
            val visitedStates = mutableSetOf<String>()
            metadata.nodeTransitions.forEach { transition ->
                (transition["from_node"] as? String)?.let { visitedStates.add(it.uppercase().trim()) }
                (transition["to_node"] as? String)?.let { visitedStates.add(it.uppercase().trim()) }
            }
            DebugLogger.debugLog(TAG, "Progress: $normalizedState = $cumulativeProgress%")
        }

        return cumulativeProgress.coerceIn(0, 100)
    }

    /**
     * Get visited states from session metadata
     */
    fun getVisitedStates(metadata: SessionMetadata?): Set<String> {
        if (metadata == null || metadata.nodeTransitions.isEmpty()) return emptySet()

        val visitedStates = mutableSetOf<String>()
        metadata.nodeTransitions.forEach { transition ->
            (transition["from_node"] as? String)?.let { visitedStates.add(it.uppercase().trim()) }
            (transition["to_node"] as? String)?.let { visitedStates.add(it.uppercase().trim()) }
        }
        return visitedStates
    }

    /**
     * Mark concept as IN_PROGRESS when chatbot session starts
     */
    suspend fun markConceptInProgress(studentId: String, conceptId: String) {
        try {
            tracker.markStudyInProgress(studentId, conceptId)
            DebugLogger.debugLog(TAG, "Concept IN_PROGRESS: $conceptId")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error marking IN_PROGRESS: ${e.message}")
        }
    }

    /**
     * Mark concept as COMPLETED when END node reached
     */
    suspend fun markConceptCompleted(studentId: String, conceptId: String) {
        try {
            tracker.markStudyCompleted(studentId, conceptId)
            unlockNextConcept(studentId, conceptId)
            DebugLogger.debugLog(TAG, "Concept COMPLETED: $conceptId")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error marking COMPLETED: ${e.message}")
        }
    }

    /**
     * Unlock next concept when current is completed
     */
    private suspend fun unlockNextConcept(studentId: String, currentConceptId: String) {
        try {
            val currentConcept = conceptRepository.getConcept(currentConceptId) ?: return
            val allConcepts = conceptRepository.getConceptsForChapter(currentConcept.chapterId, "STUDY")
            val nextConcept = allConcepts.firstOrNull { it.orderIndex == currentConcept.orderIndex + 1 }

            if (nextConcept != null) {
                val nextProgress = conceptRepository.getProgress(studentId, "CONCEPT", nextConcept.conceptId)
                if (nextProgress == null || nextProgress.status == "NOT_STARTED") {
                    tracker.markStudyInProgress(studentId, nextConcept.conceptId)
                    DebugLogger.debugLog(TAG, "Unlocked next: ${nextConcept.conceptId}")
                }
            }
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error unlocking next: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "ConceptProgressUseCase"
    }
}
