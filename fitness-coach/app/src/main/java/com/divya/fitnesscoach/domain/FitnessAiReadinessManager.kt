package com.divya.fitnesscoach.domain

import kotlinx.coroutines.flow.Flow

/**
 * Owns readiness, not inference. Answers "can on-device coaching run right now, and if
 * not, what is it waiting for?" Generating the insight stays with [FitnessInsightEngine].
 *
 * The runtime (AICore, for Gemini Nano) owns the model itself; this seam only asks for
 * availability, triggers a download when needed, warms the runtime up, and exposes the
 * result as [AiReadinessState]. Like the engine, nothing here names an SDK.
 */
interface FitnessAiReadinessManager {

    /** A single status check, with no side effects. Always ask the runtime; never trust a cached answer. */
    suspend fun checkReadiness(): AiReadinessState

    /**
     * Checks status and, when the model is already available, warms it up. Emits
     * [AiReadinessState.Checking] first, then either a terminal non-ready state
     * (download required, downloading, unavailable, failed) or
     * [AiReadinessState.WarmingUp] followed by [AiReadinessState.Ready].
     */
    fun prepare(): Flow<AiReadinessState>

    /**
     * Requests the one-time model download and reports progress. A completed download
     * still goes through [AiReadinessState.WarmingUp] before [AiReadinessState.Ready]:
     * downloaded does not mean loaded.
     */
    fun download(): Flow<AiReadinessState>

    /** Releases runtime resources. Safe to call more than once. */
    fun close()
}
