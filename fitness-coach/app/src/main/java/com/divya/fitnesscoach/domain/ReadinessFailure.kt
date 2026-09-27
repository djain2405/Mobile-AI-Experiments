package com.divya.fitnesscoach.domain

/**
 * Which lifecycle step failed while getting on-device coaching ready. Like
 * [FitnessInsightFailure], this never carries SDK exception text; the UI layer maps it
 * to copy a person can act on.
 */
enum class ReadinessFailure {
    StatusCheckFailed,
    DownloadFailed,
    WarmupFailed,
    Unknown
}
