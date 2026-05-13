package com.ncert7.aitutorandlab.ui.screens.login.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncert7.aitutorandlab.data.firebase.model.User
import com.ncert7.aitutorandlab.data.local.database.EduAiDatabase
import com.ncert7.aitutorandlab.data.local.SharedPreferenceUtils
import com.ncert7.aitutorandlab.data.local.entities.StudentEntity
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.repository.FirebaseRepository
import com.ncert7.aitutorandlab.repository.StudentLocalRepository
import com.ncert7.aitutorandlab.repository.UserCheckResult
import com.ncert7.aitutorandlab.service.sync.FirebaseSyncManager
import com.ncert7.aitutorandlab.utils.LanguageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repo: FirebaseRepository,
    private val sharedPreferenceUtils: SharedPreferenceUtils,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    private val _user = MutableStateFlow(User())
    val user = _user.asStateFlow()

    private val _userSaveState = MutableStateFlow<UserSaveState>(UserSaveState.Idle)
    val userSaveState = _userSaveState.asStateFlow()

    private val _existingUserSyncState = MutableStateFlow<ExistingUserSyncState>(ExistingUserSyncState.Idle)
    val existingUserSyncState = _existingUserSyncState.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(
        // Load saved language on initialization, default to "en" if null
        sharedPreferenceUtils.getLanguagePreference() ?: "en"
    )
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    init {
        DebugLogger.debugLog("UserViewModel", "UserViewModel initialized")
    }

    fun updateId(id: String) {
        _user.value = _user.value.copy(id = id)
    }

    fun updateName(name: String?) {
        _user.value = _user.value.copy(displayName = name)
    }

    fun updateEmail(email: String) {
        _user.value = _user.value.copy(email = email)
    }

    fun updateProfilePictureUri(uri: String?) {
        _user.value = _user.value.copy(profilePictureUri = uri)
    }

    fun updateSchool(school: String) {
        _user.value = _user.value.copy(schoolName = school)
    }

    fun updatePhoneNumber(phone: String) {
        _user.value = _user.value.copy(phoneNumber = phone)
    }

    fun updateClass(stdClass: Int) {
        _user.value = _user.value.copy(studentClass = stdClass)
    }

    fun updateLanguage(language: String) {
        _user.value = _user.value.copy(language = language)
    }

    fun updateCreatedAt(createdAt: Long) {
        _user.value = _user.value.copy(createdAt = createdAt)
    }

    fun updateUpdatedAt(updatedAt: Long) {
        _user.value = _user.value.copy(lastLogin = updatedAt)
    }

    /**
     * Convenience method to update the entire user object
     * Useful when receiving user data from Google Sign-In
     */
    fun updateUser(user: User) {
        _user.value = user
    }

    /**
     * Set language with language code (en, kn, etc.)
     * Updates UI state, saves to SharedPreferences, and applies to app
     */
    fun setLanguage(langCode: String) {
        viewModelScope.launch {
            // Update UI state immediately
            _selectedLanguage.value = langCode

            // Save to SharedPreferences
            sharedPreferenceUtils.setLanguagePreference(langCode)

            // Apply language change to app
            LanguageHelper.setLanguage(langCode)
        }
    }

    /**
     * Handle Google login flow
     * Checks if user exists in Firebase by email and appName
     * Updates login state accordingly
     */
    fun handleGoogleLogin(firebaseUser: User) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                // Use the currently selected language from state
                val currentLanguage = _selectedLanguage.value

                // Update language for new users
                updateLanguage(currentLanguage)

                // Check if user exists in Firebase by email
                // firebaseUser.id contains the email from GoogleIdTokenCredential
                when (val result = repo.checkUserExists(firebaseUser.email)) {
                    is UserCheckResult.Found -> {
                        _user.value = result.user
                        _loginState.value = LoginState.ExistingUser(result.user)
                        DebugLogger.debugLog("UserViewModel", "Existing user found - Email: ${firebaseUser.email}")
                    }

                    is UserCheckResult.NotFound -> {
                        _user.value = firebaseUser.copy(language = currentLanguage)
                        DebugLogger.debugLog("UserViewModel", "New user detected - Email: ${firebaseUser.email}")
                        _loginState.value = LoginState.NewUser
                    }

                    is UserCheckResult.Error -> {
                        _loginState.value = LoginState.Error(result.exception)
                    }
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e)
                DebugLogger.debugLog("UserViewModel", "Error during login: ${e.message}")
            }
        }
    }

    /**
     * Check if user exists - delegates to repository
     */
    suspend fun checkUserExists(uid: String): UserCheckResult {
        return repo.checkUserExists(uid)
    }

    /**
     * Save existing user data locally and sync content
     * This is called when an existing user logs in
     */
    fun saveExistingUserLocally(context: Context) {
        viewModelScope.launch {
            _existingUserSyncState.value = ExistingUserSyncState.Syncing
            try {
                val currentUser = _user.value
                val db = EduAiDatabase.getInstance(context)
                val localRepo = StudentLocalRepository(db.studentDao())
                val sharedPreference = SharedPreferenceUtils(context)

                // Save to local database
                val studentEntity = StudentEntity(
                    studentId = currentUser.id,
                    studentName = currentUser.displayName.orEmpty(),
                    email = currentUser.email,
                    phoneNumber = currentUser.phoneNumber,
                    studentSchool = currentUser.schoolName,
                    language = currentUser.language,
                    classLevel = currentUser.studentClass,
                    profilePhotoUrl = currentUser.profilePictureUri,
                    createdAt = currentUser.createdAt,
                    updatedAt = currentUser.lastLogin,
                    isSynced = true
                )
                localRepo.saveStudentLocally(studentEntity)

                // Sync content from Firebase
                val syncManager = FirebaseSyncManager(
                    subjectDao = db.subjectDao(),
                    chapterDao = db.chapterDao(),
                    conceptDao = db.conceptDao()
                )
                val result = syncManager.syncAllContent()
                DebugLogger.debugLog("UserViewModel", "Content sync: ${result.message}")

                // Save preferences
                sharedPreference.setLoggedIn(true)
                sharedPreference.setLanguagePreference(currentUser.language)
                sharedPreference.setUserId(currentUser.id)

                // Trigger restoration of user-specific data (Progress, Streaks, etc.)
                com.ncert7.aitutorandlab.service.sync.DataSyncService.restoreUserData(context, currentUser.id)

                _existingUserSyncState.value = ExistingUserSyncState.Success
            } catch (e: Exception) {
                DebugLogger.debugLog("UserViewModel", "Error saving user locally: ${e.message}")
                _existingUserSyncState.value = ExistingUserSyncState.Error(e)
            }
        }
    }

    /**
     * Submit new user data to Firebase and save locally
     * This is called when a new user completes registration
     */
    fun submitNewUser(context: Context) {
        viewModelScope.launch {
            _userSaveState.value = UserSaveState.Saving
            try {
                val currentUser = _user.value

                // Debug logging to verify user ID
                DebugLogger.debugLog("UserViewModel", "Submitting new user with ID: ${currentUser.id}")
                DebugLogger.debugLog("UserViewModel", "User email: ${currentUser.email}")
                DebugLogger.debugLog("UserViewModel", "User name: ${currentUser.displayName}")

                // Create user in Firebase
                val success = repo.createNewUser(currentUser)

                if (success) {
                    // Save to local database
                    val db = EduAiDatabase.getInstance(context)
                    val localRepo = StudentLocalRepository(db.studentDao())
                    val sharedPreference = SharedPreferenceUtils(context)

                    val studentEntity = StudentEntity(
                        studentId = currentUser.id,
                        studentName = currentUser.displayName.orEmpty(),
                        email = currentUser.email,
                        phoneNumber = currentUser.phoneNumber,
                        studentSchool = currentUser.schoolName,
                        language = currentUser.language,
                        classLevel = currentUser.studentClass,
                        profilePhotoUrl = currentUser.profilePictureUri,
                        createdAt = currentUser.createdAt,
                        updatedAt = currentUser.lastLogin,
                        isSynced = true
                    )
                    localRepo.saveStudentLocally(studentEntity)

                    // Sync content from Firebase
                    val syncManager = FirebaseSyncManager(
                        subjectDao = db.subjectDao(),
                        chapterDao = db.chapterDao(),
                        conceptDao = db.conceptDao()
                    )
                    val result = syncManager.syncAllContent()
                    DebugLogger.debugLog("UserViewModel", "Content sync: ${result.message}")

                    // Save preferences
                    sharedPreference.setLoggedIn(true)
                    sharedPreference.setLanguagePreference(currentUser.language)
                    sharedPreference.setUserId(currentUser.id)

                    _userSaveState.value = UserSaveState.Success
                } else {
                    _userSaveState.value = UserSaveState.Error(Exception("Failed to create user"))
                }
            } catch (e: Exception) {
                _userSaveState.value = UserSaveState.Error(e)
                DebugLogger.debugLog("UserViewModel", "Error submitting user: ${e.message}")
            }
        }
    }

    /**
     * Reset login state to Idle
     * Useful when navigating away from login screens
     */
    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }

    /**
     * Reset user save state to Idle
     */
    fun resetUserSaveState() {
        _userSaveState.value = UserSaveState.Idle
    }

    /**
     * Reset existing user sync state to Idle
     */
    fun resetExistingUserSyncState() {
        _existingUserSyncState.value = ExistingUserSyncState.Idle
    }

    /**
     * Reset all user state after logout
     */
    fun resetUserState() {
        DebugLogger.debugLog("UserViewModel", "Resetting user state after logout")
        _loginState.value = LoginState.Idle
        _user.value = User()
        _userSaveState.value = UserSaveState.Idle
        _existingUserSyncState.value = ExistingUserSyncState.Idle
        _selectedLanguage.value = sharedPreferenceUtils.getLanguagePreference() ?: "en"
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class ExistingUser(val currentUser: User) : LoginState()
    object NewUser : LoginState()
    data class Error(val exception: Throwable) : LoginState()
}

sealed class UserSaveState {
    object Idle : UserSaveState()
    object Saving : UserSaveState()
    object Success : UserSaveState()
    data class Error(val exception: Throwable) : UserSaveState()
}

sealed class ExistingUserSyncState {
    object Idle : ExistingUserSyncState()
    object Syncing : ExistingUserSyncState()
    object Success : ExistingUserSyncState()
    data class Error(val exception: Throwable) : ExistingUserSyncState()
}