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
    fun updateClass(stdClass: String) {
        _user.value = _user.value.copy(studentClass = stdClass)
    }

    suspend fun submit(onResult: (Boolean) -> Unit) {
        val currentUser = _user.value
        val success = repo.createNewUser(currentUser)
        onResult(success)
    }


    suspend fun handleGoogleLogin(firebaseUser: User): Boolean {
        return try {
            repo.checkUserExists(firebaseUser.id)
        } catch (e: Exception) {
            DebugLogger.errorLog("GoogleSignIn", "Error\n $e")
            false
        }
    }


}