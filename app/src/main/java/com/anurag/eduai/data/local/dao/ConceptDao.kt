package com.anurag.eduai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anurag.eduai.data.local.entities.ConceptEntity
import kotlinx.coroutines.flow.Flow

//Concept DAO
@Dao
interface ConceptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcepts(concepts: List<ConceptEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcept(concept: ConceptEntity)

    @Update
    suspend fun updateConcept(concept: ConceptEntity)

    @Query("SELECT * FROM concepts WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    fun getConceptsForChapter(chapterId: String): Flow<List<ConceptEntity>>

    @Query("SELECT * FROM concepts WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    suspend fun getConceptsForChapterSync(chapterId: String): List<ConceptEntity>

    @Query("SELECT * FROM concepts WHERE conceptId = :conceptId")
    suspend fun getConcept(conceptId: String): ConceptEntity?

    @Query("SELECT * FROM concepts WHERE conceptId = :conceptId")
    fun getConceptFlow(conceptId: String): Flow<ConceptEntity?>

    // Get next 2 concepts to show as "locked" in UI
    @Query("SELECT * FROM concepts WHERE chapterId = :chapterId AND orderIndex > :currentIndex ORDER BY orderIndex ASC LIMIT :limit")
    suspend fun getNextConcepts(chapterId: String, currentIndex: Int, limit: Int = 2): List<ConceptEntity>

    @Query("DELETE FROM concepts WHERE chapterId = :chapterId")
    suspend fun deleteConceptsForChapter(chapterId: String)

    @Query("DELETE FROM concepts WHERE conceptId = :conceptId")
    suspend fun deleteConcept(conceptId: String)
}