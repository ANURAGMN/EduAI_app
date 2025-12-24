package com.anurag.eduai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anurag.eduai.data.local.entities.ChapterEntity
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

    @Query("DELETE FROM chapters WHERE subjectId = :subjectId")
    suspend fun deleteChaptersForSubject(subjectId: String)

    @Query("DELETE FROM chapters WHERE chapterId = :chapterId")
    suspend fun deleteChapter(chapterId: String)
}