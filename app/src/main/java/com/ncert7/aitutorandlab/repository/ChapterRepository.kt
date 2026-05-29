package com.ncert7.aitutorandlab.repository

import com.ncert7.aitutorandlab.config.AppConfig
import com.ncert7.aitutorandlab.data.local.dao.ChapterDao
import com.ncert7.aitutorandlab.data.local.dao.ProgressDao
import com.ncert7.aitutorandlab.data.local.entities.ChapterEntity
import com.ncert7.aitutorandlab.utils.getCurrentLanguageCode

/**
 * Repository class for managing chapter data and related progress.
 */
class ChapterRepository(
    private val chapterDao: ChapterDao,
    private val progressDao: ProgressDao
) {
    /**
     * Retrieves all chapters for a given subject ID.
     * returns List of ChapterEntity
     */
    suspend fun getChaptersForSubject(subjectId: String): List<ChapterEntity> {
        return chapterDao.getChaptersForSubjectSync(subjectId)
    }

    /**
     * Retrieves a specific chapter by its ID.
     * returns ChapterEntity or null if not found
     */
    suspend fun getChapter(chapterId: String): ChapterEntity? {
        return chapterDao.getChapter(chapterId)
    }

    /**
     * Retrieves chapter-wise progress for a student in a specific subject.
     * Does NOT filter by classLevel — subjectId uniquely identifies the subject.
     * returns Flow of List of ChapterProgressSummary
     *
     * @param studentId The student ID
     * @param subjectId The subject ID
     * @param language Kept for compatibility with existing callers, but not used in the unified progress query.
     */
    fun getChapterWiseProgress(
        studentId: String,
        subjectId: String,
        language: String
    ) = progressDao.getChapterWiseProgressFlow(
        studentId,
        subjectId,
        getCurrentLanguageCode(),
        AppConfig.APP_NAME
    )
}
