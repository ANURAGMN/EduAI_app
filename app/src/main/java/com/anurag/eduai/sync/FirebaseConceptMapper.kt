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

    fun map(document: DocumentSnapshot): ConceptEntity {

        val summary = document.getString("summary") ?: ""
        val detail = document.getString("detail") ?: ""
        val combinedDescription = buildString {
            append(summary)
            if (detail.isNotEmpty()) {
                append("\n\n")
                append(detail)
            }
        }

        return ConceptEntity(
            conceptId = document.getString("concept_id") ?: "",
            chapterId = document.get("chapter_id").toString(),
            conceptName = document.getString("concept_name") ?: "",
            conceptNameKannada = "",
            orderIndex = document.getLong("conceptOrder")?.toInt() ?: 0,
            description = combinedDescription,
            hasSimulation = false,
            syncAt = System.currentTimeMillis(),
            isSynced = true
        )
    }
}
