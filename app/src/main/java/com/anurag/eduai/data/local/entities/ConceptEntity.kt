package com.anurag.eduai.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
* Concept Entity
*/
@Entity(
    tableName = "concepts",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["chapterId"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chapterId"])]
)
data class ConceptEntity(
    @PrimaryKey
    val conceptId: String,
    val chapterId: String,
    val conceptName: String,
    val conceptNameKannada: String,
    val orderIndex: Int,
    val description: String? = null,
    val hasSimulation: Boolean = false,
    val type: String, // simulation , study
    val simulationId: String, //simulation id
    val simulationUrl: String? = null,
    val simulationUrlKannada: String? = null,
    val syncAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
