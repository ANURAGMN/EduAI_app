package com.anurag.eduai.ui.viewmodel_factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.repository.ConceptRepository
import com.anurag.eduai.ui.viewModel.ConceptDetailViewModel

class ConceptDetailViewModelFactory(
    private val repository: ConceptRepository,
    private val sharedPrefs: SharedPreferenceUtils
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConceptDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConceptDetailViewModel(repository, sharedPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}