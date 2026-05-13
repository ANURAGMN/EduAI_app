package com.ncert7.aitutorandlab.repository

import com.ncert7.aitutorandlab.data.local.dao.StudentDao
import com.ncert7.aitutorandlab.data.local.entities.StudentEntity

class StudentLocalRepository(
    private val dao: StudentDao
) {

    suspend fun saveStudentLocally(student: StudentEntity) {
        dao.insertStudent(student)
    }

    fun getStudent(studentId: String) = dao.getStudent(studentId)

    suspend fun getStudentSync(studentId: String): StudentEntity? {
        return dao.getStudentSync(studentId)
    }
}
