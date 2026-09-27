package com.divya.fitnesscoach.domain

/**
 * Runtime engine selection for demos and day-to-day development. Persisted via
 * [com.divya.fitnesscoach.data.EngineModeStore] so the talk can switch paths
 * without rebuilding.
 */
enum class EngineMode {
    /** Real ML Kit GenAI Prompt API path. */
    Gemini,

    /** Deterministic success response (emulator / stage Fake beat). */
    Fake,

    /** Fake engine whose readiness starts at DownloadRequired, to show the full lifecycle anywhere. */
    FakeNeedsDownload,

    /** Always returns [FitnessInsightResult.Unavailable] so Demo Part 6 is forced. */
    Unavailable
}
