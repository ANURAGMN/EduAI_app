package com.anurag.eduai.ui.viewModel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anurag.eduai.data.local.dao.ConceptDao
import com.anurag.eduai.data.local.dao.ProgressDao
import com.anurag.eduai.ui.viewModel.HomeViewModel

class HomeViewModelFactory(
    private val conceptDao: ConceptDao,
    private val progressDao: ProgressDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(conceptDao, progressDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
