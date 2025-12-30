package com.anurag.eduai.sync

import com.anurag.eduai.data.local.entities.ConceptEntity
import com.google.firebase.firestore.DocumentSnapshot

/**
 *  Maps Firestore concept documents to local Room ConceptEntity objects.
 *  Ensures strong separation between API layer and local database layer.
 *  
 *  Firebase document structure:
 *  - concept_id: Unique identifier
 *  - chapter_id: Reference to chapter
 *  - concept_name: Name of the concept
 *  - summary: Brief description
 *  - detail: Detailed explanation
 *  - example: Example text
 *  - topic_name: Topic name
 *  - unit_name: Unit/Chapter name
 *  - subject_id: Subject identifier
 *  - class_id: Class level
 */
object FirebaseConceptMapper {
    /**
     * Converts Firebase document into ConceptEntity
     */
    fun map(document: DocumentSnapshot): ConceptEntity {
        // Combine summary and detail for a complete description
        val summary = document.getString("summary") ?: ""
        val detail = document.getString("detail") ?: ""
        val description = if (detail.isNotEmpty()) {
            "$summary\n\n$detail"
        } else {
            summary
        }
        
        return ConceptEntity(
            conceptId = document.getString("concept_id") ?: "",
            chapterId = document.get("chapter_id").toString(),
            conceptName = document.getString("concept_name") ?: "",
            conceptNameKannada = "", // Not provided by Firestore
            orderIndex = 0,          // You may map this if Firestore contains it
            description = description,
            hasSimulation = false,
            createdAt = System.currentTimeMillis(),
            isSynced = true
        )
        // Note: Firebase also has 'example' and 'topic_name' fields that could be stored
        // in additional tables if needed in the future
    }
}