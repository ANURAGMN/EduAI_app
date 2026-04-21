package com.anurag.eduai.service.sync

import com.anurag.eduai.data.local.entities.SubjectEntity
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Locale

/**
 * Maps Firestore subject documents to local Room SubjectEntity objects.
 * Ensures strong separation between API layer and local database layer.
 */
object FirebaseSubjectMapper {

    fun map(document: DocumentSnapshot): SubjectEntity {

        val classLevel = document.getString("class_id")?.toIntOrNull() ?: 0
        val totalChapters = document.getLong("subjectCount")?.toInt() ?: 0

        return SubjectEntity(
            subjectId = document.getString("subject_id") ?: error("Unable to extract subject from document id ${document.id}"),
            subjectName = document.getString("subject_id").toString()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                ?: error("Unable to extract subject from document id ${document.id}"),
            subjectNameKannada = document.getString("subject_name_kn")?:error("Unable to extract subject from document id ${document.id}"),
            classLevel = classLevel,
            iconUrl = null,
            orderIndex = 0,
            totalChapters = totalChapters,
            syncAt = System.currentTimeMillis(),
            isSynced = true
        )
    }
}
