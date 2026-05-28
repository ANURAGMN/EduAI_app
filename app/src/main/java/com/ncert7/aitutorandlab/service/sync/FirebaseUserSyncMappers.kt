package com.ncert7.aitutorandlab.service.sync

import com.ncert7.aitutorandlab.data.local.entities.AppAnalyticsEntity
import com.ncert7.aitutorandlab.data.local.entities.ChapterAgentProgressEntity
import com.ncert7.aitutorandlab.data.local.entities.ProgressEntity
import com.ncert7.aitutorandlab.data.local.entities.SessionEntity
import com.ncert7.aitutorandlab.data.local.entities.StreakEntity
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Mappers to convert Firestore documents to local Room entities and vice versa
 */

object FirebaseProgressMapper {
    fun map(document: DocumentSnapshot, studentId: String): ProgressEntity {
        return ProgressEntity(
            progressId = document.getLong("progressId") ?: 0L,
            studentId = studentId,
            itemType = document.getString("itemType") ?: "",
            itemId = document.getString("itemId") ?: "",
            status = document.getString("status") ?: "NOT_STARTED",
            progressPercentage = document.getLong("progressPercentage")?.toInt() ?: 0,
            language = document.getString("language") ?: "en",
            appName = document.getString("appName") ?: "",
            startedAt = document.getLong("startedAt"),
            completedAt = document.getLong("completedAt"),
            lastAccessedAt = document.getLong("lastAccessedAt") ?: 0L,
            updatedAt = document.getLong("updatedAt") ?: 0L,
            isSynced = true
        )
    }
}

object FirebaseStreakMapper {
    fun map(document: DocumentSnapshot, studentId: String): StreakEntity {
        return StreakEntity(
            userId = studentId,
            streakCount = document.getLong("streakCount")?.toInt() ?: 0,
            lastStreakDate = document.getLong("lastStreakDate") ?: 0L,
            createdAt = document.getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = document.getLong("updatedAt") ?: System.currentTimeMillis(),
            appName = document.getString("appName") ?: "",
            isSynced = true
        )
    }
}

object FirebaseChapterProgressMapper {
    fun map(document: DocumentSnapshot, studentId: String): ChapterAgentProgressEntity {
        return ChapterAgentProgressEntity(
            progressId = document.getLong("progressId") ?: 0L,
            studentId = studentId,
            chapterId = document.getString("chapterId") ?: "",
            language = document.getString("language") ?: "en",
            appName = document.getString("appName") ?: "",
            studyPercentage = document.getLong("studyPercentage")?.toInt() ?: 0,
            simulationPercentage = document.getLong("simulationPercentage")?.toInt() ?: 0,
            revisionPercentage = document.getLong("revisionPercentage")?.toInt() ?: 0,
            overallPercentage = document.getLong("overallPercentage")?.toInt() ?: 0,
            status = document.getString("status") ?: "NOT_STARTED",
            createdAt = document.getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = document.getLong("updatedAt") ?: System.currentTimeMillis(),
            completedAt = document.getLong("completedAt"),
            isSynced = true
        )
    }
}
