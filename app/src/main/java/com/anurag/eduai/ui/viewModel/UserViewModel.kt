package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import com.anurag.eduai.data.firebase.User
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel(
    private val repo: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Loading)
    val loginState = _loginState.asStateFlow()

    private val _user = MutableStateFlow(User())
    val user = _user.asStateFlow()

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
     * Submit user data to Firebase
     */
    suspend fun submit(onResult: (Boolean) -> Unit) {
        val currentUser = _user.value
        val success = repo.createNewUser(currentUser)
        onResult(success)
    }

    /**
     * Handle Google login flow
     * Checks if user exists in Firebase and updates login state accordingly
     */
    suspend fun handleGoogleLogin(firebaseUser: User) {
        _loginState.value = LoginState.Loading
        try {
            val existing = repo.checkUserExists(firebaseUser.id)
            if (existing != null) {
                // Existing user - update local state with server data
                _user.value = existing
                _loginState.value = LoginState.Existing(existing)
                DebugLogger.debugLog("UserViewModel", "Existing user found: ${existing.email}")
            } else {
                // New user - keep the Firebase user data
                _user.value = firebaseUser
                _loginState.value = LoginState.New
                DebugLogger.debugLog("UserViewModel", "New user detected: ${firebaseUser.email}")
            }
        } catch (e: Exception) {
            _loginState.value = LoginState.Error(e)
            DebugLogger.debugLog("UserViewModel", "Error during login: ${e.message}")
        }
    }

    /**
     * Reset login state to Loading
     * Useful when navigating away from login screens
     */
    fun resetLoginState() {
        _loginState.value = LoginState.Loading
    }
}

sealed class LoginState {
    object Loading : LoginState()
    data class Existing(val user: User) : LoginState()
    object New : LoginState()
    data class Error(val e: Throwable) : LoginState()
}