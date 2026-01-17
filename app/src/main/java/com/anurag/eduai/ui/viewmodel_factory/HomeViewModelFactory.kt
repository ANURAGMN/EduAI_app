package com.anurag.eduai.ui.viewmodel_factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anurag.eduai.data.local.dao.ConceptDao
import com.anurag.eduai.data.local.dao.ProgressDao
import com.anurag.eduai.data.local.dao.StudentDao
import com.anurag.eduai.ui.viewModel.HomeViewModel
import com.anurag.eduai.utils.StreakManager

class HomeViewModelFactory(
    private val conceptDao: ConceptDao,
    private val progressDao: ProgressDao,
    private val studentDao: StudentDao,
    private val userId: String,
    private val streakManager: StreakManager
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(conceptDao, progressDao,studentDao, userId, streakManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }