package com.anurag.eduai.domain.progress.usecase

import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.ConceptRepository
import javax.inject.Inject

/**
 * Use case for managing chapter completion based on multi-agent dependencies
 *
 * Chapter is marked COMPLETED only when ALL of these are true:
 * 1. Study Agent (Chat): Session completed (reached END node)
 * 2. All Simulations: Opened/Loaded
 * 3. Simulation Agent: Session started
 * 4. Math Agent (if present): First problem solved
 * 5. Science Agent (if present): All nodes completed (100%)
 */
class ChapterCompletionUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository
) {

    /**
     * Check if chapter is ready for completion
     * Returns true only if all required agents are completed
     */
    suspend fun isChapterComplete(
        studentId: String,
        chapterId: String
    ): Boolean {
        try {
            val concepts = conceptRepository.getConceptsForChapter(chapterId, "STUDY")

            if (concepts.isEmpty()) {
                DebugLogger.debugLog("ChapterCompletionUseCase", "No concepts found for chapter: $chapterId")
                return false
            }

            for (concept in concepts) {
                // Check Study Agent completion
                val studyProgress = conceptRepository.getProgress(
                    studentId = studentId,
                    itemType = "CONCEPT",
                    itemId = concept.conceptId
                )

                if (studyProgress?.status != "COMPLETED") {
                    DebugLogger.debugLog("ChapterCompletionUseCase", "Study agent not completed for concept: ${concept.conceptId}")
                    return false
                }

                // Check if concept has simulations - if yes, all must be completed
                if (!concept.simulationId.isNullOrBlank()) {
                    val simulationProgress = conceptRepository.getProgress(
                        studentId = studentId,
                        itemType = "SIMULATION",
                        itemId = concept.conceptId
                    )

                    // If simulation exists but not completed, chapter is incomplete
                    if (simulationProgress?.status != "COMPLETED") {
                        DebugLogger.debugLog("ChapterCompletionUseCase", "Simulation not completed for concept: ${concept.conceptId}")
                        return false
                    }

                    // Check Simulation Agent if exists
                    val simAgentProgress = conceptRepository.getProgress(
                        studentId = studentId,
                        itemType = "SIMULATION_AGENT",
                        itemId = concept.conceptId
                    )

                    if (simAgentProgress?.status != "COMPLETED") {
                        DebugLogger.debugLog("ChapterCompletionUseCase", "Simulation agent not completed for concept: ${concept.conceptId}")
                        return false
                    }
                }

                // Check Math Agent if exists (only first problem needs to be completed)
                val mathProgress = conceptRepository.getProgress(
                    studentId = studentId,
                    itemType = "MATH_AGENT",
                    itemId = concept.conceptId
                )

                if (concept.type == "math" && mathProgress?.status != "COMPLETED") {
                    DebugLogger.debugLog("ChapterCompletionUseCase", "Math agent not completed for concept: ${concept.conceptId}")
                    return false
                }

                // Check Science Agent if exists (must be at 100%)
                val scienceProgress = conceptRepository.getProgress(
                    studentId = studentId,
                    itemType = "SCIENCE_AGENT",
                    itemId = concept.conceptId
                )

                if (concept.type == "science" && (scienceProgress == null || scienceProgress.status != "COMPLETED" || scienceProgress.progressPercentage != 100)) {
                    DebugLogger.debugLog("ChapterCompletionUseCase", "Science agent not fully completed for concept: ${concept.conceptId}")
                    return false
                }
            }

            DebugLogger.debugLog("ChapterCompletionUseCase", "Chapter is complete: $chapterId")
            return true
        } catch (e: Exception) {
            DebugLogger.errorLog(
                "ChapterCompletionUseCase",
                "Error checking chapter completion: ${e.message}",
            )
            return false
        }
    }


    /**
     * Get chapter completion status details
     * Returns what agents are still pending
     */
    suspend fun getChapterCompletionStatus(
        studentId: String,
        chapterId: String
    ): ChapterCompletionStatus {
        try {
            val concepts = conceptRepository.getConceptsForChapter(chapterId, "STUDY")

            val incompleteConcepts = mutableListOf<ConceptCompletionDetail>()

            for (concept in concepts) {
                var detail = ConceptCompletionDetail(
                    conceptId = concept.conceptId,
                    conceptName = concept.conceptName,
                    studyAgentComplete = false,
                    mathAgentComplete = false,
                    simulationAgentComplete = false,
                    simulationComplete = false,
                    scienceAgentComplete = false
                )

                // Check each agent
                val studyProgress = conceptRepository.getProgress(
                    studentId = studentId,
                    itemType = "CONCEPT",
                    itemId = concept.conceptId
                )
                detail = detail.copy(studyAgentComplete = studyProgress?.status == "COMPLETED")

                val mathProgress = conceptRepository.getProgress(
                    studentId = studentId,
                    itemType = "MATH_AGENT",
                    itemId = concept.conceptId
                )
                detail = detail.copy(mathAgentComplete = mathProgress?.status == "COMPLETED")

                val simProgress = conceptRepository.getProgress(
                    studentId = studentId,
                    itemType = "SIMULATION",
                    itemId = concept.conceptId
                )
                detail = detail.copy(simulationComplete = simProgress?.status == "COMPLETED")

                val simAgentProgress = conceptRepository.getProgress(
                    studentId = studentId,
                    itemType = "SIMULATION_AGENT",
                    itemId = concept.conceptId
                )
                detail = detail.copy(simulationAgentComplete = simAgentProgress?.status == "COMPLETED")

                val scienceProgress = conceptRepository.getProgress(
                    studentId = studentId,
                    itemType = "SCIENCE_AGENT",
                    itemId = concept.conceptId
                )
                detail = detail.copy(scienceAgentComplete = scienceProgress?.status == "COMPLETED")

                incompleteConcepts.add(detail)
            }

            return ChapterCompletionStatus(
                chapterId = chapterId,
                isComplete = isChapterComplete(studentId, chapterId),
                conceptDetails = incompleteConcepts
            )
        } catch (e: Exception) {
            DebugLogger.errorLog(
                "ChapterCompletionUseCase",
                "Error getting chapter completion status: ${e.message}",
            )
            return ChapterCompletionStatus(
                chapterId = chapterId,
                isComplete = false,
                conceptDetails = emptyList()
            )
        }
    }
}

/**
 * Data class representing chapter completion status
 */
data class ChapterCompletionStatus(
    val chapterId: String,
    val isComplete: Boolean,
    val conceptDetails: List<ConceptCompletionDetail>
)

/**
 * Data class representing completion details for a single concept
 */
data class ConceptCompletionDetail(
    val conceptId: String,
    val conceptName: String,
    val studyAgentComplete: Boolean,
    val mathAgentComplete: Boolean,
    val simulationAgentComplete: Boolean,
    val simulationComplete: Boolean,
    val scienceAgentComplete: Boolean
) {
    val allAgentsComplete: Boolean
        get() = studyAgentComplete && mathAgentComplete && simulationAgentComplete && simulationComplete && scienceAgentComplete
}
