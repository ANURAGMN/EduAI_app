package com.anurag.eduai.di

import com.anurag.eduai.data.local.dao.ChapterDao
import com.anurag.eduai.data.local.dao.ConceptDao
import com.anurag.eduai.data.local.dao.ProgressDao
import com.anurag.eduai.data.local.dao.StreakDao
import com.anurag.eduai.data.local.dao.StudentDao
import com.anurag.eduai.data.local.dao.SubjectDao
import com.anurag.eduai.repository.ChapterRepository
import com.anurag.eduai.repository.ConceptRepository
import com.anurag.eduai.repository.FirebaseRepository
import com.anurag.eduai.repository.SimulationRepository
import com.anurag.eduai.repository.StreakRepository
import com.anurag.eduai.repository.StudentLocalRepository
import com.anurag.eduai.repository.SubjectRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideConceptRepository(
        conceptDao: ConceptDao,
        progressDao: ProgressDao
    ): ConceptRepository {
        return ConceptRepository(conceptDao, progressDao)
    }

    @Provides
    @Singleton
    fun provideChapterRepository(
        chapterDao: ChapterDao,
        progressDao: ProgressDao
    ): ChapterRepository {
        return ChapterRepository(chapterDao, progressDao)
    }

    @Provides
    @Singleton
    fun provideSubjectRepository(subjectDao: SubjectDao): SubjectRepository {
        return SubjectRepository(subjectDao)
    }

    @Provides
    @Singleton
    fun provideStudentRepository(studentDao: StudentDao): StudentLocalRepository {
        return StudentLocalRepository(studentDao)
    }

    @Provides
    @Singleton
    fun provideFirebaseRepository(): FirebaseRepository {
        return FirebaseRepository()
    }

    @Provides
    @Singleton
    fun provideSimulationRepository(conceptDao: ConceptDao): SimulationRepository {
        return SimulationRepository(conceptDao)
    }
    @Provides
    @Singleton
    fun provideStreakRepository(
        streakDao: StreakDao,
        firebaseRepository: FirebaseRepository
    ): StreakRepository {
        return StreakRepository(streakDao, firebaseRepository)
    }
}
