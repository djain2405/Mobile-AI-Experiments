package com.divya.fitnesscoach.domain

/**
 * The one entry point the ViewModel calls. Everything about *how* an insight gets
 * generated lives behind [engine]; this use case just orchestrates the call.
 *
 * Expected product conditions become state, not exceptions: an invalid summary
 * returns [FitnessInsightResult.InvalidInput] rather than throwing, so a coroutine
 * launched from the ViewModel can't silently fail and leave the UI stuck loading.
 * A real bug elsewhere is still free to throw, this only covers conditions the
 * caller should be able to plan for.
 */
class GenerateFitnessInsightUseCase(
    private val engine: FitnessInsightEngine
) {
    suspend operator fun invoke(rawActivitySummary: String): FitnessInsightResult {
        val summary = ActivitySummary.createOrNull(rawActivitySummary)
            ?: return FitnessInsightResult.InvalidInput

        return engine.generateInsight(summary)
    }
}
