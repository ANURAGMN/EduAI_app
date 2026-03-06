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
 *  - type: either "SIMULATION" or "STUDY"
 *  - simulation_url: if type == simulation then webpage url else empty or null
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
        val typeRaw = document.getString("type")
            ?: error("concept_type missing at concept ${document.id}")
        val conceptType = ConceptType.from(typeRaw)


        return ConceptEntity(
            conceptId = document.id,
            chapterId = document.getString("chapter_id") ?: error("ChapterId missing for concept ${document.id}"),
            conceptName = document.getString("concept_name") ?: error("concept_name missing for concept ${document.id}"),
            conceptNameKannada = document.getString("concept_name_kn") ?: error("Kannada Concept name missing for concept ${document.id}"),
            orderIndex = document.getLong("conceptOrder")?.toInt() ?: 0,
            description = combinedDescription,
            hasSimulation = conceptType is ConceptType.Simulation,
            type = conceptType.raw,
            simulationUrl = document.getString("simulation_url") ?: "",
            simulationUrlKannada = document.getString("simulation_url_kannada") ?: "",
            syncAt = System.currentTimeMillis(),
            isSynced = true
        )
    }
}

sealed class ConceptType(val raw: String) {
    object Simulation : ConceptType("SIMULATION")
    object Study : ConceptType("STUDY")

    companion object {
        fun from(raw: String?): ConceptType =
            when (raw) {
                "SIMULATION" -> Simulation
                "STUDY" -> Study
                else -> error("Unknown concept type: $raw")
            }
    }
}
