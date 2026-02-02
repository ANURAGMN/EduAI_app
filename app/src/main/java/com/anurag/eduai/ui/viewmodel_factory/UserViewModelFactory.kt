package com.anurag.eduai.ui.viewmodel_factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anurag.eduai.repository.FirebaseRepository
import com.anurag.eduai.ui.viewModel.UserViewModel

class UserViewModelFactory(
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(firebaseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}