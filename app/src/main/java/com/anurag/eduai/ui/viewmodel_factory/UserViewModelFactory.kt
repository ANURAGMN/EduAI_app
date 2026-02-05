package com.anurag.eduai.ui.viewmodel_factory

import android.content.Context
import androidx.compose.runtime.CompositionContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anurag.eduai.repository.FirebaseRepository
import com.anurag.eduai.ui.viewModel.UserViewModel

class UserViewModelFactory() : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}