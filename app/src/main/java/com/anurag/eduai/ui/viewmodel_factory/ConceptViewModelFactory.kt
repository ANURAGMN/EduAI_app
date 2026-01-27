package com.anurag.eduai.ui.viewmodel_factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.repository.ChapterRepository
import com.anurag.eduai.repository.ConceptRepository
import com.anurag.eduai.repository.StudentLocalRepository
import com.anurag.eduai.repository.SubjectRepository
import com.anurag.eduai.ui.viewModel.ConceptViewModel

class ConceptViewModelFactory(
    private val conceptRepository: ConceptRepository,
    private val chapterRepository: ChapterRepository,
    private val subjectRepository: SubjectRepository,
    private val studentRepository: StudentLocalRepository,
    private val sharedPrefs: SharedPreferenceUtils
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConceptViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConceptViewModel(
                conceptRepository,
                chapterRepository,
                subjectRepository,
                studentRepository,
                sharedPrefs
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}