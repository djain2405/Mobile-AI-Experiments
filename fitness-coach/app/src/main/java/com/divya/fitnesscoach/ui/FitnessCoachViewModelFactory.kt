package com.divya.fitnesscoach.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.divya.fitnesscoach.domain.FitnessInsightEngine
import com.divya.fitnesscoach.domain.GenerateFitnessInsightUseCase

/**
 * Builds [FitnessCoachViewModel] with a real [FitnessInsightEngine] wired in. For a
 * teaching sample this manual factory is enough, a DI framework like Hilt could
 * replace it later without changing anything above the ViewModel.
 *
 * Using a factory (rather than `FitnessCoachViewModel(engine)` called directly in
 * an Activity) is what makes this a real, lifecycle-managed Android ViewModel, with
 * `onCleared()` reliably tied to the ViewModel store instead of hoped for.
 */
class FitnessCoachViewModelFactory(
    private val engine: FitnessInsightEngine
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FitnessCoachViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FitnessCoachViewModel(
                generateInsight = GenerateFitnessInsightUseCase(engine),
                onClear = engine::close
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
