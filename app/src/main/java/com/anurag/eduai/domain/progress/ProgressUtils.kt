package com.anurag.eduai.domain.progress

import com.anurag.eduai.ui.models.ChapterProgressUiModel
import kotlin.ranges.coerceAtLeast
import kotlin.ranges.coerceIn

/**
 * Build ChapterProgressUiModel with all calculated progress data
 * This utility function is used across ViewModels to compute progress metrics
 *
 * @param completed Number of items completed
 * @param total Total number of items
 * @return ChapterProgressUiModel with calculated progress fraction, percentage, and remaining count
 */
fun buildProgressUiModel(
    completed: Int,
    total: Int
): ChapterProgressUiModel {
    val safeTotal = total.coerceAtLeast(1)
    val fraction = completed.toFloat() / safeTotal

    return ChapterProgressUiModel(
        completed = completed,
        total = total,
        progressFraction = fraction.coerceIn(0f, 1f),
        progressPercentage = (fraction * 100).toInt(),
        remaining = (total - completed).coerceAtLeast(0)
    )
}
