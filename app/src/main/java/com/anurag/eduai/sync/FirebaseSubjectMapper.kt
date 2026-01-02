package com.anurag.eduai.sync

import com.anurag.eduai.data.local.entities.SubjectEntity
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Maps Firestore subject documents to local Room SubjectEntity objects.
 * Ensures strong separation between API layer and local database layer.
 */
object FirebaseSubjectMapper {

    fun map(document: DocumentSnapshot): SubjectEntity {

        val classLevel = document.getString("class_id")?.toIntOrNull() ?: 0
        val totalChapters = document.getLong("subjectCount")?.toInt() ?: 0

        return SubjectEntity(
            subjectId = document.getString("subject_id") ?: "",
            subjectName = document.getString("subject_id") ?: "",
            subjectNameKannada = "",
            classLevel = classLevel,
            iconUrl = null,
            orderIndex = 0,
            totalChapters = totalChapters,
            createdAt = System.currentTimeMillis(),
            isSynced = true
        )
    }
}
