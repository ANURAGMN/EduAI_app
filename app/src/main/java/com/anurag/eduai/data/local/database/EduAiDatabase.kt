package com.anurag.eduai.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.anurag.eduai.data.local.dao.AppAnalyticsDao
import com.anurag.eduai.data.local.dao.ChapterAgentProgressDao
import com.anurag.eduai.data.local.dao.ChapterDao
import com.anurag.eduai.data.local.dao.ConceptDao
import com.anurag.eduai.data.local.dao.ProgressDao
import com.anurag.eduai.data.local.dao.SessionDao
import com.anurag.eduai.data.local.dao.StreakDao
import com.anurag.eduai.data.local.dao.StudentDao
import com.anurag.eduai.data.local.dao.SubjectDao
import com.anurag.eduai.data.local.entities.AppAnalyticsEntity
import com.anurag.eduai.data.local.entities.ChapterAgentProgressEntity
import com.anurag.eduai.data.local.entities.ChapterEntity
import com.anurag.eduai.data.local.entities.ConceptEntity
import com.anurag.eduai.data.local.entities.ProgressEntity
import com.anurag.eduai.data.local.entities.SessionEntity
import com.anurag.eduai.data.local.entities.StreakEntity
import com.anurag.eduai.data.local.entities.StudentEntity
import com.anurag.eduai.data.local.entities.SubjectEntity

/**
 * Main Room Database for EduAi App
 */
@Database(
    entities = [
        StudentEntity::class,
        SubjectEntity::class,
        ChapterEntity::class,
        ConceptEntity::class,
        SessionEntity::class,
        AppAnalyticsEntity::class,
        ProgressEntity::class,
        StreakEntity::class,
        ChapterAgentProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EduAiDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun subjectDao(): SubjectDao
    abstract fun chapterDao(): ChapterDao
    abstract fun conceptDao(): ConceptDao
    abstract fun progressDao(): ProgressDao
    abstract fun sessionDao(): SessionDao
    abstract fun appAnalyticsDao(): AppAnalyticsDao
    abstract fun streakDao(): StreakDao
    abstract fun chapterAgentProgressDao(): ChapterAgentProgressDao

    companion object {
        @Volatile
        private var INSTANCE: EduAiDatabase? = null

        private const val DATABASE_NAME = "eduai_database"

        fun getInstance(context: Context): EduAiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EduAiDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}