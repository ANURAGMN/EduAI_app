package com.anurag.eduai.ui.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.firebase.User
import com.anurag.eduai.data.local.EduAiDatabase
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.data.local.entities.StudentEntity
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.FirebaseRepository
import com.anurag.eduai.repository.StudentLocalRepository
import com.anurag.eduai.sync.FirebaseSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val repo: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    private val _user = MutableStateFlow(User())
    val user = _user.asStateFlow()

    private val _userSaveState = MutableStateFlow<UserSaveState>(UserSaveState.Idle)
    val userSaveState = _userSaveState.asStateFlow()

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
     * Handle Google login flow
     * Checks if user exists in Firebase and updates login state accordingly
     */
    fun handleGoogleLogin(firebaseUser: User, selectedLanguage: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                // Update language for new users
                updateLanguage(selectedLanguage)

                // Check if user exists in Firebase
                val existingUser = repo.checkUserExists(firebaseUser.id)

                if (existingUser != null) {
                    // Existing user found
                    _user.value = existingUser
                    _loginState.value = LoginState.ExistingUser(existingUser)
                    DebugLogger.debugLog("UserViewModel", "Existing user logged in: ${existingUser.email}")
                } else {
                    // New user - prepare for registration
                    _user.value = firebaseUser.copy(language = selectedLanguage)
                    _loginState.value = LoginState.NewUser
                    DebugLogger.debugLog("UserViewModel", "New user detected: ${firebaseUser.email}")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e)
                DebugLogger.debugLog("UserViewModel", "Error during login: ${e.message}")
            }
        }
    }

    /**
     * Save existing user data locally and sync content
     * This is called when an existing user logs in
     */
    fun saveExistingUserLocally(context: Context, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
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

                onComplete(true)
            } catch (e: Exception) {
                DebugLogger.debugLog("UserViewModel", "Error saving user locally: ${e.message}")
                onComplete(false)
            }
        }
    }

    /**
     * Submit new user data to Firebase and save locally
     * This is called when a new user completes registration
     */
    fun submitNewUser(context: Context, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _userSaveState.value = UserSaveState.Saving
            try {
                val currentUser = _user.value

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
                    onComplete(true)
                } else {
                    _userSaveState.value = UserSaveState.Error(Exception("Failed to create user"))
                    onComplete(false)
                }
            } catch (e: Exception) {
                _userSaveState.value = UserSaveState.Error(e)
                DebugLogger.debugLog("UserViewModel", "Error submitting user: ${e.message}")
                onComplete(false)
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
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class ExistingUser(val user: User) : LoginState()
    object NewUser : LoginState()
    data class Error(val exception: Throwable) : LoginState()
}

sealed class UserSaveState {
    object Idle : UserSaveState()
    object Saving : UserSaveState()
    object Success : UserSaveState()
    data class Error(val exception: Throwable) : UserSaveState()
}