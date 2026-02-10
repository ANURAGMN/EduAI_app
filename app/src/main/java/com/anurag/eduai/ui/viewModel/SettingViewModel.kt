package com.anurag.eduai.ui.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.data.local.dao.StudentDao
import com.anurag.eduai.data.local.entities.StudentEntity
import com.anurag.eduai.repository.FirebaseRepository
import com.anurag.eduai.utils.LanguageHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UpdateProfileState {
    object Idle : UpdateProfileState()
    object Loading : UpdateProfileState()
    object Success : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}

class SettingViewModel(
    context: Context
) : ViewModel() {

    val sharedPref = SharedPreferenceUtils(context)
    val repository: FirebaseRepository = FirebaseRepository()
    val db = EduAiDatabase.getInstance(context)
    val studentDao = db.studentDao()
    val userId = sharedPref.getUserId().toString()

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
                    localProfilePhotoUri = localPath,
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

            // Set logout state to trigger navigation
            _logoutState.value = true
        }
    }
}