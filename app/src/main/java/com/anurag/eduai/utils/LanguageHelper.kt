package com.anurag.eduai.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Helper to switch app language at runtime using Android's resource system.
 *
 * Usage:
 *   LanguageHelper.setLanguage("en")  // English
 *   LanguageHelper.setLanguage("kn")  // Kannada
 */
object LanguageHelper {

    /**
     * @param languageCode ISO language code, e.g. "en", "kn"
     */
    fun setLanguage(languageCode: String) {
        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

}