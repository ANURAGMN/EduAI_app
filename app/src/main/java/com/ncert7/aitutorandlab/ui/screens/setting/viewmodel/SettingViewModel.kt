package com.ncert7.aitutorandlab.ui.screens.setting.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncert7.aitutorandlab.data.local.ConceptSessionRepository
import com.ncert7.aitutorandlab.data.local.database.EduAiDatabase
import com.ncert7.aitutorandlab.data.local.SharedPreferenceUtils
import com.ncert7.aitutorandlab.data.local.dao.StudentDao
import com.ncert7.aitutorandlab.data.local.entities.StudentEntity
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.repository.FirebaseRepository
import com.ncert7.aitutorandlab.utils.LanguageHelper
import com.ncert7.aitutorandlab.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UpdateProfileState {
    object Idle : UpdateProfileState()
    object Loading : UpdateProfileState()
    object Success : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}

sealed class LogoutState {
    object Idle : LogoutState()
    object Loading : LogoutState()
    object Success : LogoutState()
    data class Error(val message: String) : LogoutState()
}
@HiltViewModel
class SettingViewModel @Inject constructor(
    private val sharedPref: SharedPreferenceUtils,
    private val repository: FirebaseRepository,
    private val studentDao: StudentDao,
    @ApplicationContext private val context: Context,
    val userId: String
) : ViewModel() {
    
    private val _student = MutableStateFlow<StudentEntity?>(null)
    val student: StateFlow<StudentEntity?> = _student.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateState: StateFlow<UpdateProfileState> = _updateState.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(
        // Load saved language on initialization, default to "en" if null
        sharedPref.getLanguagePreference() ?: "en"
    )
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _logoutState = MutableStateFlow<LogoutState>(
        LogoutState.Idle)
    val logoutState: StateFlow<LogoutState> = _logoutState.asStateFlow()

    init {
        // Load student profile
        loadStudent()
    }

    private fun loadStudent() {
        viewModelScope.launch {
            val result = studentDao.getStudentSync(userId)
            _student.value = result
        }
    }

    fun setLanguage(langCode: String) {
        viewModelScope.launch {
            // Update UI state immediately
            _selectedLanguage.value = langCode

            // Save to SharedPreferences
            sharedPref.setLanguagePreference(langCode)

            // Apply language change to app
            LanguageHelper.setLanguage(langCode)
        }
    }

    fun updateProfile(
        updatedName: String,
        updatedPhone: String,
        updatedSchool: String,
        updatedClass: Int
    ) {
        viewModelScope.launch {
            _updateState.value = UpdateProfileState.Loading

            val existing = studentDao.getStudentSync(userId)
            if (existing == null) {
                _updateState.value = UpdateProfileState.Error("User not found")
                return@launch
            }

            val updatedStudent =
                existing.copy(
                    studentName = updatedName,
                    phoneNumber = updatedPhone,
                    classLevel = updatedClass,
                    updatedAt = System.currentTimeMillis(),
                    isSynced = false
                )

            val firebaseSuccess =
                repository.updateUserProfile(
                    userId = existing.studentId,
                    name = updatedName,
                    phone = updatedPhone,
                    school = updatedSchool,
                    studentClass = updatedClass,
                    updatedAt = updatedStudent.updatedAt
                )

            if (firebaseSuccess) {
                studentDao.updateStudent(updatedStudent.copy(isSynced = true))
                _updateState.value = UpdateProfileState.Success
                // Reload student data
                loadStudent()
            } else {
                studentDao.updateStudent(updatedStudent)
                _updateState.value = UpdateProfileState.Error("Failed to sync with server")
            }
        }
    }

    fun resetState() {
        _updateState.value = UpdateProfileState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            try {
                _logoutState.value = LogoutState.Loading
                DebugLogger.debugLog("SettingViewModel", "Starting logout process")

                // Clear student data
                studentDao.deleteAllStudents()
                DebugLogger.debugLog("SettingViewModel", "Cleared student data")

                // Clear shared preferences
                sharedPref.clearAllUserData()
                DebugLogger.debugLog("SettingViewModel", "Cleared user preferences")

                // Clear Google authentication tokens
                TokenManager.clearAllTokens(context)
                DebugLogger.debugLog("SettingViewModel", "Cleared authentication tokens")

                // Clear all session mappings for chatbot
                ConceptSessionRepository(context).clearAllMappings()
                DebugLogger.debugLog("SettingViewModel", "Cleared concept session mappings")

                // Clear all sessions from database
                val db = EduAiDatabase.getInstance(context)
                db.sessionDao().deleteAllSessions()
                DebugLogger.debugLog("SettingViewModel", "Cleared database sessions")

                _logoutState.value = LogoutState.Success
                DebugLogger.debugLog("SettingViewModel", "Logout completed successfully")

            } catch (e: Exception) {
                DebugLogger.errorLog("SettingViewModel", "Error during logout: ${e.message}")
                // Still set logout state even if there's an error
                _logoutState.value = LogoutState.Error(e.message ?: "Logout failed")
            }
        }
    }
}