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
//        val conceptName = document.getString("concept_name")
//            ?: error("concept_name missing for concept ${document.id}")

        return ConceptEntity(
            conceptId = document.id,
            chapterId = document.get("chapter_id")?.toString() ?: error("ChapterId missing for concept ${document.id}"),
            conceptName = document.getString("concept_name") ?: error("concept_name missing for concept ${document.id}"),
            conceptNameKannada = "",
            orderIndex = document.getLong("conceptOrder")?.toInt() ?: 0,
            description = combinedDescription,
            hasSimulation = false,
            createdAt = System.currentTimeMillis(),
            isSynced = true
        )
    }
}
