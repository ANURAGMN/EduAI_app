package com.ncert7.aitutorandlab.service.sync

import com.ncert7.aitutorandlab.data.local.entities.ChapterEntity
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Maps Firestore chapter documents to local Room ChapterEntity objects.
 * Ensures strong separation between API layer and local database layer.
 */
object FirebaseChapterMapper {

    fun map(document: DocumentSnapshot): ChapterEntity {
        val chapterId = document.getString("chapter_id") ?: error("chapterId missing for concept ${document.id}")
        val subjectId = document.getString("subject_id") ?: error("subjectId missing for concept ${document.id}")
        val chapterName = document.getString("unit_name") ?: error("chapterName missing for concept ${document.id}")
        val kannadaChapterName = document.getString("unit_name_kn") ?: error("Kannada chapter Name missing for concept ${document.id}")
        val revisionId = document.getString("revision_id") ?: error("revisionId missing for concept ${document.id}")
        val problemId = document.get("problem_ids") as? List<String> ?: emptyList()
        val orderIndex = document.getLong("chapter_order")?.toInt()
            ?: 0

        val totalConcepts = document.getLong("conceptCount")?.toInt() ?: 0

        return ChapterEntity(
            chapterId = chapterId,
            subjectId = subjectId,
            chapterName = chapterName,
            chapterNameKannada = kannadaChapterName,
            revisionId =revisionId,
            problemId = problemId,
            orderIndex = orderIndex,
            totalConcepts = totalConcepts,
            syncAt = System.currentTimeMillis(),
            isSynced = true
        )
    }
}