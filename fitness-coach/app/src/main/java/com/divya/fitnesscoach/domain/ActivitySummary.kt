package com.divya.fitnesscoach.domain

/**
 * A bounded, already-validated activity summary. This is what makes "the input is
 * bounded and safe" an actual guarantee the type system enforces, rather than just
 * something written in a comment somewhere.
 *
 * Build one with [createOrNull], never call the constructor directly, that's what
 * keeps every caller from inventing its own idea of "bounded."
 */
@JvmInline
value class ActivitySummary private constructor(val value: String) {

    companion object {
        private const val MAX_LENGTH = 1_000

        /** Returns null if [raw] is blank or too long, rather than throwing. */
        fun createOrNull(raw: String): ActivitySummary? {
            val trimmed = raw.trim()
            if (trimmed.isEmpty() || trimmed.length > MAX_LENGTH) return null
            return ActivitySummary(trimmed)
        }
    }
}
