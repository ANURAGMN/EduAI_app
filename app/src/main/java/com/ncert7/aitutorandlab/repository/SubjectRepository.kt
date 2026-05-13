package com.ncert7.aitutorandlab.repository

import com.ncert7.aitutorandlab.data.local.dao.SubjectDao
import com.ncert7.aitutorandlab.data.local.entities.SubjectEntity

/**
 * Repository class for managing subject data.
 */
class SubjectRepository(
    private val subjectDao: SubjectDao
) {
    /**
     * Retrieves all subjects for a given class level.
     * returns List of SubjectEntity
     */
    suspend fun getSubjectsForClass(classLevel: Int): List<SubjectEntity> {
        return subjectDao.getSubjectsForClassSync(classLevel)
    }

    /**
     * Retrieves a specific subject by its ID.
     * returns SubjectEntity or null if not found
     */
    suspend fun getSubject(subjectId: String): SubjectEntity? {
        return subjectDao.getSubject(subjectId)
    }
}

