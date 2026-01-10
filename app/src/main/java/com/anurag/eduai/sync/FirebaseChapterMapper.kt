package com.anurag.eduai.sync

import com.anurag.eduai.data.local.entities.ChapterEntity
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Maps Firestore chapter documents to local Room ChapterEntity objects.
 * Ensures strong separation between API layer and local database layer.
 */
object FirebaseChapterMapper {

    fun map(document: DocumentSnapshot): ChapterEntity {
        val chapterId = document.get("chapter_id")?.toString() ?: error("chapterId missing for concept ${document.id}")
        val subjectId = document.get("subject_id")?.toString() ?: error("subjectId missing for concept ${document.id}")
        val chapterName = document.get("unit_name")?.toString() ?: error("chapterName missing for concept ${document.id}")
        val orderIndex = document.getLong("chapter_id")?.toInt() ?: error("orderIndex missing for concept ${document.id}")
        val totalConcepts = document.getLong("conceptCount")?.toInt() ?: 0

//        val chapterId = document.getString("chapter_id")
//            ?: error("chapter_id missing in Firestore document ${document.id}")
        return ChapterEntity(
            chapterId = chapterId,
            subjectId = subjectId,
            chapterName = chapterName,
            chapterNameKannada = "",
            orderIndex = orderIndex,
            totalConcepts = totalConcepts,
            createdAt = System.currentTimeMillis(),
            isSynced = true
        )
    }
}