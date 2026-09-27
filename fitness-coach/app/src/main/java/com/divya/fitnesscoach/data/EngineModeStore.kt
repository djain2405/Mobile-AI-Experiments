package com.divya.fitnesscoach.data

import android.content.Context
import com.divya.fitnesscoach.domain.EngineMode

/**
 * Persists [EngineMode] in SharedPreferences so the selected engine survives
 * process death and Activity recreate during a live demo.
 */
class EngineModeStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var mode: EngineMode
        get() {
            val stored = prefs.getString(KEY_MODE, EngineMode.Gemini.name) ?: EngineMode.Gemini.name
            return when (stored) {
                // Migrate older demo builds that used AlwaysUnavailable.
                "AlwaysUnavailable" -> EngineMode.Unavailable
                else -> runCatching { EngineMode.valueOf(stored) }.getOrDefault(EngineMode.Gemini)
            }
        }
        set(value) {
            prefs.edit().putString(KEY_MODE, value.name).apply()
        }

    companion object {
        private const val PREFS_NAME = "fitness_coach_engine_mode"
        private const val KEY_MODE = "engine_mode"
    }
}
