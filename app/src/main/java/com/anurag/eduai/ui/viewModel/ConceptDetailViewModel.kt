package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.dao.ConceptDao
import com.anurag.eduai.data.local.entities.ConceptEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConceptDetailScreenState(
    val concept: ConceptEntity? = null,
    val conceptId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class ConceptDetailViewModel(
    private val conceptDao: ConceptDao
) : ViewModel() {

    private val _state = MutableStateFlow(ConceptDetailScreenState())
    val state: StateFlow<ConceptDetailScreenState> = _state.asStateFlow()

    fun loadConcept(conceptId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val concept = conceptDao.getConcept(conceptId)

                _state.value = _state.value.copy(
                    concept = concept,
                    conceptId = conceptId,
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
}