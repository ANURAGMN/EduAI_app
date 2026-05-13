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
 * Progress Event Tracker — Central entry point for all progress state updates
 *
 * This is the ONLY class used for recording progress events throughout the app.
 * Uses ProgressRepository and ConceptRepository — no direct DAO access.
 *
 * Every call:
 *   1. Writes the progress row via ProgressRepository
 *   2. Triggers chapter-level progress recalculation via ChapterProgressService
 *   3. Records a streak activity
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

    // Removed StreakManager - now uses StreakRepository for synced streaks
    // private val streakManager: StreakManager by lazy { StreakManager(context) }

    /**
     * Look up which chapter a concept belongs to, then trigger chapter progress recalculation.
     * This ensures chapter_agent_progress is always up-to-date after any concept change.
     */
    private suspend fun triggerChapterProgressUpdate(
        studentId: String,
        conceptId: String,
        language: String = if (com.ncert7.aitutorandlab.utils.isKannada()) "kn" else "en"
    ) {
        try {
            val concept = conceptRepository.getConcept(conceptId)
            if (concept == null) {
                DebugLogger.errorLog(TAG, "Concept not found for id=$conceptId — chapter progress not updated")
                return
            }
            val progress = chapterProgressService.updateChapterProgress(studentId, concept.chapterId, language)
            DebugLogger.debugLog(TAG, "Chapter ${concept.chapterId} progress updated: $progress%")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error triggering chapter progress update: ${e.message}")
        }
    }

    // ===== STUDY =====

    /** Mark a concept study as fully COMPLETED (e.g. END node reached) */
    suspend fun markStudyCompleted(studentId: String, conceptId: String, language: String = "en") {
        try {
            progressRepository.markStudyCompleted(studentId, conceptId)
            triggerChapterProgressUpdate(studentId, conceptId, language)
            streakRepository.recordActivity(studentId)
            DebugLogger.debugLog(TAG, "Study COMPLETED: $conceptId")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Study mark error: ${e.message}")
        }
    }

    /** Mark a concept study as IN_PROGRESS (session started) */
    suspend fun markStudyInProgress(studentId: String, conceptId: String, language: String = "en") {
        try {
            progressRepository.markStudyInProgress(studentId, conceptId)
            triggerChapterProgressUpdate(studentId, conceptId, language)
            streakRepository.recordActivity(studentId)
            DebugLogger.debugLog(TAG, "Study IN_PROGRESS: $conceptId")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Study progress error: ${e.message}")
        }
    }

    // ===== SIMULATION =====

    /** Mark simulation agent session COMPLETED (50% contribution to simulation score) */
    suspend fun markSimulationAgentCompleted(studentId: String, conceptId: String) {
        try {
            progressRepository.markSimulationAgentCompleted(studentId, conceptId)
            triggerChapterProgressUpdate(studentId, conceptId)
            streakRepository.recordActivity(studentId)
            DebugLogger.debugLog(TAG, "Simulation Agent COMPLETED: $conceptId")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Simulation Agent error: ${e.message}")
        }
    }

    /** Mark simulation URL as loaded/COMPLETED (50% contribution to simulation score) */
    suspend fun markSimulationUrlCompleted(studentId: String, conceptId: String) {
        try {
            progressRepository.markSimulationUrlCompleted(studentId, conceptId)
            triggerChapterProgressUpdate(studentId, conceptId)
            streakRepository.recordActivity(studentId)
            DebugLogger.debugLog(TAG, "Simulation URL COMPLETED: $conceptId")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Simulation URL error: ${e.message}")
        }
    }

    // ===== REVISION =====

    /** Mark revision agent session COMPLETED for a specific concept */
    suspend fun markRevisionCompleted(studentId: String, conceptId: String) {
        try {
            progressRepository.markRevisionCompleted(studentId, conceptId)
            triggerChapterProgressUpdate(studentId, conceptId)
            streakRepository.recordActivity(studentId)
            DebugLogger.debugLog(TAG, "Revision COMPLETED: $conceptId")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Revision error: ${e.message}")
        }
    }

    // ===== MATH AGENT =====

    /**
     * Mark math agent COMPLETED.
     * Also marks the concept as STUDY COMPLETED (math agent = study + agent in one step).
     */
    suspend fun markMathAgentCompleted(studentId: String, conceptId: String) {
        try {
            progressRepository.markMathAgentCompleted(studentId, conceptId)
            // Math agent also counts as study completion
            markStudyCompleted(studentId, conceptId)
            DebugLogger.debugLog(TAG, "Math Agent COMPLETED (study also marked): $conceptId")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Math Agent error: ${e.message}")
        }
    }

    // ===== SCIENCE AGENT =====

    /** Update science agent progress (partial or complete) */
    suspend fun updateScienceAgentProgress(
        studentId: String,
        conceptId: String,
        progressPercentage: Int
    ) {
        try {
            val status = if (progressPercentage >= 100)
                ProgressStatus.COMPLETED.value else ProgressStatus.IN_PROGRESS.value

            progressRepository.updateProgressStatus(
                studentId          = studentId,
                itemType           = "SCIENCE_AGENT",
                itemId             = conceptId,
                newStatus          = status,
                progressPercentage = progressPercentage.coerceIn(0, 100)
            )
            triggerChapterProgressUpdate(studentId, conceptId)
            if (progressPercentage > 0) streakRepository.recordActivity(studentId)
            DebugLogger.debugLog(TAG, "Science Agent updated: $conceptId ($progressPercentage%)")
        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Science Agent error: ${e.message}")
        }
    }
}
