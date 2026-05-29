package com.ncert7.aitutorandlab.domain.progress

import android.content.Context
import com.ncert7.aitutorandlab.domain.progress.model.ProgressStatus
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.repository.ConceptRepository
import com.ncert7.aitutorandlab.repository.ProgressRepository
import com.ncert7.aitutorandlab.repository.StreakRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Progress Event Tracker — Central entry point for all progress state updates.
 *
 * This is the ONLY class used for recording progress events throughout the app.
 * Every call:
 *   1. Writes the progress row via ProgressRepository
 *   2. Triggers chapter-level recalculation via ChapterProgressService
 *   3. Records a streak activity via StreakRepository
 *
 * itemType constants (keep in sync with ChapterProgressCalculator):
 *   CONCEPT          — study session progress (non-math)
 *   MATH_AGENT       — math problem session progress
 *   SIMULATION_AGENT — simulation agent session progress
 *   SIMULATION       — simulation URL load progress
 *   REVISION_AGENT   — revision session progress
 *   SCIENCE_AGENT    — science agent progress
 */
@Singleton
class ProgressEventTracker @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val conceptRepository: ConceptRepository,
    private val chapterProgressService: ChapterProgressService,
    private val streakRepository: StreakRepository,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ProgressEventTracker"
    }

    /**
     * Look up which chapter a concept belongs to, then trigger chapter progress recalculation.
     * Ensures chapter_agent_progress is always up-to-date after any concept change.
     */
    private suspend fun triggerChapterProgressUpdate(
        studentId: String,
        conceptId: String,
        language: String = if (com.ncert7.aitutorandlab.utils.isKannada()) "kn" else "en"
    ) {
        try {
            val concept = conceptRepository.getConcept(conceptId)
            if (concept == null) {
                DebugLogger.errorLog(TAG, "Concept not found id=$conceptId — chapter progress not updated")
                return
            }
            val progress = chapterProgressService.updateChapterProgress(studentId, concept.chapterId, language)
            DebugLogger.debugLog(TAG, "Chapter ${concept.chapterId} recalculated → $progress%")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error triggering chapter progress update: ${e.message}")
        }
    }

    // ===== STUDY (non-math) =====

    /** Mark a concept study session as fully COMPLETED (END node reached) */
    suspend fun markStudyCompleted(studentId: String, conceptId: String, language: String? = null) {
        try {
            val resolvedLang = language ?: if (com.ncert7.aitutorandlab.utils.isKannada()) "kn" else "en"
            progressRepository.markStudyCompleted(studentId, conceptId, resolvedLang)
            triggerChapterProgressUpdate(studentId, conceptId, resolvedLang)
            streakRepository.recordActivity(studentId)
            DebugLogger.debugLog(TAG, "Study COMPLETED: $conceptId ($resolvedLang)")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Study mark error: ${e.message}")
        }
    }

    /** Mark a concept study session as IN_PROGRESS (session started) */
    suspend fun markStudyInProgress(studentId: String, conceptId: String, language: String? = null) {
        try {
            val resolvedLang = language ?: if (com.ncert7.aitutorandlab.utils.isKannada()) "kn" else "en"
            progressRepository.markStudyInProgress(studentId, conceptId, resolvedLang)
            triggerChapterProgressUpdate(studentId, conceptId, resolvedLang)
            streakRepository.recordActivity(studentId)
            DebugLogger.debugLog(TAG, "Study IN_PROGRESS: $conceptId ($resolvedLang)")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Study progress error: ${e.message}")
        }
    }

    // ===== SIMULATION =====

    /** Mark simulation agent session COMPLETED (contributes to simulation component) */
    suspend fun markSimulationAgentCompleted(studentId: String, conceptId: String, language: String? = null) {
        try {
            val resolvedLang = language ?: if (com.ncert7.aitutorandlab.utils.isKannada()) "kn" else "en"
            progressRepository.markSimulationAgentCompleted(studentId, conceptId, resolvedLang)
            triggerChapterProgressUpdate(studentId, conceptId, resolvedLang)
            streakRepository.recordActivity(studentId)
            DebugLogger.debugLog(TAG, "Simulation Agent COMPLETED: $conceptId ($resolvedLang)")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Simulation Agent error: ${e.message}")
        }
    }

    /** Mark simulation URL as loaded/COMPLETED (contributes to simulation component) */
    suspend fun markSimulationUrlCompleted(studentId: String, conceptId: String, language: String? = null) {
        try {
            val resolvedLang = language ?: if (com.ncert7.aitutorandlab.utils.isKannada()) "kn" else "en"
            progressRepository.markSimulationUrlCompleted(studentId, conceptId, resolvedLang)
            triggerChapterProgressUpdate(studentId, conceptId, resolvedLang)
            streakRepository.recordActivity(studentId)
            DebugLogger.debugLog(TAG, "Simulation URL COMPLETED: $conceptId ($resolvedLang)")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Simulation URL error: ${e.message}")
        }
    }

    // ===== REVISION =====

    /** Mark revision agent session COMPLETED for a specific concept */
    suspend fun markRevisionCompleted(studentId: String, conceptId: String, language: String? = null) {
        try {
            val resolvedLang = language ?: if (com.ncert7.aitutorandlab.utils.isKannada()) "kn" else "en"
            progressRepository.markRevisionCompleted(studentId, conceptId, resolvedLang)
            triggerChapterProgressUpdate(studentId, conceptId, resolvedLang)
            streakRepository.recordActivity(studentId)
            DebugLogger.debugLog(TAG, "Revision COMPLETED: $conceptId ($resolvedLang)")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Revision error: ${e.message}")
        }
    }

    // ===== MATH AGENT =====

    /**
     * Mark math agent COMPLETED.
     *
     * Writes TWO rows:
     *   - MATH_AGENT / conceptId = COMPLETED  (used by ChapterProgressCalculator for math study %)
     *   - CONCEPT    / conceptId = COMPLETED  (used by ConceptViewModel for lock/unlock logic)
     *
     * ChapterProgressCalculator checks MATH_AGENT first, falls back to CONCEPT, so both being
     * written is correct and keeps ConceptScreen unlock logic working.
     */
    suspend fun markMathAgentCompleted(studentId: String, conceptId: String, language: String? = null) {
        try {
            val resolvedLang = language ?: if (com.ncert7.aitutorandlab.utils.isKannada()) "kn" else "en"
            // Primary: write MATH_AGENT row (drives chapter % on math subjects)
            progressRepository.markMathAgentCompleted(studentId, conceptId, resolvedLang)
            // Secondary: write CONCEPT row (drives ConceptScreen unlock sequence)
            progressRepository.markStudyCompleted(studentId, conceptId, resolvedLang)
            // Recalculate chapter progress once after both writes
            triggerChapterProgressUpdate(studentId, conceptId, resolvedLang)
            streakRepository.recordActivity(studentId)
            DebugLogger.debugLog(TAG, "Math Agent COMPLETED (MATH_AGENT + CONCEPT written): $conceptId ($resolvedLang)")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Math Agent error: ${e.message}")
        }
    }

    // ===== SCIENCE AGENT =====

    /** Update science agent progress (partial or complete) */
    suspend fun updateScienceAgentProgress(
        studentId: String,
        conceptId: String,
        progressPercentage: Int,
        language: String? = null
    ) {
        try {
            val resolvedLang = language ?: if (com.ncert7.aitutorandlab.utils.isKannada()) "kn" else "en"
            val status = if (progressPercentage >= 100)
                ProgressStatus.COMPLETED.value else ProgressStatus.IN_PROGRESS.value

            progressRepository.updateProgressStatus(
                studentId          = studentId,
                itemType           = "SCIENCE_AGENT",
                itemId             = conceptId,
                language           = resolvedLang,
                newStatus          = status,
                progressPercentage = progressPercentage.coerceIn(0, 100)
            )
            triggerChapterProgressUpdate(studentId, conceptId, resolvedLang)
            if (progressPercentage > 0) streakRepository.recordActivity(studentId)
            DebugLogger.debugLog(TAG, "Science Agent updated: $conceptId ($progressPercentage%) ($resolvedLang)")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Science Agent error: ${e.message}")
        }
    }
}