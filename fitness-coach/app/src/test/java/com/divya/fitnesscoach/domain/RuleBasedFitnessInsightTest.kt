package com.divya.fitnesscoach.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class RuleBasedFitnessInsightTest {

    @Test
    fun `uses four active days copy for the demo summary`() {
        val summary = ActivitySummary.createOrNull(
            "Active days: 4 of the last 7\n" +
                "Average workout duration: 31 minutes\n" +
                "Most consistent range: 20–35 minutes\n" +
                "Rest days: 3"
        )!!

        val insight = RuleBasedFitnessInsight.insightFor(summary)

        assertEquals(
            "You completed four active days this week. A shorter session tomorrow " +
                "may help you stay consistent.",
            insight
        )
    }

    @Test
    fun `uses shorter-workout coaching when summary mentions consistent range only`() {
        val summary = ActivitySummary.createOrNull(
            "Most consistent range: 20–35 minutes."
        )!!

        val insight = RuleBasedFitnessInsight.insightFor(summary)

        assertEquals(
            "Your shorter workouts have been easier to sustain. A 25-minute session " +
                "tomorrow may help you keep the rhythm going.",
            insight
        )
    }

    @Test
    fun `uses rest-day coaching when summary mentions rest days without other cues`() {
        val summary = ActivitySummary.createOrNull(
            "Workouts: Mon 40min. Rest days: Tue, Thu."
        )!!

        val insight = RuleBasedFitnessInsight.insightFor(summary)

        assertEquals(
            "You've already built rest into the week — protect one recovery day " +
                "and keep the next workout short.",
            insight
        )
    }

    @Test
    fun `uses default coaching when no heuristics match`() {
        val summary = ActivitySummary.createOrNull("Workouts: Mon 40min, Wed 45min.")!!

        val insight = RuleBasedFitnessInsight.insightFor(summary)

        assertEquals(
            "Keep the next session short and consistent — finishing matters more " +
                "than extending today's effort.",
            insight
        )
    }
}
