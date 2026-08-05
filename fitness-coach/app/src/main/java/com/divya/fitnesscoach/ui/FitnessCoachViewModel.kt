package com.divya.fitnesscoach.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divya.fitnesscoach.domain.FitnessInsightResult
import com.divya.fitnesscoach.domain.GenerateFitnessInsightUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The only class that's allowed to know about [GenerateFitnessInsightUseCase]. Compose
 * only ever sees [uiState], it has no idea a use case, an engine interface, or Gemini
 * Nano exist underneath.
 *
 * This class does not hold a [com.divya.fitnesscoach.domain.FitnessInsightEngine]
 * directly, only the use case and an [onClear] callback. That keeps the ViewModel
 * from needing to know the engine even exists as a concept, closing it is the one
 * exception, and even that is just a callback it was handed.
 *
 * Construct this through [FitnessCoachViewModelFactory], not directly, so it's
 * managed by Android's ViewModel APIs and its lifecycle guarantees (including
 * [onCleared]) actually hold.
 */
class FitnessCoachViewModel(
    private val generateInsight: GenerateFitnessInsightUseCase,
    private val onClear: () -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow<FitnessCoachUiState>(FitnessCoachUiState.Idle)
    val uiState: StateFlow<FitnessCoachUiState> = _uiState.asStateFlow()

    /**
     * A stand-in for the real FitnessSummaryBuilder (later milestone). For this
     * milestone, a fixed, already-bounded summary is enough to exercise the whole
     * architecture honestly.
     */
    private val sampleActivitySummary =
        "Workouts this week: Mon 28min, Wed 32min, Fri 40min. Rest days: Tue, Thu, " +
            "Sat, Sun. Most consistent on days under 35 minutes."

    fun requestInsight() {
        viewModelScope.launch {
            _uiState.value = FitnessCoachUiState.Loading
            _uiState.value = when (val result = generateInsight(sampleActivitySummary)) {
                is FitnessInsightResult.Success -> FitnessCoachUiState.Insight(result.insight)
                is FitnessInsightResult.Unavailable -> FitnessCoachUiState.Unavailable
                is FitnessInsightResult.InvalidInput -> FitnessCoachUiState.Error(
                    "That activity summary couldn't be used, try again."
                )
                is FitnessInsightResult.Failed -> FitnessCoachUiState.Error(
                    result.reason.toUserMessage()
                )
            }
        }
    }

    override fun onCleared() {
        onClear()
    }
}

/** UI-facing state. Compose renders this directly, nothing richer than this is needed yet. */
sealed interface FitnessCoachUiState {
    data object Idle : FitnessCoachUiState
    data object Loading : FitnessCoachUiState
    data class Insight(val text: String) : FitnessCoachUiState
    data object Unavailable : FitnessCoachUiState
    data class Error(val message: String) : FitnessCoachUiState
}
