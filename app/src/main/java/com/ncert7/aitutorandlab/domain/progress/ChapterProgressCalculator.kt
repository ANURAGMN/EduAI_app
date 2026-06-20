package com.ncert7.aitutorandlab.domain.progress

import com.ncert7.aitutorandlab.domain.progress.model.ProgressStatus
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.repository.ConceptRepository
import com.ncert7.aitutorandlab.repository.ProgressRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calculates chapter progress from the progress table.
 *
 * Counts ONLY concepts visible on screen for the current language:
 * - Study/Math concepts: COMPLETED when CONCEPT or MATH_AGENT row = COMPLETED
 * - Simulation concepts: COMPLETED when ANY present component (SIMULATION_AGENT OR SIMULATION) = COMPLETED
 * - Revision also contributes: REVISION_AGENT = COMPLETED counts toward that concept's completion
 */
@Singleton
class ChapterProgressCalculator @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val progressRepository: ProgressRepository
) {
    companion object {
        private const val TAG = "ChapterProgressCalc"
        private const val MATH_SUBJECT_ID = "5c0a6b6d-7c6b-4f35-9d5b-9fd0fd8e8a01"
    }

    /**
     * Data class to hold detailed component-wise progress.
     */
    data class ChapterProgressCalculationResult(
        val overallPercentage: Int,
        val studyPercentage: Int,
        val simulationPercentage: Int,
        val revisionPercentage: Int
    )

    /**
     * Calculate chapter progress component-wise based on study, simulation, and revision.
     */
    suspend fun calculateDetailedChapterProgress(
        studentId: String,
        chapterId: String,
        language: String = "en"
    ): ChapterProgressCalculationResult {
        try {
            val chapter = conceptRepository.getChapter(chapterId) 
                ?: return ChapterProgressCalculationResult(0, 0, 0, 0)
            val isMathSubject = chapter.subjectId == MATH_SUBJECT_ID

            // Load ONLY the concepts that are visible on screen for this language
            val studyConcepts = if (isMathSubject) {
                conceptRepository.getMathProblemConceptsForChapter(chapterId)
            } else {
                conceptRepository.getStudyConceptsForChapter(chapterId)
            }

            val simulationConcepts = conceptRepository.getSimulationConceptsForChapter(chapterId, language)
            val hasRevision = chapter.revisionId.isNotEmpty()

            val components = mutableListOf<Int>()
            var studyPct = 0
            var simPct = 0
            var revPct = 0

            // 1. Study Component
            if (studyConcepts.isNotEmpty()) {
                var completedStudyCount = 0
                for (concept in studyConcepts) {
                    val studyDone = if (isMathSubject) {
                        val mathDone = progressRepository.getProgress(studentId, "MATH_AGENT", concept.conceptId, language)
                            ?.status == ProgressStatus.COMPLETED.value
                        val conceptDone = progressRepository.getProgress(studentId, "CONCEPT", concept.conceptId, language)
                            ?.status == ProgressStatus.COMPLETED.value
                        mathDone || conceptDone
                    } else {
                        progressRepository.getProgress(studentId, "CONCEPT", concept.conceptId, language)
                            ?.status == ProgressStatus.COMPLETED.value
                    }
                    if (studyDone) {
                        completedStudyCount++
                    }
                }
                studyPct = (completedStudyCount * 100) / studyConcepts.size
                components.add(studyPct)
            }

            // 2. Simulation Component
            if (simulationConcepts.isNotEmpty()) {
                var completedSimCount = 0
                for (concept in simulationConcepts) {
                    val simId = if (language.equals("kn", ignoreCase = true)) {
                        concept.simulationIdKannada
                    } else {
                        concept.simulationId
                    }
                    val simUrl = if (language.equals("kn", ignoreCase = true)) {
                        concept.simulationUrlKannada
                    } else {
                        concept.simulationUrl
                    }

                    val hasAgent = !simId.isNullOrBlank() && simId != "null" && simId.trim().lowercase() != "not found"
                    val hasUrl = !simUrl.isNullOrBlank() && simUrl != "null" && simUrl.trim().lowercase() != "not found"

                    val agentDone = progressRepository.getProgress(studentId, "SIMULATION_AGENT", concept.conceptId, language)
                        ?.status == ProgressStatus.COMPLETED.value
                    val urlDone = progressRepository.getProgress(studentId, "SIMULATION", concept.conceptId, language)
                        ?.status == ProgressStatus.COMPLETED.value

                    // Simulation is done if ANY present component is completed (OR logic)
                    val simulationDone = when {
                        hasAgent && hasUrl -> agentDone || urlDone
                        hasAgent -> agentDone
                        hasUrl -> urlDone
                        else -> false
                    }

                    if (simulationDone) {
                        completedSimCount++
                    }
                }
                simPct = (completedSimCount * 100) / simulationConcepts.size
                components.add(simPct)
            }

            // 3. Revision Component
            if (hasRevision) {
                val allConcepts = studyConcepts + simulationConcepts
                val isRevisionCompleted = allConcepts.any { concept ->
                    progressRepository.getProgress(studentId, "REVISION_AGENT", concept.conceptId, language)
                        ?.status == ProgressStatus.COMPLETED.value
                }
                revPct = if (isRevisionCompleted) 100 else 0
                components.add(revPct)
            }

            val overall = if (components.isNotEmpty()) {
                components.sum() / components.size
            } else {
                0
            }

            val overallClamped = overall.coerceIn(0, 100)
            DebugLogger.debugLog(
                TAG,
                "Chapter $chapterId [$language] components: Study=$studyPct%, Sim=$simPct%, Rev=$revPct% -> Overall=$overallClamped%"
            )

            return ChapterProgressCalculationResult(
                overallPercentage = overallClamped,
                studyPercentage = studyPct,
                simulationPercentage = simPct,
                revisionPercentage = revPct
            )
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error calculating detailed progress for $chapterId: ${e.message}")
            return ChapterProgressCalculationResult(0, 0, 0, 0)
        }
    }

    /**
     * Calculate chapter progress based on ONLY loaded concepts on screen
     * Returning overall percentage.
     */
    suspend fun calculateChapterProgress(
        studentId: String,
        chapterId: String,
        language: String = "en"
    ): Int {
        return calculateDetailedChapterProgress(studentId, chapterId, language).overallPercentage
    }



    /** Returns true if the chapter's stored progress is 100% COMPLETED */
    suspend fun isChapterFullyCompleted(
        studentId: String,
        chapterId: String,
        language: String = "en"
    ): Boolean {
        return try {
            progressRepository.getChapterAgentProgress(studentId, chapterId, language)
                ?.let { it.status == "COMPLETED" && it.overallPercentage >= 100 } ?: false
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error checking chapter completion: ${e.message}")
            false
        }
    }

    /** Count of all completed activities today (concepts + simulations) */
    suspend fun getTodayActivityCount(studentId: String): Int {
        return try {
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            val startOfDay = cal.timeInMillis
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
            cal.set(java.util.Calendar.MINUTE, 59)
            cal.set(java.util.Calendar.SECOND, 59)
            val endOfDay = cal.timeInMillis

            progressRepository.getTodayFullyCompletedActivityCount(studentId, startOfDay, endOfDay)
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error getting today activity count: ${e.message}")
            0
        }
    }

    /** 7-day activity summary for streak tracking */
    suspend fun getSevenDayActivitySummary(studentId: String): List<Pair<String, Int>> {
        return try {
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -7)
            val sevenDaysAgo = cal.timeInMillis
            progressRepository.getDailyCompletedActivityLast7Days(studentId, sevenDaysAgo)
                .map { Pair(it.date, it.count) }
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error getting 7-day activity summary: ${e.message}")
            emptyList()
        }
    }
}