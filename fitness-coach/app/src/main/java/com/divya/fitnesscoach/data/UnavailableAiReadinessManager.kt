package com.divya.fitnesscoach.data

import com.divya.fitnesscoach.domain.AiReadinessState
import com.divya.fitnesscoach.domain.FitnessAiReadinessManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Always reports [AiReadinessState.Unavailable], pairing with [UnavailableInsightEngine]. */
class UnavailableAiReadinessManager : FitnessAiReadinessManager {

    override suspend fun checkReadiness(): AiReadinessState = AiReadinessState.Unavailable

    override fun prepare(): Flow<AiReadinessState> =
        flowOf(AiReadinessState.Checking, AiReadinessState.Unavailable)

    override fun download(): Flow<AiReadinessState> = flowOf(AiReadinessState.Unavailable)

    override fun close() {
        // No resources.
    }
}
