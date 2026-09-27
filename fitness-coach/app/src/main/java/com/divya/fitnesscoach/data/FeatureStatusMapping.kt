package com.divya.fitnesscoach.data

import com.divya.fitnesscoach.domain.AiReadinessState
import com.divya.fitnesscoach.domain.ReadinessFailure
import com.google.mlkit.genai.common.FeatureStatus

/**
 * Translates the runtime's status into Fitness Coach's readiness state. AVAILABLE maps
 * to Ready here only in the sense of "assets are present"; [GeminiNanoAiReadinessManager]
 * still warms up before telling the UI it's Ready.
 *
 * DOWNLOADING carries no progress: the official docs don't describe attaching to a
 * download that's already in flight, so the app re-checks status instead.
 */
internal fun readinessStateFor(@FeatureStatus status: Int): AiReadinessState = when (status) {
    FeatureStatus.AVAILABLE -> AiReadinessState.Ready
    FeatureStatus.DOWNLOADABLE -> AiReadinessState.DownloadRequired
    FeatureStatus.DOWNLOADING -> AiReadinessState.Downloading(bytesDownloaded = 0L)
    FeatureStatus.UNAVAILABLE -> AiReadinessState.Unavailable
    else -> AiReadinessState.Failed(ReadinessFailure.Unknown)
}
