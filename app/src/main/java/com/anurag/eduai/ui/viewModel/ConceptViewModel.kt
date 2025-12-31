package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.dao.ChapterDao
import com.anurag.eduai.data.local.dao.ConceptDao
import com.anurag.eduai.data.local.entities.ChapterEntity
import com.anurag.eduai.data.local.entities.ConceptEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConceptScreenState(
    val concepts: List<ConceptEntity> = emptyList(),
    val chapter: ChapterEntity? = null,
    val chapterId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class ConceptViewModel(
    private val conceptDao: ConceptDao,
    private val chapterDao: ChapterDao
) : ViewModel() {

    private val _state = MutableStateFlow(ConceptScreenState())
    val state: StateFlow<ConceptScreenState> = _state.asStateFlow()

    fun loadConcepts(chapterId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val concepts = conceptDao.getConceptsForChapterSync(chapterId)
                val chapter = chapterDao.getChapter(chapterId)

                _state.value = _state.value.copy(
                    concepts = concepts,
                    chapter = chapter,
                    chapterId = chapterId,
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