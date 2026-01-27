package com.anurag.eduai.ui.viewmodel_factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.repository.SubjectRepository
import com.anurag.eduai.ui.viewModel.SubjectViewModel

class SubjectViewModelFactory(
    private val repository: SubjectRepository,
    private val sharedPreferenceUtils: SharedPreferenceUtils
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectViewModel(repository, sharedPreferenceUtils) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}