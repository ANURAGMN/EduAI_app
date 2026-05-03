package com.anurag.eduai.ui.screens.setting.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.ConceptSessionRepository
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.data.local.dao.StudentDao
import com.anurag.eduai.data.local.entities.StudentEntity
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.FirebaseRepository
import com.anurag.eduai.utils.LanguageHelper
import com.anurag.eduai.utils.StreakManager
import com.anurag.eduai.utils.TokenManager
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

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val sharedPref: SharedPreferenceUtils,
    private val repository: FirebaseRepository,
    private val studentDao: StudentDao,
    @ApplicationContext private val context: Context,
    val userId: String
) : ViewModel() {

    // ...existing code...

    private val _student = MutableStateFlow<StudentEntity?>(null)
    val student: StateFlow<StudentEntity?> = _student.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateState: StateFlow<UpdateProfileState> = _updateState.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(
        // Load saved language on initialization, default to "en" if null
        sharedPref.getLanguagePreference() ?: "en"
    )
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _logoutState = MutableStateFlow(false)
    val logoutState: StateFlow<Boolean> = _logoutState.asStateFlow()

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

    fun updateProfilePhoto(localPath: String) {
        viewModelScope.launch {
            val existing = studentDao.getStudentSync(userId) ?: return@launch

            val updated =
                existing.copy(
                    profilePhotoUrl = localPath,
                    updatedAt = System.currentTimeMillis(),
                    isSynced = false
                )

            studentDao.updateStudent(updated)
            // Reload student data
            loadStudent()
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                DebugLogger.debugLog("SettingViewModel", "Starting logout process")

                // Clear Google authentication tokens
                TokenManager.clearAllTokens(context)
                DebugLogger.debugLog("SettingViewModel", "Cleared authentication tokens")

                // Clear all session mappings for chatbot
                ConceptSessionRepository(context).clearAllMappings()
                DebugLogger.debugLog("SettingViewModel", "Cleared concept session mappings")

                // Clear streak data
                StreakManager(context).resetStreak()
                DebugLogger.debugLog("SettingViewModel", "Reset streak data")

                // Clear shared preferences (user data, login status, etc.)
                sharedPref.clearAllUserData()
                DebugLogger.debugLog("SettingViewModel", "Cleared user preferences")

                // Clear all sessions from database
                val db = EduAiDatabase.getInstance(context)
                db.sessionDao().deleteAllSessions()
                DebugLogger.debugLog("SettingViewModel", "Cleared database sessions")

                // Clear all student data from local database
                studentDao.deleteAllStudents()
                DebugLogger.debugLog("SettingViewModel", "Cleared local student data")

                // Set logout state to trigger navigation
                _logoutState.value = true
                DebugLogger.debugLog("SettingViewModel", "Logout completed successfully")

            } catch (e: Exception) {
                DebugLogger.errorLog("SettingViewModel", "Error during logout: ${e.message}")
                // Still set logout state even if there's an error
                _logoutState.value = true
            }
        }
    }
}