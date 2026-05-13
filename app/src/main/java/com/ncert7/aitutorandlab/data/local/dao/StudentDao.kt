package com.ncert7.aitutorandlab.data.local.dao

import androidx.room.*
import com.ncert7.aitutorandlab.data.local.entities.StudentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Student DAO
 */
@Dao
interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Query("SELECT * FROM students WHERE studentId = :studentId")
    fun getStudent(studentId: String): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE studentId = :studentId")
    suspend fun getStudentSync(studentId: String): StudentEntity?

    @Query("SELECT * FROM students WHERE isSynced = 0")
    suspend fun getUnsyncedStudents(): List<StudentEntity>

    @Query("UPDATE students SET isSynced = 1 WHERE studentId = :studentId")
    suspend fun markAsSynced(studentId: String)

    @Query("UPDATE students SET language = :language, updatedAt = :timestamp, isSynced = 0 WHERE studentId = :studentId")
    suspend fun updateLanguage(
        studentId: String,
        language: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM students WHERE studentId = :studentId")
    suspend fun deleteStudent(studentId: String)

    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()
}