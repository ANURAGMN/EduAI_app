package com.ncert7.aitutorandlab.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Chapter Entity
 */
@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["subjectId"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId"])]
)
data class ChapterEntity(
    @PrimaryKey
    val chapterId: String,
    val subjectId: String,
    val chapterName: String,
    val chapterNameKannada: String,
    val revisionId:String,
    val problemId: List<String>,
    val orderIndex: Int,
    val totalConcepts: Int = 0,
    val syncAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)