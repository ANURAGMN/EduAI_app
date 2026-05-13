package com.ncert7.aitutorandlab.service.sync

import android.content.Context
import com.ncert7.aitutorandlab.config.AppConfig
import com.ncert7.aitutorandlab.data.local.dao.AppAnalyticsDao
import com.ncert7.aitutorandlab.data.local.dao.ChapterAgentProgressDao
import com.ncert7.aitutorandlab.data.local.dao.ChapterDao
import com.ncert7.aitutorandlab.data.local.dao.ConceptDao
import com.ncert7.aitutorandlab.data.local.dao.ProgressDao
import com.ncert7.aitutorandlab.data.local.dao.SessionDao
import com.ncert7.aitutorandlab.data.local.dao.StreakDao
import com.ncert7.aitutorandlab.data.local.dao.SubjectDao
import com.ncert7.aitutorandlab.data.local.entities.ChapterEntity
import com.ncert7.aitutorandlab.data.local.entities.ConceptEntity
import com.ncert7.aitutorandlab.data.local.entities.SubjectEntity
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Handles synchronization of educational content and user progress from Firebase Firestore to local Room database.
 * Uses mapper objects to convert Firestore documents to local entities.
 * Ensures data isolation across multiple apps using the same Firebase project.
 */
class FirebaseSyncManager(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val subjectDao: SubjectDao,
    private val chapterDao: ChapterDao,
    private val conceptDao: ConceptDao,
    private val progressDao: ProgressDao? = null,
    private val analyticsDao: AppAnalyticsDao? = null,
    private val sessionDao: SessionDao? = null,
    private val streakDao: StreakDao? = null,
    private val chapterProgressDao: ChapterAgentProgressDao? = null,
    private val context: Context? = null
) {
    companion object {
        private const val CONCEPTS_COLLECTION = "Concept"
        private const val PROGRESS_COLLECTION = "progress"
        private const val ANALYTICS_COLLECTION = "analytics"
        private const val SESSIONS_COLLECTION = "sessions"
        private const val STREAK_COLLECTION = "streak"
        private const val CHAPTER_PROGRESS_COLLECTION = "chapterprogress"
        private const val TAG = "FirebaseSyncManager"
    }

    /**
     * Syncs all concepts from Firestore and extracts unique subjects and chapters.
     * Also detects and notifies about new simulation concepts.
     */
    suspend fun syncAllContent(): SyncResult {
        return try {
            DebugLogger.debugLog(TAG, "Starting content sync from Firestore...")

            val snapshot = firestore.collection(CONCEPTS_COLLECTION).get().await()

            if (snapshot.isEmpty) {
                DebugLogger.debugLog(TAG, "No concepts found in Firestore")
                return SyncResult(success = true, message = "No data to sync")
            }

            val subjects = mutableMapOf<String, SubjectEntity>()
            val chapters = mutableMapOf<String, ChapterEntity>()
            val concepts = mutableListOf<ConceptEntity>()

            for (document in snapshot.documents) {
                try {
                    val subjectId = document.getString("subject_id")
                    if (subjectId != null && !subjects.containsKey(subjectId)) {
                        subjects[subjectId] = FirebaseSubjectMapper.map(document)
                    }

                    val chapterId = document.getString("chapter_id")
                    if (chapterId != null && !chapters.containsKey(chapterId)) {
                        chapters[chapterId] = FirebaseChapterMapper.map(document)
                    }

                    concepts.add(FirebaseConceptMapper.map(document))
                } catch (e: Exception) {
                    DebugLogger.errorLog(TAG, "Error mapping document ${document.id}: ${e.message}")
                }
            }

            // Insert content
            subjectDao.insertSubjects(subjects.values.toList())
            chapterDao.insertChapters(chapters.values.toList())

            // Detect and Notify about new simulations
            val newSimulations = if (context != null) {
                NewSimulationNotifier.getNewSimulations(concepts, conceptDao)
            } else emptyList()

            conceptDao.insertConcepts(concepts)

            if (context != null && newSimulations.isNotEmpty()) {
                NewSimulationNotifier.showNotification(context, newSimulations)
            }

            val message = "Synced ${subjects.size} subjects, ${chapters.size} chapters, ${concepts.size} concepts"
            DebugLogger.debugLog(TAG, message)
            SyncResult(success = true, message = message)
        } catch (e: Exception) {
            val errorMsg = "Content sync failed: ${e.message}"
            DebugLogger.errorLog(TAG, errorMsg)
            SyncResult(success = false, message = errorMsg)
        }
    }

    /**
     * Syncs user's progress history from Firestore to local database.
     * Uses APP_NAME for data isolation.
     */
    suspend fun syncUserProgress(userId: String): SyncResult {
        return try {
            if (progressDao == null) return SyncResult(true, "ProgressDao not available")

            val studentAppDocId = "${AppConfig.APP_NAME}_$userId"
            DebugLogger.debugLog(TAG, "Syncing user progress for: $studentAppDocId")

            val snapshot = firestore.collection(PROGRESS_COLLECTION)
                .document(studentAppDocId)
                .collection("records")
                .get()
                .await()

            val progressList = snapshot.documents.mapNotNull { 
                try { FirebaseProgressMapper.map(it, userId) } catch (e: Exception) { null }
            }

            if (progressList.isNotEmpty()) {
                progressDao.insertProgressList(progressList)
            }

            SyncResult(true, "Synced ${progressList.size} progress entries")
        } catch (e: Exception) {
            SyncResult(false, "Progress sync failed: ${e.message}")
        }
    }

    /**
     * Syncs user's streak data from Firestore.
     */
    suspend fun syncUserStreak(userId: String): SyncResult {
        return try {
            if (streakDao == null) return SyncResult(true, "StreakDao not available")

            val studentAppDocId = "${AppConfig.APP_NAME}_$userId"
            val snapshot = firestore.collection(STREAK_COLLECTION)
                .document(studentAppDocId)
                .collection("data")
                .document("current")
                .get()
                .await()

            if (snapshot.exists()) {
                val streak = FirebaseStreakMapper.map(snapshot, userId)
                streakDao.insertStreak(streak)
                SyncResult(true, "Synced streak data")
            } else {
                SyncResult(true, "No streak data found")
            }
        } catch (e: Exception) {
            SyncResult(false, "Streak sync failed: ${e.message}")
        }
    }

    /**
     * Syncs chapter agent progress history.
     */
    suspend fun syncChapterAgentProgress(userId: String): SyncResult {
        return try {
            if (chapterProgressDao == null) return SyncResult(true, "ChapterProgressDao not available")

            val studentAppDocId = "${AppConfig.APP_NAME}_$userId"
            val snapshot = firestore.collection(CHAPTER_PROGRESS_COLLECTION)
                .document(studentAppDocId)
                .collection("records")
                .get()
                .await()

            DebugLogger.debugLog(TAG, "Retrieved ${snapshot.size()} chapter progress records from Firestore for: $studentAppDocId")

            val list = snapshot.documents.mapNotNull { 
                try { FirebaseChapterProgressMapper.map(it, userId) } catch (e: Exception) { null }
            }

            if (list.isNotEmpty()) {
                chapterProgressDao.insertAll(list)
                DebugLogger.debugLog(TAG, "Restored ${list.size} chapter progress records to local database")
            }

            SyncResult(true, "Synced ${list.size} chapter progress entries")
        } catch (e: Exception) {
            SyncResult(false, "Chapter progress sync failed: ${e.message}")
        }
    }
}

/** Result of a sync operation */
data class SyncResult(val success: Boolean, val message: String)
