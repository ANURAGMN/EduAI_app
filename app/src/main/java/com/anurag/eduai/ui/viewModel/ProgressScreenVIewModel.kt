package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.data.local.dao.DailyConceptCount
import com.anurag.eduai.data.local.dao.ProgressDao
import com.anurag.eduai.debug.DebugLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProgressScreenVIewModel(
    private val progressDao: ProgressDao
) : ViewModel() {

    // --- State holders ---
    private val _totalCompletedConcept = MutableStateFlow("0")
    val totalCompletedConcept: StateFlow<String> = _totalCompletedConcept

    private val _streakCount = MutableStateFlow("0")
    val streakCount: StateFlow<String> = _streakCount

    private val _sevenDayProgress = MutableStateFlow<List<DailyConceptCount>>(emptyList())
    val sevenDayProgress: StateFlow<List<DailyConceptCount>> = _sevenDayProgress

    fun getTotalCompletedConcept(userId: String) {
        viewModelScope.launch {
            val result = progressDao.getTotalCompletedConcepts(userId).toString()
            _totalCompletedConcept.value = result
        }
    }

    fun getSevenDayProgress(userId: String, sevenDaysAgoTimeStamp: Long){
        viewModelScope.launch {
            val result = progressDao.getConceptsClearedLast7Days(userId, sevenDaysAgoTimeStamp)
            _sevenDayProgress.value = result
            DebugLogger.debugLog(
                "ProgressScreenViewModel",
                "SevenDayProgress = $result"
            )
        }
    }

    fun getStreak(userId:String) {
        val result = "0" // later i will update this with logic till now logic is not ready
        _streakCount.value = result
    }


}
