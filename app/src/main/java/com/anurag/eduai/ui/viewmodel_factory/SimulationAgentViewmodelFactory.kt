package com.anurag.eduai.ui.viewmodel_factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anurag.eduai.data.remote.SimulationAgentAPI
import com.anurag.eduai.ui.viewModel.SimulationAgentViewModel

class SimulationAgentViewmodelFactory(
    private val api: SimulationAgentAPI = SimulationAgentAPI()

) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimulationAgentViewModel::class.java)) {
            return SimulationAgentViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}