package com.divya.fitnesscoach.domain

/**
 * Deterministic, non-ML coaching used when the on-device model is unavailable.
 * Heuristics stay intentionally small so Demo Part 6 stays honest and predictable.
 */
object RuleBasedFitnessInsight {

    fun insightFor(summary: ActivitySummary): String {
        val text = summary.value.lowercase()
        return when {
            text.contains("active days: 4") || text.contains("4 of the last 7") ->
                "You completed four active days this week. A shorter session tomorrow " +
                    "may help you stay consistent."

            text.contains("20–35") || text.contains("20-35") ||
                text.contains("under 35") || text.contains("most consistent range") ->
                "Your shorter workouts have been easier to sustain. A 25-minute session " +
                    "tomorrow may help you keep the rhythm going."

            text.contains("rest day") || text.contains("rest days") ->
                "You've already built rest into the week — protect one recovery day " +
                    "and keep the next workout short."

            else ->
                "Keep the next session short and consistent — finishing matters more " +
                    "than extending today's effort."
        }
    }
}
