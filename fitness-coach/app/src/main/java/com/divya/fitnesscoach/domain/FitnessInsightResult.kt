package com.divya.fitnesscoach.domain

/**
 * What comes back from asking for a coaching insight.
 *
 * Expected product conditions become state here (Unavailable, InvalidInput, Failed,
 * Fallback). Unexpected programming errors, a real bug, still throw, this type is
 * only for outcomes the caller should be able to plan for.
 *
 * Engines may still return [Unavailable]; [GenerateFitnessInsightUseCase] maps that
 * into [Fallback] so the product still offers rule-based coaching.
 */
sealed interface FitnessInsightResult {

    /** A short, supportive coaching insight from the on-device model, ready to show as-is. */
    data class Success(val insight: String) : FitnessInsightResult

    /**
     * Deterministic rule-based coaching used when the model is unavailable.
     * Produced by the use case, not by engines. The UI shows the unavailable notice
     * and this suggestion together (Demo Part 6).
     */
    data class Fallback(val insight: String) : FitnessInsightResult

    /**
     * Gemini Nano isn't available on this device or hasn't finished downloading.
     * Engines return this; the use case converts it to [Fallback] for the UI.
     */
    data object Unavailable : FitnessInsightResult

    /** The provided activity summary was blank or otherwise couldn't be validated. */
    data object InvalidInput : FitnessInsightResult

    /** Something went wrong generating the insight. [reason] is a stable, UI-safe category. */
    data class Failed(val reason: FitnessInsightFailure) : FitnessInsightResult
}
