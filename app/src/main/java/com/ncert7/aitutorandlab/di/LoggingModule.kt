package com.ncert7.aitutorandlab.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.ncert7.aitutorandlab.data.local.SharedPreferenceUtils
import com.ncert7.aitutorandlab.service.logging.FirestoreErrorLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency Injection Module for Logging Services
 * Provides Firestore error logging throughout the app
 */
@Module
@InstallIn(SingletonComponent::class)
object LoggingModule {

    @Provides
    @Singleton
    fun provideFirestoreErrorLogger(
        @ApplicationContext context: Context,
        sharedPrefs: SharedPreferenceUtils
    ): FirestoreErrorLogger {
        return FirestoreErrorLogger(context, sharedPrefs)
    }
}
