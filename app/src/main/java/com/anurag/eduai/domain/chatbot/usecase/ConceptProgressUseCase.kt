package com.anurag.eduai.domain.chatbot.usecase

import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.ConceptRepository
import javax.inject.Inject

/**
 * Use case for managing concept progress tracking based on chatbot session flow.
 * Handles status updates: NOT_STARTED -> IN_PROGRESS -> COMPLETED
 */
class ConceptProgressUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository
) {

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

            // Only update if not already completed
            if (currentStatus != "COMPLETED") {
                conceptRepository.updateProgressStatus(
                    studentId = studentId,
                    itemType = "CONCEPT",
                    itemId = conceptId,
                    newStatus = "IN_PROGRESS",
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
