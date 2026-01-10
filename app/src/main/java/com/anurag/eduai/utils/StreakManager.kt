package com.anurag.eduai.utils

import android.content.Context
import java.util.Calendar

/**
 * StreakManager is responsible for tracking the user's learning streak
 * based on when the Concept screen is opened.
 */
class StreakManager(context: Context) {

    /**
     * Private SharedPreferences file used only for streak-related state.
     * This isolates behavioral tracking from the rest of the app data.
     */
    private val prefs = context.getSharedPreferences("streak_prefs", Context.MODE_PRIVATE)

    private companion object {

        const val KEY_LAST_OPEN_TIME = "last_open_time"
        const val KEY_LAST_STREAK_DAY = "last_streak_day"
        const val KEY_STREAK_COUNT = "streak_count"
        const val STREAK_WINDOW = 24 * 60 * 60 * 1000L
    }

    /**
     * Records a Concept screen open and updates the streak accordingly.
     *
     * Streak behavior:
     * - If the previous open occurred within the defined time window,
     *   the streak is incremented.
     * - If the time window has been exceeded, the streak is reset to 1.
     */
    fun onConceptOpened(): Int {
        val now = System.currentTimeMillis()

        val lastStreakDay = prefs.getLong(KEY_LAST_STREAK_DAY, 0L)
        val oldStreak = prefs.getInt(KEY_STREAK_COUNT, 0)

        val newStreak = when {
            // First ever streak event
            lastStreakDay == 0L -> 1

            // Same calendar day → do NOT increment
            isSameDay(lastStreakDay, now) -> oldStreak

            // Next valid day within time window → continue streak
            now - lastStreakDay <= STREAK_WINDOW -> oldStreak + 1

            // Too much time has passed → reset streak
            else -> 1
        }

        prefs.edit()
            .putLong(KEY_LAST_STREAK_DAY, now)
            .putInt(KEY_STREAK_COUNT, newStreak)
            .apply()

        return newStreak
    }

    /**
     * Provides read-only access to the currently stored streak value.
     */
    fun getCurrentStreak(): Int {
        return prefs.getInt(KEY_STREAK_COUNT, 0)
    }

    // call this on logout
    fun resetStreak() {
        prefs.edit()
            .remove(KEY_LAST_OPEN_TIME)
            .remove(KEY_STREAK_COUNT)
            .apply()
    }
    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
