package com.anurag.eduai.sync

import com.anurag.eduai.data.local.entities.ChapterEntity
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Maps Firestore chapter documents to local Room ChapterEntity objects.
 * Ensures strong separation between API layer and local database layer.
 */
object FirebaseChapterMapper {

    fun map(document: DocumentSnapshot): ChapterEntity {
        val chapterId = document.get("chapter_id").toString()
        val subjectId = document.get("subject_id").toString()
        val chapterName = document.get("unit_name").toString()
        val orderIndex = document.getLong("conceptOrder")?.toInt() ?: 0
        val totalConcepts = document.getLong("conceptCount")?.toInt() ?: 0

        return ChapterEntity(
            chapterId = chapterId,
            subjectId = subjectId,
            chapterName = chapterName,
            chapterNameKannada = "",
            orderIndex = orderIndex,
            totalConcepts = totalConcepts,
            syncAt = System.currentTimeMillis(),
            isSynced = true
        )
    }
}