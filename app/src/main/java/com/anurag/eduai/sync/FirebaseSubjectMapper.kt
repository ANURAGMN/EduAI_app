package com.anurag.eduai.sync

import com.anurag.eduai.data.local.entities.SubjectEntity
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Maps Firestore subject documents to local Room SubjectEntity objects.
 * Ensures strong separation between API layer and local database layer.
 */
object FirebaseSubjectMapper {
    /**
     * Converts Firebase document into SubjectEntity
     */
    fun map(document: DocumentSnapshot): SubjectEntity {
        // Extract class level from class_id (e.g., "7" -> 7)
        val classLevel = try {
            document.getString("class_id")?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
        
        return SubjectEntity(
            subjectId = document.getString("subject_id") ?: "",
            subjectName = document.getString("subject_id") ?: "", // Firestore doesn't have subject_name, using ID
            subjectNameKannada = "", // Not provided by Firestore
            classLevel = classLevel,
            iconUrl = null,          // Not provided by Firestore
            orderIndex = 0,          // You may map this if Firestore contains it
            createdAt = System.currentTimeMillis(),
            isSynced = true
        )
    }
}
