package com.ncert7.aitutorandlab.service.sync

import com.ncert7.aitutorandlab.config.AppConfig
import com.ncert7.aitutorandlab.data.local.entities.ProgressEntity
import com.ncert7.aitutorandlab.utils.normalizeLanguageCode

/**
 * Shared Firestore path and document ID helpers for progress sync.
 * Keeps batch upload, real-time upload, and restore aligned.
 */
object FirestoreSyncUtils {
    /** Only restore progress touched within this window (avoids pulling full history on login). */
    const val PROGRESS_RESTORE_LOOKBACK_MS = 180L * 24 * 60 * 60 * 1000

    fun studentAppDocId(studentId: String): String = "${AppConfig.APP_NAME}_$studentId"

    /** Firestore doc id under progress/{studentAppDocId}/records — includes language so en/kn do not overwrite. */
    fun progressRecordDocId(itemType: String, itemId: String, language: String): String {
        val lang = normalizeLanguageCode(language)
        return "${itemType}_${itemId}_$lang"
    }

    fun progressRecordPayload(progress: ProgressEntity, syncedAt: Long = System.currentTimeMillis()): Map<String, Any?> {
        val lang = normalizeLanguageCode(progress.language)
        return mapOf(
            "progressId" to progress.progressId,
            "studentId" to progress.studentId,
            "itemType" to progress.itemType,
            "itemId" to progress.itemId,
            "status" to progress.status,
            "progressPercentage" to progress.progressPercentage,
            "language" to lang,
            "startedAt" to progress.startedAt,
            "completedAt" to progress.completedAt,
            "lastAccessedAt" to progress.lastAccessedAt,
            "updatedAt" to progress.updatedAt,
            "appName" to AppConfig.APP_NAME,
            "syncedAt" to syncedAt
        )
    }

    fun shouldRestoreProgressRecord(
        lastAccessedAt: Long,
        completedAt: Long?,
        updatedAt: Long,
        now: Long = System.currentTimeMillis(),
        lookbackMs: Long = PROGRESS_RESTORE_LOOKBACK_MS
    ): Boolean {
        val cutoff = now - lookbackMs
        return lastAccessedAt >= cutoff ||
            (completedAt != null && completedAt >= cutoff) ||
            updatedAt >= cutoff
    }
}
