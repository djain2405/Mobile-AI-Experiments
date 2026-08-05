package com.divya.fitnesscoach.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ActivitySummaryTest {

    @Test
    fun `trims and accepts a normal summary`() {
        val summary = ActivitySummary.createOrNull("  Workouts: Mon 30min  ")
        assertEquals("Workouts: Mon 30min", summary?.value)
    }

    @Test
    fun `rejects a blank summary`() {
        assertNull(ActivitySummary.createOrNull("   "))
    }

    @Test
    fun `rejects a summary over 1000 characters`() {
        assertNull(ActivitySummary.createOrNull("a".repeat(1_001)))
    }

    @Test
    fun `accepts a summary at exactly 1000 characters`() {
        val summary = ActivitySummary.createOrNull("a".repeat(1_000))
        assertEquals(1_000, summary?.value?.length)
    }
}
