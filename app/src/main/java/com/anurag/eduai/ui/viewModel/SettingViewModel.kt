package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.dao.StudentDao
import com.anurag.eduai.data.local.entities.StudentEntity
import com.anurag.eduai.repository.FirebaseRepository
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
    private val repository: FirebaseRepository,
    private val studentDao: StudentDao,
    private val userId: String
) : ViewModel() {

    private val _student = MutableStateFlow<StudentEntity?>(null)
    val student: StateFlow<StudentEntity?> = _student

    private val _updateState =
        MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateState = _updateState.asStateFlow()

    init {
        viewModelScope.launch {
            getStudent()
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
    fun getStudent(){
        viewModelScope.launch {
            val result = studentDao.getStudentSync(userId)
            _student.value = result
        }
    }

}