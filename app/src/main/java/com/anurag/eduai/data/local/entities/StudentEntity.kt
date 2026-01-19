package com.anurag.eduai.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Student Entity
 */
@Entity(
    tableName = "students",
    indices = [Index(value = ["phoneNumber"], unique = true)]
)
data class StudentEntity(
    @PrimaryKey
    val studentId: String, // Firebase UID
    val studentName: String,
    val email: String,
    val phoneNumber: String,
    val studentSchool: String,
    val language: String, // "English" or "Kannada"
    val classLevel: Int = 7, // Default class 7
    val profilePhotoUrl: String? = null,
    val localProfilePhotoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
