package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.dao.SubjectDao
import com.anurag.eduai.data.local.entities.SubjectEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubjectScreenState(
    val subjects: List<SubjectEntity> = emptyList(),
    val classLevel: Int = 7,
    val isLoading: Boolean = false,
    val error: String? = null
)

class SubjectViewModel(
    private val subjectDao: SubjectDao
) : ViewModel() {

    private val _state = MutableStateFlow(SubjectScreenState())
    val state: StateFlow<SubjectScreenState> = _state.asStateFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val subjects = subjectDao.getSubjectsForClassSync(_state.value.classLevel)
                _state.value = _state.value.copy(
                    subjects = subjects,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun setClassLevel(classLevel: Int) {
        _state.value = _state.value.copy(classLevel = classLevel)
        loadSubjects()
    }
}