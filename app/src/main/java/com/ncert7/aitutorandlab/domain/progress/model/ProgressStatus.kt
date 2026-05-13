package com.ncert7.aitutorandlab.domain.progress.model

import androidx.annotation.StringRes
import com.ncert7.aitutorandlab.R

/**
 * Unified enum representing the status of a lesson/concept/chapter/simulation
 * Single source of truth for all progress tracking across the app
 */
enum class ProgressStatus(val value: String, @StringRes val displayNameRes: Int) {
    COMPLETED("COMPLETED", R.string.progress_status_completed),
    IN_PROGRESS("IN_PROGRESS", R.string.progress_status_in_progress),
    NOT_STARTED("NOT_STARTED", R.string.progress_status_not_started),
    LOCKED("LOCKED", R.string.progress_status_locked);

    companion object {
        fun fromString(status: String): ProgressStatus {
            return entries.find { it.value == status } ?: NOT_STARTED
        }
    }
}