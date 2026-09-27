package com.divya.fitnesscoach.data

import com.divya.fitnesscoach.domain.ActivitySummary
import com.divya.fitnesscoach.domain.FitnessInsightEngine
import com.divya.fitnesscoach.domain.FitnessInsightResult

/**
 * Forces [FitnessInsightResult.Unavailable] so Demo Part 6 (unavailable → fallback)
 * can be shown on any device without rebuilding.
 */
class UnavailableInsightEngine : FitnessInsightEngine {

    override suspend fun generateInsight(summary: ActivitySummary): FitnessInsightResult {
        return FitnessInsightResult.Unavailable
    }

    override fun close() {
        // No resources.
    }
}
