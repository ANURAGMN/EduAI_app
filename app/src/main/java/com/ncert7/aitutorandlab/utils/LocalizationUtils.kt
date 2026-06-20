package com.ncert7.aitutorandlab.utils

import androidx.appcompat.app.AppCompatDelegate
import com.ncert7.aitutorandlab.data.local.entities.ChapterEntity
import com.ncert7.aitutorandlab.data.local.entities.ConceptEntity
import com.ncert7.aitutorandlab.data.local.entities.SubjectEntity

/**
 * Extension functions to get localized names based on current app language
 */

fun SubjectEntity.getLocalizedName(): String {
    return if (isKannada())
        subjectNameKannada else subjectName
}

fun ChapterEntity.getLocalizedName(): String {
    return if (isKannada()) {
        chapterNameKannada.ifBlank { chapterName }
    } else {
        chapterName
    }
}

fun ConceptEntity.getLocalizedName(): String {
    return if (isKannada()) {
        conceptNameKannada.ifBlank { conceptName }
    } else {
        conceptName
    }
}

/**
 * Check if the app is currently in Kannada language
 */
fun isKannada(): Boolean {
    val currentLocale = AppCompatDelegate.getApplicationLocales()[0]?.language
    return currentLocale == "kn"
}

/**
 * Get current app language code (`en` or `kn`).
 */
fun getCurrentLanguageCode(): String {
    val currentLocale = AppCompatDelegate.getApplicationLocales()[0]?.language
    return normalizeLanguageCode(currentLocale)
}

/**
 * Normalize profile/UI language values to progress language codes used in Room DB.
 * Accepts: en, kn, English, Kannada, en-IN, kn-IN, etc.
 */
fun normalizeLanguageCode(raw: String?): String {
    if (raw.isNullOrBlank()) return "en"
    return when (raw.trim().lowercase()) {
        "kn", "kannada" -> "kn"
        "en", "english" -> "en"
        else -> if (raw.startsWith("kn", ignoreCase = true)) "kn" else "en"
    }
}

/**
 * Resolve language for progress writes/queries: explicit value first, else current app locale.
 */
fun resolveProgressLanguage(language: String? = null): String {
    return if (language.isNullOrBlank()) getCurrentLanguageCode() else normalizeLanguageCode(language)
}

/** Legacy Firestore/local rows without explicit language — excluded from today's counts. */
const val LEGACY_PROGRESS_LANGUAGE = "legacy"

/** Language codes used for new progress writes and today's progress queries. */
fun isExplicitProgressLanguage(language: String): Boolean =
    language == "en" || language == "kn"

/**
 * Resolve language when restoring progress from Firestore.
 * Legacy docs (no language field, no _en/_kn doc suffix) map to [LEGACY_PROGRESS_LANGUAGE].
 */
fun resolveProgressLanguageFromFirestore(documentId: String, languageField: String?): String {
    if (!languageField.isNullOrBlank()) return normalizeLanguageCode(languageField)
    return when {
        documentId.endsWith("_kn") -> "kn"
        documentId.endsWith("_en") -> "en"
        else -> LEGACY_PROGRESS_LANGUAGE
    }
}

/** Legacy values stored before normalization (Firebase profile / early builds). */
fun legacyProgressLanguageAlias(normalized: String): String? = when (normalized) {
    "en" -> "English"
    "kn" -> "Kannada"
    else -> null
}

