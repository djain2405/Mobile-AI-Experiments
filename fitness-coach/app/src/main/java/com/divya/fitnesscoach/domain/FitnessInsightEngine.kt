package com.divya.fitnesscoach.domain

/**
 * The single seam between the rest of the app and whatever is actually generating
 * the coaching insight underneath.
 *
 * Nothing above this interface, not the use case, not the ViewModel, not the UI,
 * is allowed to know whether it's talking to Gemini Nano, a fake, or something
 * that replaces both later. That is the whole point of this architecture milestone.
 *
 * Two implementations exist for this milestone:
 *  - [com.divya.fitnesscoach.data.GeminiNanoInsightEngine], the real ML Kit GenAI
 *    Prompt API implementation used by this sample.
 *  - [com.divya.fitnesscoach.data.FakeInsightEngine], used for previews, tests, and
 *    development on hardware that doesn't support Gemini Nano (including emulators).
 *
 * What this interface deliberately does NOT do yet: retries, cloud fallback, or
 * confidence scoring. That belongs in a later post on confidence and fallbacks. For now, [FitnessInsightResult.Unavailable]
 * and [FitnessInsightResult.Failed] are as far as failure handling goes.
 */
interface FitnessInsightEngine {

    /**
     * Generates one short coaching insight from an already-bounded [summary]. Callers
     * never pass raw, unvalidated text here, that's what [ActivitySummary.createOrNull]
     * is for, one layer up.
     */
    suspend fun generateInsight(summary: ActivitySummary): FitnessInsightResult

    /**
     * Releases any underlying client/model resources. Call this when the engine is
     * no longer needed, e.g. from a ViewModel's onCleared().
     */
    fun close()
}
