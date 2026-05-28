package com.ncert7.aitutorandlab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ncert7.aitutorandlab.data.local.entities.SubjectEntity
import kotlinx.coroutines.flow.Flow

/**
 * subjects DAO.
 */
@Dao
interface SubjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Query("SELECT * FROM subjects WHERE classLevel = :classLevel ORDER BY orderIndex ASC")
    fun getSubjectsForClass(classLevel: Int): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE classLevel = :classLevel ORDER BY orderIndex ASC")
    suspend fun getSubjectsForClassSync(classLevel: Int): List<SubjectEntity>

    @Query("SELECT * FROM subjects ORDER BY orderIndex ASC")
    suspend fun getAllSubjectsSync(): List<SubjectEntity>

    @Query("SELECT * FROM subjects WHERE subjectId = :subjectId")
    suspend fun getSubject(subjectId: String): SubjectEntity?

    @Query("SELECT * FROM subjects WHERE subjectId = :subjectId")
    fun getSubjectFlow(subjectId: String): Flow<SubjectEntity?>

    @Query("DELETE FROM subjects WHERE classLevel = :classLevel")
    suspend fun deleteSubjectsForClass(classLevel: Int)

    @Query("DELETE FROM subjects WHERE subjectId = :subjectId")
    suspend fun deleteSubject(subjectId: String)
}
