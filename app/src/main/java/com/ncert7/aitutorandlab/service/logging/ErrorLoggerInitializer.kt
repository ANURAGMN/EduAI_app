package com.ncert7.aitutorandlab.service.logging

import com.ncert7.aitutorandlab.debug.DebugLogger

/**
 * Initialize Firestore Error Logger
 * Call this in your Application.onCreate()
 */
object ErrorLoggerInitializer {

    /**
     * Initialize the Firestore error logger
     * Should be called once in Application.onCreate() or MainActivity
     *
     * Example in your MainActivity or Application class:
     * ```kotlin
     * override fun onCreate(savedInstanceState: Bundle?) {
     *     super.onCreate(savedInstanceState)
     *     // Initialize Hilt first
     *     // Then initialize error logger
     *     ErrorLoggerInitializer.initialize(this)
     * }
     * ```
     */
    fun initialize(firestoreLogger: FirestoreErrorLogger) {
        DebugLogger.setFirestoreLogger(firestoreLogger)
        DebugLogger.infoLog("ErrorLoggerInitializer", "Firestore Error Logger initialized successfully")
    }
}
