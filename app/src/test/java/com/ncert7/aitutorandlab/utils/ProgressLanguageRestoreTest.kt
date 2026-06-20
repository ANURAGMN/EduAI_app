package com.ncert7.aitutorandlab.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressLanguageRestoreTest {

    @Test
    fun resolveProgressLanguageFromFirestore_usesExplicitField() {
        assertEquals(
            "kn",
            resolveProgressLanguageFromFirestore("SIMULATION_abc", "Kannada")
        )
    }

    @Test
    fun resolveProgressLanguageFromFirestore_usesDocSuffix() {
        assertEquals(
            "en",
            resolveProgressLanguageFromFirestore("SIMULATION_abc_en", null)
        )
        assertEquals(
            "kn",
            resolveProgressLanguageFromFirestore("SIMULATION_abc_kn", null)
        )
    }

    @Test
    fun resolveProgressLanguageFromFirestore_marksLegacyWithoutLanguage() {
        assertEquals(
            LEGACY_PROGRESS_LANGUAGE,
            resolveProgressLanguageFromFirestore("SIMULATION_abc", null)
        )
    }

    @Test
    fun isExplicitProgressLanguage_onlyEnAndKn() {
        assertEquals(true, isExplicitProgressLanguage("en"))
        assertEquals(true, isExplicitProgressLanguage("kn"))
        assertEquals(false, isExplicitProgressLanguage("legacy"))
        assertEquals(false, isExplicitProgressLanguage("English"))
    }
}
