package com.ncert7.aitutorandlab.service.analytics

import org.junit.Assert.assertEquals
import org.junit.Test

class ContentClickNavigationTest {

    @Test
    fun chapterType_mapsToContentClickType() {
        assertEquals(
            ContentClickType.CHAPTER_SIMULATION,
            chapterContentType("SIMULATION")
        )
        assertEquals(
            ContentClickType.CHAPTER_MATH,
            chapterContentType("MATH PROBLEM")
        )
        assertEquals(
            ContentClickType.CHAPTER_STUDY,
            chapterContentType("STUDY")
        )
    }

    private fun chapterContentType(type: String): ContentClickType {
        return when (type.uppercase()) {
            "SIMULATION" -> ContentClickType.CHAPTER_SIMULATION
            "MATH PROBLEM" -> ContentClickType.CHAPTER_MATH
            else -> ContentClickType.CHAPTER_STUDY
        }
    }
}
