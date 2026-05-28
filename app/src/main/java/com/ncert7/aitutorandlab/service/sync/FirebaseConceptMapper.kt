package com.ncert7.aitutorandlab.service.sync

import com.ncert7.aitutorandlab.data.local.entities.ConceptEntity
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
 *  - type: either "SIMULATION", "STUDY", or "MATH PROBLEM"
 *  - simulation_id: if type == simulation then the simulation ID (used for API calls)
 *  - simulation_url: if type == simulation then webpage url else empty or null
 *  - problem_id: if type == MATH PROBLEM then the problem ID
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

        val chapterId = document.getString("chapter_id")

        val conceptName = document.getString("concept_name")
        return ConceptEntity(
            conceptId = document.id,
            chapterId = chapterId?:"",
            conceptName = conceptName?:"",
            conceptNameKannada = document.getString("concept_name_kn") ?:"",
            orderIndex = document.getLong("conceptOrder")?.toInt() ?: 0,
            description = combinedDescription,
            hasSimulation = conceptType is ConceptType.Simulation,
            type = conceptType.raw,
            problemId = document.getString("problem_id") ?: "",
            problemTopicName=document.getString("problem_topic_name") ?: "",
            problemTopicNameKn=document.getString("problem_topic_name_kn") ?: "",
            simulationId = document.getString("simulation_id") ?: "",
            simulationIdKannada = document.getString("simulation_id_kn") ?: "",
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
    object Math : ConceptType("MATH PROBLEM")

    companion object {
        fun from(raw: String?): ConceptType {
            if (raw == null) {
                throw IllegalArgumentException("Type cannot be null")
            }

            val normalized = raw.trim().uppercase()

            return when (normalized) {
                "SIMULATION" -> Simulation
                "STUDY" -> Study
                "MATH PROBLEM" -> Math
                else -> {
                    // Try to give a helpful error with suggestions
                    throw IllegalArgumentException(
                        "Unknown concept type: '$raw' (normalized: '$normalized'). " +
                        "Allowed values: SIMULATION, STUDY, MATH PROBLEM"
                    )
                }
            }
        }
    }
}
