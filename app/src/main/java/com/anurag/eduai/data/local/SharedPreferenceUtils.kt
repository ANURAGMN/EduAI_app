package com.anurag.eduai.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPreferenceUtils(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
        private const val KEY_SESSION = "key_current_session"
    }

    fun setUserId(id: String) {
        prefs.edit { putString(KEY_USER_ID, id) }
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun setLanguagePreference(lang: String) {
        prefs.edit { putString(KEY_LANGUAGE, lang) }
    }

    fun getLanguagePreference(): String? {
        return prefs.getString(KEY_LANGUAGE, "en") // default: English
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit { putBoolean(KEY_IS_LOGGED_IN, isLoggedIn) }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    /** Returns last successful sync timestamp, or 0 if never synced. */
    fun getLastSyncTime(): Long {
        return prefs.getLong(KEY_LAST_SYNC, 0L)
    }

    /** Stores the timestamp of last successful sync. */
    fun updateLastSyncTime(time: Long = System.currentTimeMillis()) {
        prefs.edit { putLong(KEY_LAST_SYNC, time) }
    }

    /** Stores current session id **/
    fun setCurrentSession(sessionId:String){
        prefs.edit{putString(KEY_SESSION,sessionId)}
    }

    /** Retrieves current session id **/
    fun getCurrentSession(): String? {
        return prefs.getString(KEY_SESSION, null)
    }
    /** Clears current session id **/
    fun clearCurrentSession() {
        prefs.edit { remove(KEY_SESSION) }
    }
}