package com.divya.fitnesscoach.domain

/**
 * Stable, UI-safe reasons an insight generation attempt failed. Deliberately does not
 * carry the underlying SDK exception or its message, that text may be technical,
 * unstable across SDK versions, or just not something a user should see. Log the
 * original exception privately for debugging; map this enum to user-facing copy
 * in the UI layer.
 */
enum class FitnessInsightFailure {
    TemporarilyUnavailable,
    DeviceUnsupported,
    InsufficientStorage,
    GenerationFailed
}
