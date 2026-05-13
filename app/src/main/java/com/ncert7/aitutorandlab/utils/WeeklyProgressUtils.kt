package com.ncert7.aitutorandlab.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class WeeklyProgressUtils {
    /** Returns the time in milliseconds for the moment exactly 7 days ago from now */
    fun getSevenDaysAgoInMillis(): Long {
        return System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
    }
    /**
     * Converts a date string in format YYYY-MM-DD to day of week
     * Example: "2026-01-09" -> "Fri"
     */
    fun getDayOfWeek(date: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val localDate = LocalDate.parse(date, formatter)
        return localDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH)
    }
}