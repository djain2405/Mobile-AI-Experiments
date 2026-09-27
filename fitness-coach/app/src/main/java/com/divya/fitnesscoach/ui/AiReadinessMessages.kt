package com.divya.fitnesscoach.ui

import com.divya.fitnesscoach.domain.AiReadinessState

/**
 * Turns a readiness state into copy a person understands. The implementation state
 * might be "download failed"; the experience shouldn't read like an exception log.
 * Returns null when there's nothing to say (Ready).
 */
fun AiReadinessState.toUserMessage(): String? = when (this) {
    AiReadinessState.Checking ->
        "Preparing on-device coaching…"
    AiReadinessState.DownloadRequired ->
        "On-device coaching needs a one-time download before it can run."
    is AiReadinessState.Downloading ->
        "Getting on-device coaching ready…"
    AiReadinessState.WarmingUp ->
        "Almost ready…"
    AiReadinessState.Ready ->
        null
    AiReadinessState.Unavailable ->
        "On-device coaching isn't available on this device right now. " +
            "You can still get a rule-based suggestion."
    is AiReadinessState.Failed ->
        "We couldn't finish setting up on-device coaching. Try again."
}
