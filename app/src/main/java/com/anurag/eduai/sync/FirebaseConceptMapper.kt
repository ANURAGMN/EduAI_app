package com.anurag.eduai.sync

import com.anurag.eduai.data.local.entities.ConceptEntity
import com.google.firebase.firestore.DocumentSnapshot

/**
 *  Maps Firestore concept documents to local Room ConceptEntity objects.
 *  Ensures strong separation between API layer and local database layer.
 */
object FirebaseConceptMapper {
    /**
     * To convert firebase document into ConceptEntity
     */
    fun map(document: DocumentSnapshot): ConceptEntity {
        return ConceptEntity(
            conceptId = document.getString("concept_id") ?: "",
            chapterId = document.get("chapter_id").toString(),
            conceptName = document.getString("concept_name") ?: "",
            conceptNameKannada = "", // Not provided by Firestore
            orderIndex = 0,          // You may map this if Firestore contains it
            description = document.getString("summary") ?: "",
            hasSimulation = false,
            createdAt = System.currentTimeMillis(),
            isSynced = true
        )
    }
}