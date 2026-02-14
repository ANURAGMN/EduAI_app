package com.anurag.eduai.utils

import androidx.appcompat.app.AppCompatDelegate
import com.anurag.eduai.data.local.entities.ChapterEntity
import com.anurag.eduai.data.local.entities.ConceptEntity
import com.anurag.eduai.data.local.entities.SubjectEntity

/**
 * Extension functions to get localized names based on current app language
 */

fun SubjectEntity.getLocalizedName(): String {
    return if (isKannada()) subjectNameKannada else subjectName
}

fun ChapterEntity.getLocalizedName(): String {
    return if (isKannada()) chapterNameKannada else chapterName
}

fun ConceptEntity.getLocalizedName(): String {
    return if (isKannada()) conceptNameKannada else conceptName
}

private fun isKannada(): Boolean {
    val currentLocale = AppCompatDelegate.getApplicationLocales()[0]?.language
    return currentLocale == "kn"
}
