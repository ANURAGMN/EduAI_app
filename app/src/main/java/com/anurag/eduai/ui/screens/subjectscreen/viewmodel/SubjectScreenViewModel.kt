package com.anurag.eduai.ui.screens.subjectscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.repository.SubjectRepository
import com.anurag.eduai.ui.screens.subjectscreen.dataclass.SubjectScreenState
import com.anurag.eduai.ui.models.SubjectUiModel
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.utils.getLocalizedName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val repository: SubjectRepository,
    private val sharedPreferenceUtils: SharedPreferenceUtils
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
                // Hardcode to class 7 to ensure syllabus is independent of user's profile class level
                val subjectEntities = repository.getSubjectsForClass(7)

                val subjectUiModels = subjectEntities.map { entity ->
                    SubjectUiModel(
                        id = entity.subjectId,
                        name = entity.getLocalizedName(),
                        color = BrandPrimary,
                        totalChapters = entity.totalChapters,
                        iconUrl = entity.iconUrl
                    )
                }

                _state.value = _state.value.copy(
                    subjects = subjectUiModels,
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

    fun onSubjectSelected(subjectId: String) {
        sharedPreferenceUtils.setSubjectSelection(subjectId)
    }
}