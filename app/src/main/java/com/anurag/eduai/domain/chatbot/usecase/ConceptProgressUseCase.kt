package com.anurag.eduai.domain.chatbot.usecase

import com.anurag.eduai.data.remote.SessionMetadata
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.ConceptRepository
import javax.inject.Inject

/**
 * Use case for managing concept progress tracking based on chatbot session flow.
 * Handles status updates: NOT_STARTED -> IN_PROGRESS -> COMPLETED
 * Tracks progress percentage using current state
 */
class ConceptProgressUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository
) {

    // Cumulative progress mapping up to a given state (used when calculating from current state)
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
     * Primary method: calculate progress percentage from an explicit currentState.
     * Uses metadata.node_transitions only to check whether the currentState was visited before
     * (for logging or special handling), but does not derive state from transitions.
     */
    fun calculateProgressPercentage(
        currentState: String?,
        metadata: SessionMetadata?
    ): Int {
        if (currentState.isNullOrBlank()) {
            DebugLogger.debugLog(
                "ConceptProgressUseCase",
                "calculateProgressPercentage called without currentState. Returning 0."
            )
            return 0
        }

        val normalizedState = currentState.uppercase().trim()

        val cumulativeProgress = conceptLearningProgressMap[normalizedState] ?: 0

        // If metadata is provided, build visited set only to check whether this state was already seen
        if (metadata != null && metadata.nodeTransitions.isNotEmpty()) {
            val visitedStates = mutableSetOf<String>()
            metadata.nodeTransitions.forEach { transition ->
                (transition["from_node"] as? String)?.let { fromNode ->
                    visitedStates.add(fromNode.uppercase().trim())
                }
                (transition["to_node"] as? String)?.let { toNode ->
                    visitedStates.add(toNode.uppercase().trim())
                }
            }

            val wasVisited = visitedStates.contains(normalizedState)
            DebugLogger.debugLog(
                "ConceptProgressUseCase",
                "Calculated progress from current state $normalizedState: $cumulativeProgress%. wasVisited=$wasVisited (visitedStates=${visitedStates.joinToString(",")})"
            )
        } else {
            DebugLogger.debugLog(
                "ConceptProgressUseCase",
                "Calculated progress from current state $normalizedState: $cumulativeProgress% (no metadata provided)"
            )
        }

        return cumulativeProgress.coerceIn(0, 100)
    }

    /**
     * Get visited states from node_transitions for debugging
     */
    fun getVisitedStates(metadata: SessionMetadata?): Set<String> {
        if (metadata == null || metadata.nodeTransitions.isEmpty()) {
            return emptySet()
        }

        val visitedStates = mutableSetOf<String>()

        metadata.nodeTransitions.forEach { transition ->
            (transition["from_node"] as? String)?.let { fromNode ->
                visitedStates.add(fromNode.uppercase().trim())
            }
            (transition["to_node"] as? String)?.let { toNode ->
                visitedStates.add(toNode.uppercase().trim())
            }
        }

        return visitedStates
    }

    /**
     * Marks concept as IN_PROGRESS when session starts successfully
     */
    suspend fun markConceptInProgress(
        studentId: String,
        conceptId: String
    ) {
        try {
            val currentProgress = conceptRepository.getProgress(
                studentId = studentId,
                itemType = "CONCEPT",
                itemId = conceptId
            )

            val currentStatus = currentProgress?.status ?: "NOT_STARTED"
            val progressPercentage = currentProgress?.progressPercentage ?: 5 // START state = 5%

            // Only update if not already completed
            if (currentStatus != "COMPLETED") {
                conceptRepository.updateProgressStatus(
                    studentId = studentId,
                    itemType = "CONCEPT",
                    itemId = conceptId,
                    newStatus = "IN_PROGRESS",
                    progressPercentage = progressPercentage,
                    timestamp = System.currentTimeMillis()
                )
                DebugLogger.debugLog("ConceptProgressUseCase", "Marked concept $conceptId as IN_PROGRESS")
            }
        } catch (e: Exception) {
            DebugLogger.errorLog("ConceptProgressUseCase", "Error marking concept as IN_PROGRESS: ${e.message}")
        }
    }

    /**
     * Marks concept as COMPLETED when END node is reached
     */
    suspend fun markConceptCompleted(
        studentId: String,
        conceptId: String
    ) {
        try {
            conceptRepository.updateProgressStatus(
                studentId = studentId,
                itemType = "CONCEPT",
                itemId = conceptId,
                newStatus = "COMPLETED",
                progressPercentage = 100,//completed = 100%
                timestamp = System.currentTimeMillis()
            )

            // Unlock next concept
            unlockNextConcept(studentId, conceptId)

            DebugLogger.debugLog("ConceptProgressUseCase", "Marked concept $conceptId as COMPLETED")
        } catch (e: Exception) {
            DebugLogger.errorLog("ConceptProgressUseCase", "Error marking concept as COMPLETED: ${e.message}")
        }
    }

    /**
     * Unlocks the next concept in the chapter when current concept is completed
     */
    private suspend fun unlockNextConcept(studentId: String, currentConceptId: String) {
        try {
            val currentConcept = conceptRepository.getConcept(currentConceptId) ?: return

            val allConcepts = conceptRepository.getConceptsForChapter(
                chapterId = currentConcept.chapterId,
                type = "STUDY"
            )

            val nextConcept = allConcepts.firstOrNull {
                it.orderIndex == currentConcept.orderIndex + 1
            }

            if (nextConcept != null) {
                val nextProgress = conceptRepository.getProgress(
                    studentId = studentId,
                    itemType = "CONCEPT",
                    itemId = nextConcept.conceptId
                )

                if (nextProgress == null || nextProgress.status == "NOT_STARTED") {
                    conceptRepository.updateProgressStatus(
                        studentId = studentId,
                        itemType = "CONCEPT",
                        itemId = nextConcept.conceptId,
                        newStatus = "IN_PROGRESS",
                        progressPercentage = 0, // Start with 0% for new concept
                        timestamp = System.currentTimeMillis()
                    )
                    DebugLogger.debugLog("ConceptProgressUseCase", "Unlocked next concept: ${nextConcept.conceptId}")
                }
            }
        } catch (e: Exception) {
            DebugLogger.errorLog("ConceptProgressUseCase", "Error unlocking next concept: ${e.message}")
        }
    }
}
