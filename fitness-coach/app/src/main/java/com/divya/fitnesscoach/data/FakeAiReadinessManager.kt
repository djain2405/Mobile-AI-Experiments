package com.divya.fitnesscoach.data

import com.divya.fitnesscoach.domain.AiReadinessState
import com.divya.fitnesscoach.domain.FitnessAiReadinessManager
import com.divya.fitnesscoach.domain.ReadinessFailure
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A deterministic stand-in for [GeminiNanoAiReadinessManager], so the whole readiness
 * lifecycle (check, download, warm-up, ready) can be seen on an emulator and exercised
 * in unit tests.
 *
 * [currentStatus] is what a status check reports. It's mutable so tests can simulate
 * the runtime changing underneath the app, e.g. a failed check that succeeds on retry.
 */
class FakeAiReadinessManager(
    initialStatus: AiReadinessState = AiReadinessState.Ready,
    private val downloadSucceeds: Boolean = true,
    private val downloadSizeBytes: Long = 2_000_000_000L,
    private val stepDelayMillis: Long = 300L
) : FitnessAiReadinessManager {

    var currentStatus: AiReadinessState = initialStatus

    override suspend fun checkReadiness(): AiReadinessState = currentStatus

    override fun prepare(): Flow<AiReadinessState> = flow {
        emit(AiReadinessState.Checking)
        delay(stepDelayMillis)
        val state = checkReadiness()
        if (state == AiReadinessState.Ready) {
            emit(AiReadinessState.WarmingUp)
            delay(stepDelayMillis)
        }
        emit(state)
    }

    override fun download(): Flow<AiReadinessState> = flow {
        emit(AiReadinessState.Downloading(0L, downloadSizeBytes))
        if (!downloadSucceeds) {
            delay(stepDelayMillis)
            emit(AiReadinessState.Failed(ReadinessFailure.DownloadFailed))
            return@flow
        }
        for (step in 1..DOWNLOAD_STEPS) {
            delay(stepDelayMillis)
            emit(AiReadinessState.Downloading(downloadSizeBytes * step / DOWNLOAD_STEPS, downloadSizeBytes))
        }
        currentStatus = AiReadinessState.Ready
        emit(AiReadinessState.WarmingUp)
        delay(stepDelayMillis)
        emit(AiReadinessState.Ready)
    }

    override fun close() {
        // No real resources to release.
    }

    private companion object {
        const val DOWNLOAD_STEPS = 5
    }
}
