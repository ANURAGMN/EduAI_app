package com.anurag.eduai.ui.viewmodel_factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anurag.eduai.repository.SimulationRepository
import com.anurag.eduai.ui.viewModel.SimulationViewModel

class SimulationViewModelFactory(
    private val simulationRepository: SimulationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimulationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SimulationViewModel(simulationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}