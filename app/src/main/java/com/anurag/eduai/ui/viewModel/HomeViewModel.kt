package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.dao.ProgressDao
import com.anurag.eduai.data.local.entities.ProgressEntity
import com.anurag.eduai.data.local.dao.ConceptDao
import com.anurag.eduai.data.local.entities.ConceptEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val conceptDao: ConceptDao,
    private val progressDao: ProgressDao,
) : ViewModel(){
    
    // Pair of ProgressEntity and its corresponding ConceptEntity
    // Using a simple Map or List of Pairs for UI to consume
    var progressConcepts = MutableStateFlow<List<Pair<ProgressEntity, ConceptEntity?>>>(emptyList())

    init {
        viewModelScope.launch {
            progressDao.getHomeScreenConcepts("userId", "CONCEPT")
                .collect { progressList ->
                    val conceptIds = progressList.map { it.itemId }
                    
                    // We need to fetch concepts. Since we are inside a collect, ideally we should use flatMapLatest or combine
                    // But for simplicity in this Flow setup, we can collect inside or just use a suspend function if Dao supports it.
                    // However, Dao getConceptsByIds returns Flow.
                    // Let's use a simpler approach: observe progress, then for those IDs, subscribe to concepts.
                    
                    // Actually, a better approach with Flows:
                    // But to keep it simple and consistent with the codebase's likely pattern:
                    // I'll assume we can just fetch distinct concepts for now or observe them.
                    
                    if (conceptIds.isNotEmpty()) {
                         // fetching just once for the latest set to avoid complex nesting loop logic for now
                         // Or better, let's just use the flow effectively.
                         
                         conceptDao.getConceptsByIds(conceptIds).collect { concepts ->
                             // Map progress to their concept
                             val combinedList = progressList.map { progress ->
                                 val concept = concepts.find { it.conceptId == progress.itemId }
                                 progress to concept
                             }
                             progressConcepts.value = combinedList
                         }
                    } else {
                        progressConcepts.value = emptyList()
                    }
                }
        }
    }
}