package com.divya.fitnesscoach.data

import com.divya.fitnesscoach.domain.ActivitySummary
import com.divya.fitnesscoach.domain.FitnessInsightEngine
import com.divya.fitnesscoach.domain.FitnessInsightResult
import kotlinx.coroutines.delay

/**
 * A deterministic stand-in for [GeminiNanoInsightEngine], used for Compose previews,
 * unit tests, and day-to-day development on an emulator (where Gemini Nano can't run
 * at all, it needs real supported hardware).
 *
 * This isn't a throwaway. Since on-device generative AI needs specific hardware,
 * this fake is what makes the rest of the app buildable and testable without one.
 */
class FakeInsightEngine(
    private val response: FitnessInsightResult = defaultSuccess,
    private val simulatedDelayMillis: Long = 400L
) : FitnessInsightEngine {

    override suspend fun generateInsight(summary: ActivitySummary): FitnessInsightResult {
        delay(simulatedDelayMillis) // mimics real inference latency for realistic previews
        return response
    }

    override fun close() {
        // No real resources to release.
    }

    companion object {
        val defaultSuccess = FitnessInsightResult.Success(
            "You've been most consistent on days when your workouts stayed under " +
                "35 minutes, a shorter session tomorrow may help you keep the streak."
        )
    }
}
