package com.anurag.eduai.sync

import com.anurag.eduai.data.local.dao.ChapterDao
import com.anurag.eduai.data.local.dao.ConceptDao
import com.anurag.eduai.data.local.dao.SubjectDao
import com.anurag.eduai.debug.DebugLogger
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Handles synchronization of educational content from Firebase Firestore to local Room database.
 * Uses mapper objects to convert Firestore documents to local entities.
 */
class FirebaseSyncManager(
        private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
        private val subjectDao: SubjectDao,
        private val chapterDao: ChapterDao,
        private val conceptDao: ConceptDao
) {
    companion object {
        private const val CONCEPTS_COLLECTION = "Concept"
        private const val TAG = "FirebaseSyncManager"
    }

    /**
     * Syncs all concepts from Firestore and extracts unique subjects and chapters to populate the
     * local database.
     */
    suspend fun syncAllContent(): SyncResult {
        return try {
            DebugLogger.debugLog(TAG, "Starting content sync from Firestore...")

            // Fetch all concept documents from Firestore
            val snapshot = firestore.collection(CONCEPTS_COLLECTION).get().await()

            if (snapshot.isEmpty) {
                DebugLogger.debugLog(TAG, "No concepts found in Firestore")
                return SyncResult(success = true, message = "No data to sync")
            }

            val subjects =
                    mutableMapOf<String, com.anurag.eduai.data.local.entities.SubjectEntity>()
            val chapters =
                    mutableMapOf<String, com.anurag.eduai.data.local.entities.ChapterEntity>()
            val concepts = mutableListOf<com.anurag.eduai.data.local.entities.ConceptEntity>()

            // Process each document
            for (document in snapshot.documents) {
                try {
                    // Extract and store unique subjects
                    val subjectId = document.getString("subject_id")
                    if (subjectId != null && !subjects.containsKey(subjectId)) {
                        val subject = FirebaseSubjectMapper.map(document)
                        subjects[subjectId] = subject
                    }

                    // Extract and store unique chapters
                    val chapterId = document.get("chapter_id").toString()
                    if (!chapters.containsKey(chapterId)) {
                        val chapter = FirebaseChapterMapper.map(document)
                        chapters[chapterId] = chapter
                    }

                    // Map concept
                    val concept = FirebaseConceptMapper.map(document)
                    concepts.add(concept)
                } catch (e: Exception) {
                    DebugLogger.errorLog(TAG, "Error mapping document ${document.id}: ${e.message}")
                }
            }

            // Insert into local database in correct order (subjects -> chapters -> concepts)
            DebugLogger.debugLog(TAG, "Inserting ${subjects.size} subjects...")
            subjectDao.insertSubjects(subjects.values.toList())

            DebugLogger.debugLog(TAG, "Inserting ${chapters.size} chapters...")
            chapterDao.insertChapters(chapters.values.toList())

            DebugLogger.debugLog(TAG, "Inserting ${concepts.size} concepts...")
            conceptDao.insertConcepts(concepts)

            val message =
                    "Synced ${subjects.size} subjects, ${chapters.size} chapters, ${concepts.size} concepts"
            DebugLogger.debugLog(TAG, message)

            SyncResult(success = true, message = message)
        } catch (e: Exception) {
            val errorMsg = "Sync failed: ${e.message}"
            DebugLogger.errorLog(TAG, errorMsg)
            SyncResult(success = false, message = errorMsg)
        }
    }

    /** Syncs concepts for a specific subject */
    suspend fun syncSubjectContent(subjectId: String): SyncResult {
        return try {
            DebugLogger.debugLog(TAG, "Syncing content for subject: $subjectId")

            val snapshot =
                    firestore
                            .collection(CONCEPTS_COLLECTION)
                            .whereEqualTo("subject_id", subjectId)
                            .get()
                            .await()

            if (snapshot.isEmpty) {
                return SyncResult(success = true, message = "No concepts found for subject")
            }

            val chapters =
                    mutableMapOf<String, com.anurag.eduai.data.local.entities.ChapterEntity>()
            val concepts = mutableListOf<com.anurag.eduai.data.local.entities.ConceptEntity>()

            for (document in snapshot.documents) {
                try {
                    // Extract unique chapters
                    val chapterId = document.get("chapter_id").toString()
                    if (!chapters.containsKey(chapterId)) {
                        val chapter = FirebaseChapterMapper.map(document)
                        chapters[chapterId] = chapter
                    }

                    // Map concept
                    val concept = FirebaseConceptMapper.map(document)
                    concepts.add(concept)
                } catch (e: Exception) {
                    DebugLogger.errorLog(TAG, "Error mapping document: ${e.message}")
                }
            }

            chapterDao.insertChapters(chapters.values.toList())
            conceptDao.insertConcepts(concepts)

            val message =
                    "Synced ${chapters.size} chapters, ${concepts.size} concepts for subject $subjectId"
            DebugLogger.debugLog(TAG, message)

            SyncResult(success = true, message = message)
        } catch (e: Exception) {
            val errorMsg = "Subject sync failed: ${e.message}"
            DebugLogger.errorLog(TAG, errorMsg)
            SyncResult(success = false, message = errorMsg)
        }
    }

    /** Syncs concepts for a specific chapter */
    suspend fun syncChapterContent(chapterId: String): SyncResult {
        return try {
            DebugLogger.debugLog(TAG, "Syncing content for chapter: $chapterId")

            val snapshot =
                    firestore
                            .collection(CONCEPTS_COLLECTION)
                            .whereEqualTo("chapter_id", chapterId)
                            .get()
                            .await()

            if (snapshot.isEmpty) {
                return SyncResult(success = true, message = "No concepts found for chapter")
            }

            val concepts =
                    snapshot.documents.mapNotNull { document ->
                        try {
                            FirebaseConceptMapper.map(document)
                        } catch (e: Exception) {
                            DebugLogger.errorLog(TAG, "Error mapping concept: ${e.message}")
                            null
                        }
                    }

            conceptDao.insertConcepts(concepts)

            val message = "Synced ${concepts.size} concepts for chapter $chapterId"
            DebugLogger.debugLog(TAG, message)

            SyncResult(success = true, message = message)
        } catch (e: Exception) {
            val errorMsg = "Chapter sync failed: ${e.message}"
            DebugLogger.errorLog(TAG, errorMsg)
            SyncResult(success = false, message = errorMsg)
        }
    }
}

/** Result of a sync operation */
data class SyncResult(val success: Boolean, val message: String)
