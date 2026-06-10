package com.ncert7.aitutorandlab.domain.progress

import com.ncert7.aitutorandlab.data.local.entities.ConceptEntity
import com.ncert7.aitutorandlab.domain.progress.model.ProgressStatus
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.domain.revisionagent.usecase.RevisionUseCase
import com.ncert7.aitutorandlab.repository.ConceptRepository
import com.ncert7.aitutorandlab.repository.ProgressRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.round

/**
 * ✅ SIMPLIFIED: Real-time chapter progress from loaded concepts only
 *
 * KEY CHANGE: Counts ONLY concepts loaded on screen for the current language:
 * - totalConcepts = actual loaded concepts on screen (study + simulation)
 * - completedConcepts = count of those with all components completed
 * - progress% = (completed / total) * 100
 *
 * This is used by ProgressDao.getChapterWiseProgressFlow() which powers ALL 3 screens:
 * 1. ProgressScreen - chapter progress list
 * 2. ChapterScreen - chapter cards with progress bars
 * 3. ConceptScreen - chapter header progress card
 *
 * All three screens now show the SAME progress because they use the SAME flow.
 */
@Singleton
class ChapterProgressCalculator @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val progressRepository: ProgressRepository,
    private val revisionUseCase: RevisionUseCase
) {
    companion object {
        private const val TAG = "ChapterProgressCalc"
        private const val MATH_SUBJECT_ID = "5c0a6b6d-7c6b-4f35-9d5b-9fd0fd8e8a01"
    }

    /**
     * Calculate chapter progress based on ONLY loaded concepts on screen
     *
     * ✅ UNIFIED CALCULATION:
     * - Counts ONLY concepts visible for current language
     * - STUDY + SIMULATION + REVISION all count equally (same weight)
     * - For each concept: ALL required components must be COMPLETED
     * - Returns (completedConcepts / totalConcepts) * 100
     *
     * This is the SINGLE SOURCE OF TRUTH for all 3 screens:
     * 1. ProgressScreen
     * 2. ChapterScreen (cards)
     * 3. ConceptScreen (header)
     *
     * @param studentId Student ID
     * @param chapterId Chapter ID
     * @param language "en" or "kn"
     * @return Progress percentage (0-100) based on loaded concepts
     */
    suspend fun calculateChapterProgress(
        studentId: String,
        chapterId: String,
        language: String = "en"
    ): Int {
        return try {
            val chapter = conceptRepository.getChapter(chapterId) ?: return 0
            val isMathSubject = chapter.subjectId == MATH_SUBJECT_ID

            // Load ONLY the concepts that are visible on screen for this language
            val studyConcepts = if (isMathSubject) {
                conceptRepository.getMathProblemConceptsForChapter(chapterId)
            } else {
                conceptRepository.getStudyConceptsForChapter(chapterId)
            }

            val simulationConcepts = conceptRepository.getSimulationConceptsForChapter(chapterId, language)

            // REVISION: Check if chapter has revision agent and count revision concepts
            val hasRevisionAgent = checkChapterHasRevisionAgent(chapterId)
            val revisionConceptCount = if (hasRevisionAgent) {
                // Revision counts for all concepts in the chapter, but only if we're tracking revision progress
                studyConcepts.size + simulationConcepts.size
            } else {
                0
            }

            // Total loaded = study + simulation (revision is tracked separately in progress table)
            val totalLoaded = studyConcepts.size + simulationConcepts.size
            if (totalLoaded == 0) {
                DebugLogger.debugLog(TAG, "✅ Chapter $chapterId [$language]: No loaded concepts")
                return 0
            }

            var completedCount = 0
            val completedConceptsList = mutableListOf<String>()
            val totalConceptsList = mutableListOf<String>()

            // ===== COUNT COMPLETED STUDY/MATH CONCEPTS =====
            for (concept in studyConcepts) {
                totalConceptsList.add("${concept.conceptName}(STUDY)")

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

                // ALSO check if revision was completed (revision counts equally)
                val revisionDone = progressRepository.getProgress(studentId, "REVISION_AGENT", concept.conceptId, language)
                    ?.status == ProgressStatus.COMPLETED.value

                // Concept is complete if either study OR revision is done
                if (studyDone || revisionDone) {
                    completedCount++
                    completedConceptsList.add("${concept.conceptName}(STUDY-${if(studyDone)"Study" else "Revision"})")
                }
            }

            // ===== COUNT COMPLETED SIMULATION CONCEPTS =====
            for (concept in simulationConcepts) {
                totalConceptsList.add("${concept.conceptName}(SIM)")

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

                val hasAgent = !simId.isNullOrBlank() && simId != "null"
                val hasUrl = !simUrl.isNullOrBlank() && simUrl != "null"

                val agentDone = progressRepository.getProgress(studentId, "SIMULATION_AGENT", concept.conceptId, language)
                    ?.status == ProgressStatus.COMPLETED.value
                val urlDone = progressRepository.getProgress(studentId, "SIMULATION", concept.conceptId, language)
                    ?.status == ProgressStatus.COMPLETED.value

                // All required simulation components must be done
                val simulationDone = when {
                    hasAgent && hasUrl -> agentDone && urlDone
                    hasAgent -> agentDone
                    hasUrl -> urlDone
                    else -> false
                }

                // ALSO check if revision was completed (revision counts equally)
                val revisionDone = progressRepository.getProgress(studentId, "REVISION_AGENT", concept.conceptId, language)
                    ?.status == ProgressStatus.COMPLETED.value

                // Simulation is complete if either simulation components OR revision is done
                if (simulationDone || revisionDone) {
                    completedCount++
                    completedConceptsList.add("${concept.conceptName}(SIM-${if(simulationDone)"Sim" else "Revision"})")
                }
            }

            // Calculate and log progress
            val progress = if (totalLoaded > 0) {
                (completedCount * 100) / totalLoaded
            } else {
                0
            }

            DebugLogger.debugLog(
                TAG,
                "✅ Chapter $chapterId [$language]: $completedCount/$totalLoaded concepts = $progress%\n" +
                "   Total: ${totalConceptsList.joinToString(", ")}\n" +
                "   Completed: ${completedConceptsList.joinToString(", ")}\n" +
                "   (study=${studyConcepts.size}, sim=${simulationConcepts.size}, revision=${if (hasRevisionAgent) "enabled" else "disabled"})"
            )
            progress.coerceIn(0, 100)
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "❌ Error calculating progress for $chapterId: ${e.message}")
            0
        }
    }

    // ===== PRIVATE HELPERS =====

    /** Returns true if the chapter has a revision agent available */
    private suspend fun checkChapterHasRevisionAgent(chapterId: String): Boolean {
        return try {
            revisionUseCase.getAvailableChapters().contains(chapterId)
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error checking revision agent for $chapterId: ${e.message}")
            false
        }
    }


    // ===== PUBLIC HELPERS =====

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

            progressRepository.getTodayCompletedConceptCount(studentId, startOfDay, endOfDay) +
                    progressRepository.getTodayCompletedSimulationCount(studentId, startOfDay, endOfDay)
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