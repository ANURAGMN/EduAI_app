package com.ncert7.aitutorandlab.service.sync

import com.ncert7.aitutorandlab.data.local.entities.SubjectEntity
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Maps Firestore subject documents to local Room SubjectEntity objects.
 * Ensures strong separation between API layer and local database layer.
 */
object FirebaseSubjectMapper {

    fun map(document: DocumentSnapshot): SubjectEntity {
        val documentId = document.id
        val subjectId = document.getString("subject_id") ?: documentId
        val classLevel = document.getString("class_id")?.toIntOrNull() ?: 7  // Default to 7 instead of 0
        val totalChapters = document.getLong("subjectCount")?.toInt() ?: 0
        val subjectName = document.getString("subject_name")?:""
        val kannadaName = document.getString("subject_name_kn") ?:""
        return SubjectEntity(
            subjectId = subjectId,
            subjectName = subjectName,
            subjectNameKannada = kannadaName,
            classLevel = classLevel,
            iconUrl = document.getString("iconUrl"),
            orderIndex = (document.getLong("orderIndex") ?: 0L).toInt(),
            totalChapters = totalChapters,
            syncAt = System.currentTimeMillis(),
            isSynced = true
        )
    }
}
