package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.dao.StudentDao
import com.anurag.eduai.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UpdateProfileState {
    object Idle : UpdateProfileState()
    object Loading : UpdateProfileState()
    object Success : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}

class UpdateUserViewModel(
    private val repository: FirebaseRepository,
    private val studentDao: StudentDao,
    private val userId: String
) : ViewModel() {

    private val _updateState =
        MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateState = _updateState.asStateFlow()

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
                _updateState.value =
                    UpdateProfileState.Error("User not found")
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
            } else {
                studentDao.updateStudent(updatedStudent)
                _updateState.value =
                    UpdateProfileState.Error("Failed to sync with server")
            }
        }
    }

    fun resetState() {
        _updateState.value = UpdateProfileState.Idle
    }

    // update local DB with newly picked profile picture
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
        }
    }

}