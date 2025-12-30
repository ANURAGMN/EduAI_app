package com.anurag.eduai.sync

import com.anurag.eduai.data.local.entities.ChapterEntity
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Maps Firestore chapter documents to local Room ChapterEntity objects.
 * Ensures strong separation between API layer and local database layer.
 */
object FirebaseChapterMapper {
    /**
     * Converts Firebase document into ChapterEntity
     */
    fun map(document: DocumentSnapshot): ChapterEntity {
        return ChapterEntity(
            chapterId = document.get("chapter_id").toString(),
            subjectId = document.getString("subject_id") ?: "",
            chapterName = document.getString("unit_name") ?: "",
            chapterNameKannada = "", // Not provided by Firestore
            orderIndex = 0,          // Not provided by Firestore
            totalConcepts = 0,       // Can be calculated separately
            createdAt = System.currentTimeMillis(),
            isSynced = true
        )
    }
}
