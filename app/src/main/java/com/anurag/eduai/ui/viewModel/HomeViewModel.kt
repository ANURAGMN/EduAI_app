package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.dao.ProgressDao
import com.anurag.eduai.data.local.entities.ProgressEntity
import com.anurag.eduai.data.local.dao.ConceptDao
import com.anurag.eduai.data.local.entities.ConceptEntity
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.utils.StreakManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val conceptDao: ConceptDao,
    private val progressDao: ProgressDao,
    private val userId: String,
    private val streakManager: StreakManager
) : ViewModel(){
    
    // Pair of ProgressEntity and its corresponding ConceptEntity
    // Using a simple Map or List of Pairs for UI to consume
    var progressConcepts = MutableStateFlow<List<Pair<ProgressEntity?, ConceptEntity?>>>(emptyList())
    private val _streakCount = MutableStateFlow("0")
    val streakCount: StateFlow<String> = _streakCount

    init {
        viewModelScope.launch {

            progressDao.getHomeScreenConcepts(userId, "CONCEPT")
                .collect { progressList ->

                    // all completed (latest first)
                    val completedList =
                        progressDao.getAllProgressSync(userId, "CONCEPT")
                            .filter { it.status == "COMPLETED" }
                            .sortedByDescending { it.completedAt ?: 0L }

                    // all in-progress (most recent first)
                    val inProgressList =
                        progressList
                            .filter { it.status == "IN_PROGRESS" }
                            .sortedByDescending { it.lastAccessedAt }

                    // build final list: 1 completed + up to 3 in-progress
                    val curatedProgress = mutableListOf<ProgressEntity>()

                    completedList.firstOrNull()?.let { curatedProgress.add(it) }

                    curatedProgress.addAll(
                        inProgressList.take(3)
                    )

                    // if less than 4, fill remaining with more COMPLETED
                    if (curatedProgress.size < 4) {
                        val remaining =
                            completedList
                                .drop(1) // skip the first completed already added
                                .take(4 - curatedProgress.size)

                        curatedProgress.addAll(remaining)
                    }

                    // FIRST LOGIN FALLBACK: No progress at all
                    if (curatedProgress.isEmpty()) {

                        val firstUnitConcepts =
                            conceptDao.getFirstConceptsOfChapter("1", 4)

                        // Show concepts without progress entries
                        val combined = firstUnitConcepts.map { concept ->
                            null to concept
                        }

                        progressConcepts.value = combined
                        return@collect
                    }

                    // Normal path: fetch concepts for progress entries
                    val conceptIds = curatedProgress.map { it.itemId }

                    conceptDao.getConceptsByIds(conceptIds)
                        .collect { concepts ->

                            val combined = curatedProgress.map { progress ->
                                val concept = concepts.find { it.conceptId == progress.itemId }
                                progress to concept
                            }

                            progressConcepts.value = combined
                        }
                }
        }
    }


    fun getStreak() {
        val result = streakManager.getCurrentStreak()
        _streakCount.value = result.toString() ?: "0"
    }
}