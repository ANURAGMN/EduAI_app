package com.anurag.eduai.ui.viewmodel_factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anurag.eduai.data.local.dao.ProgressDao
import com.anurag.eduai.data.local.dao.StudentDao
import com.anurag.eduai.data.local.dao.SubjectDao
import com.anurag.eduai.ui.viewModel.ProgressScreenViewModel
import com.anurag.eduai.utils.StreakManager

class ProgressViewModelFactory(
    private val progressDao: ProgressDao,
    private val subjectDao: SubjectDao,
    private val streakManager: StreakManager,
    private val studentDao: StudentDao,
    private val userId: String
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgressScreenViewModel::class.java)) {
            return ProgressScreenViewModel(progressDao,subjectDao,streakManager, studentDao, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}