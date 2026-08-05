package com.divya.fitnesscoach.domain

/**
 * What comes back from asking for a coaching insight.
 *
 * Expected product conditions become state here (Unavailable, InvalidInput, Failed).
 * Unexpected programming errors, a real bug, still throw, this type is only for
 * outcomes the caller should be able to plan for.
 *
 * A real confidence/relevance/safety check on [Success] belongs in a later post on confidence and fallbacks, not here,
 * this is just enough shape for the UI to render something honest today.
 */
sealed interface FitnessInsightResult {

    /** A short, supportive coaching insight, ready to show as-is. */
    data class Success(val insight: String) : FitnessInsightResult

    /**
     * Gemini Nano isn't available on this device or hasn't finished downloading.
     * On an emulator, the real engine returns this, which is exactly how the
     * unsupported-device state gets verified without needing real hardware.
     */
    data object Unavailable : FitnessInsightResult

    /** The provided activity summary was blank or otherwise couldn't be validated. */
    data object InvalidInput : FitnessInsightResult

    /** Something went wrong generating the insight. [reason] is a stable, UI-safe category. */
    data class Failed(val reason: FitnessInsightFailure) : FitnessInsightResult
}
