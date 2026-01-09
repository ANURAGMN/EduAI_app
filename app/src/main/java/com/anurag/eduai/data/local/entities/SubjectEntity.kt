package com.anurag.eduai.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Subject Entity
 */
@Entity(
    tableName = "subjects",
    indices = [Index(value = ["classLevel"])]
)
data class SubjectEntity(
    @PrimaryKey
    val subjectId: String,
    val subjectName: String,
    val subjectNameKannada: String,
    val classLevel: Int,
    val iconUrl: String? = null,
    val orderIndex: Int = 0,
    val totalChapters: Int = 0,
    val syncAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)