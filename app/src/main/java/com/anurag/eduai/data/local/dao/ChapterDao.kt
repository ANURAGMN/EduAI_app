package com.anurag.eduai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anurag.eduai.data.local.entities.ChapterEntity
import com.anurag.eduai.data.local.entities.ConceptEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for managing chapters in the local database.
 */
@Dao
interface ChapterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY orderIndex ASC")
    fun getChaptersForSubject(subjectId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY orderIndex ASC")
    suspend fun getChaptersForSubjectSync(subjectId: String): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE chapterId = :chapterId")
    suspend fun getChapter(chapterId: String): ChapterEntity?

    @Query("SELECT * FROM chapters WHERE chapterId = :chapterId")
    fun getChapterFlow(chapterId: String): Flow<ChapterEntity?>

    @Query("SELECT * FROM chapters WHERE chapterId = :chapterId")
    suspend fun getChapterById(chapterId: String): ChapterEntity?

    @Query("DELETE FROM chapters WHERE subjectId = :subjectId")
    suspend fun deleteChaptersForSubject(subjectId: String)

    @Query("DELETE FROM chapters WHERE chapterId = :chapterId")
    suspend fun deleteChapter(chapterId: String)

    /**
     * Get all concepts for a chapter
     * Used for progress calculation
     */
    @Query("SELECT * FROM concepts WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    suspend fun getConceptsByChapterId(chapterId: String): List<ConceptEntity>

    /**
     * Get a single concept by its ID
     * Used to find chapter for a concept
     */
    @Query("SELECT * FROM concepts WHERE conceptId = :conceptId")
    suspend fun getConceptById(conceptId: String): ConceptEntity?

    /**
     * Find a chapter by its name (case-insensitive)
     * Used by RevisionViewModel to resolve chapter name → chapterId
     */
    @Query("SELECT * FROM chapters WHERE chapterName = :chapterName LIMIT 1")
    suspend fun getChapterByName(chapterName: String): ChapterEntity?
}