package com.divya.fitnesscoach.ui

import com.divya.fitnesscoach.domain.FitnessInsightFailure

/**
 * Turns a stable domain failure reason into copy a user can actually read. This is
 * the only place in the app that decides what failure text looks like, the domain
 * and data layers never construct user-facing strings themselves.
 */
fun FitnessInsightFailure.toUserMessage(): String = when (this) {
    FitnessInsightFailure.TemporarilyUnavailable ->
        "On-device AI is busy right now, try again in a moment."
    FitnessInsightFailure.DeviceUnsupported ->
        "On-device AI isn't supported on this device."
    FitnessInsightFailure.InsufficientStorage ->
        "Not enough storage available to run on-device AI right now."
    FitnessInsightFailure.GenerationFailed ->
        "Couldn't generate an insight this time, try again."
}
