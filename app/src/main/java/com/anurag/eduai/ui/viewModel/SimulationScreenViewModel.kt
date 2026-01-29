package com.anurag.eduai.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anurag.eduai.repository.SimulationRepository
import com.anurag.eduai.ui.models.SimulationUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SimulationScreenState(
    val isLoading: Boolean = false,
    val chapterId: String = "",
    val simulations: List<SimulationUiModel> = emptyList(),
    val error: String? = null
)

class SimulationViewModel(
    private val simulationRepository: SimulationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SimulationScreenState())
    val state: StateFlow<SimulationScreenState> = _state.asStateFlow()

    /**
     * Load simulations for a specific chapter
     */
    fun loadSimulations(chapterId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
                chapterId = chapterId
            )
            try {
                val simulations = simulationRepository.getSimulationsForChapter(chapterId)
                _state.value = _state.value.copy(
                    simulations = simulations,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}