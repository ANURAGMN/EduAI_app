package com.anurag.eduai.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Simulation Entity
 */
@Entity(
    tableName = "simulations",
    foreignKeys = [
        ForeignKey(
            entity = ConceptEntity::class,
            parentColumns = ["conceptId"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conceptId"])]
)
data class SimulationEntity(
    @PrimaryKey
    val simulationId: String,
    val conceptId: String,
    val simulationName: String,
    val simulationNameKannada: String,
    val simulationUrl: String,
    val description: String? = null,
    val orderIndex: Int = 0,
    val syncAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)