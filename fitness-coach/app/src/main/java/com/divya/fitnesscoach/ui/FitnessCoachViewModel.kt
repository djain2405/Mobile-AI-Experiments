package com.divya.fitnesscoach.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divya.fitnesscoach.domain.AiReadinessState
import com.divya.fitnesscoach.domain.FitnessAiReadinessManager
import com.divya.fitnesscoach.domain.FitnessInsightResult
import com.divya.fitnesscoach.domain.GenerateFitnessInsightUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The only class that's allowed to know about [GenerateFitnessInsightUseCase] and
 * [FitnessAiReadinessManager]. Compose only ever sees [uiState] and [readiness], it has
 * no idea a use case, an engine interface, or Gemini Nano exist underneath.
 *
 * Readiness starts as soon as the ViewModel exists, i.e. when the user enters the
 * coaching screen, so warm-up can happen before they tap Generate. It runs in
 * [viewModelScope]: if the screen goes away, so does the work.
 *
 * Construct this through [FitnessCoachViewModelFactory], not directly, so it's
 * managed by Android's ViewModel APIs and its lifecycle guarantees (including
 * [onCleared]) actually hold.
 */
class FitnessCoachViewModel(
    private val generateInsight: GenerateFitnessInsightUseCase,
    private val readinessManager: FitnessAiReadinessManager,
    private val onClear: () -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow<FitnessCoachUiState>(FitnessCoachUiState.Idle)
    val uiState: StateFlow<FitnessCoachUiState> = _uiState.asStateFlow()

    private val _readiness = MutableStateFlow<AiReadinessState>(AiReadinessState.Checking)
    val readiness: StateFlow<AiReadinessState> = _readiness.asStateFlow()

    private var readinessJob: Job? = null

    /**
     * Bounded demo summary (stand-in for FitnessSummaryBuilder). Matches Demo Part 1.
     */
    val activitySummary: String =
        "Active days: 4 of the last 7\n" +
            "Average workout duration: 31 minutes\n" +
            "Most consistent range: 20–35 minutes\n" +
            "Rest days: 3\n" +
            "Pattern: longer planned workouts were skipped more often"

    init {
        observeReadiness(readinessManager.prepare())
    }

    fun downloadModel() {
        if (_readiness.value != AiReadinessState.DownloadRequired) return
        observeReadiness(readinessManager.download())
    }

    /** Re-asks the runtime. Used after a failure, or to re-check a download AICore is running. */
    fun retryReadiness() {
        observeReadiness(readinessManager.prepare())
    }

    fun requestInsight() {
        if (!_readiness.value.allowsInsightRequest()) return
        viewModelScope.launch {
            _uiState.value = FitnessCoachUiState.Loading
            _uiState.value = when (val result = generateInsight(activitySummary)) {
                is FitnessInsightResult.Success -> FitnessCoachUiState.Insight(result.insight)
                is FitnessInsightResult.Fallback -> FitnessCoachUiState.Fallback(result.insight)
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

    /** One readiness flow at a time, so repeated taps can't start parallel downloads. */
    private fun observeReadiness(states: Flow<AiReadinessState>) {
        readinessJob?.cancel()
        readinessJob = viewModelScope.launch {
            states.collect { _readiness.value = it }
        }
    }

    override fun onCleared() {
        readinessManager.close()
        onClear()
    }
}

/**
 * Generate is available once the runtime is ready, and also when it's unavailable,
 * because the use case then returns a rule-based fallback instead of an AI insight.
 */
fun AiReadinessState.allowsInsightRequest(): Boolean =
    this == AiReadinessState.Ready || this == AiReadinessState.Unavailable

/** UI-facing state. Compose renders this directly. */
sealed interface FitnessCoachUiState {
    data object Idle : FitnessCoachUiState
    data object Loading : FitnessCoachUiState
    data class Insight(val text: String) : FitnessCoachUiState
    /** Unavailable notice + rule-based suggestion (Demo Part 6). */
    data class Fallback(val text: String) : FitnessCoachUiState
    data object Unavailable : FitnessCoachUiState
    data class Error(val message: String) : FitnessCoachUiState
}
