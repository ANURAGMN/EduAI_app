package com.anurag.eduai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anurag.eduai.data.local.entities.SimulationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for managing simulations in the local database.
 */
@Dao
interface SimulationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSimulations(simulations: List<SimulationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSimulation(simulation: SimulationEntity)

    @Update
    suspend fun updateSimulation(simulation: SimulationEntity)

    @Query("SELECT * FROM simulations WHERE conceptId = :conceptId ORDER BY orderIndex ASC")
    fun getSimulationsForConcept(conceptId: String): Flow<List<SimulationEntity>>

    @Query("SELECT * FROM simulations WHERE conceptId = :conceptId ORDER BY orderIndex ASC")
    suspend fun getSimulationsForConceptSync(conceptId: String): List<SimulationEntity>

    @Query("SELECT * FROM simulations WHERE simulationId = :simulationId")
    suspend fun getSimulation(simulationId: String): SimulationEntity?

    @Query("SELECT * FROM simulations WHERE simulationId = :simulationId")
    fun getSimulationFlow(simulationId: String): Flow<SimulationEntity?>

    @Query("SELECT * FROM simulations ORDER BY orderIndex ASC")
    fun getAllSimulations(): Flow<List<SimulationEntity>>

    @Query("DELETE FROM simulations WHERE conceptId = :conceptId")
    suspend fun deleteSimulationsForConcept(conceptId: String)

    @Query("DELETE FROM simulations WHERE simulationId = :simulationId")
    suspend fun deleteSimulation(simulationId: String)
}