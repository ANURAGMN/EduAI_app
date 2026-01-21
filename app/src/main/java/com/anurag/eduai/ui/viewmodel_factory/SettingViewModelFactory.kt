package com.anurag.eduai.ui.viewmodel_factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anurag.eduai.data.local.dao.StudentDao
import com.anurag.eduai.repository.FirebaseRepository
import com.anurag.eduai.ui.viewModel.HomeViewModel
import com.anurag.eduai.ui.viewModel.SettingViewModel

class SettingViewModelFactory(
    private val repository: FirebaseRepository,
    private val studentDao: StudentDao,
    private val userId: String,
    private val context: Context

) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
            return SettingViewModel(repository, studentDao, userId, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}