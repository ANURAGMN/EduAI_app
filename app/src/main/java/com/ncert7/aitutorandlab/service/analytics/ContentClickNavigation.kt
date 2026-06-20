package com.ncert7.aitutorandlab.service.analytics

/**
 * Helpers to keep navigation-layer click tracking consistent.
 */
object ContentClickNavigation {

    suspend fun trackSubjectClick(subjectId: String) {
        ContentClickAnalyticsTracker.trackClickAndWait(
            itemId = subjectId,
            contentType = ContentClickType.SUBJECT,
            source = ClickSource.SUBJECT_LIST
        )
    }

    suspend fun trackChapterListClick(chapterId: String, type: String) {
        val contentType = when (type.uppercase()) {
            "SIMULATION" -> ContentClickType.CHAPTER_SIMULATION
            "MATH PROBLEM" -> ContentClickType.CHAPTER_MATH
            else -> ContentClickType.CHAPTER_STUDY
        }
        ContentClickAnalyticsTracker.trackClickAndWait(
            itemId = chapterId,
            contentType = contentType,
            source = ClickSource.CHAPTER_LIST
        )
    }

    suspend fun trackRevisionClick(chapterId: String) {
        ContentClickAnalyticsTracker.trackClickAndWait(
            itemId = chapterId,
            contentType = ContentClickType.REVISION,
            source = ClickSource.CHAPTER_LIST
        )
    }

    suspend fun trackConceptClick(conceptId: String, problemId: String, conceptType: String) {
        if (conceptType.equals("MATH PROBLEM", ignoreCase = true)) {
            ContentClickAnalyticsTracker.trackClickAndWait(
                itemId = problemId.ifBlank { conceptId },
                contentType = ContentClickType.MATH_PROBLEM,
                source = ClickSource.CONCEPT_LIST
            )
        } else {
            ContentClickAnalyticsTracker.trackClickAndWait(
                itemId = conceptId,
                contentType = ContentClickType.STUDY,
                source = ClickSource.CONCEPT_LIST
            )
        }
    }

    suspend fun trackHomeLessonClick(conceptId: String) {
        ContentClickAnalyticsTracker.trackClickAndWait(
            itemId = conceptId,
            contentType = ContentClickType.LESSON,
            source = ClickSource.HOME
        )
    }
}
