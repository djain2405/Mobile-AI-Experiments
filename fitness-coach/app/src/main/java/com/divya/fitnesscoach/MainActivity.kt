package com.divya.fitnesscoach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.divya.fitnesscoach.data.EngineModeStore
import com.divya.fitnesscoach.data.InsightEngineFactory
import com.divya.fitnesscoach.domain.EngineMode
import com.divya.fitnesscoach.ui.FitnessCoachRoute
import com.divya.fitnesscoach.ui.FitnessCoachViewModel
import com.divya.fitnesscoach.ui.FitnessCoachViewModelFactory

/**
 * Wires the [EngineMode]-selected engine and readiness manager into the ViewModel.
 * Switch Gemini / Fake / Fake (needs download) / Unavailable from the on-screen
 * debug selector without rebuilding; the choice is persisted and applied on recreate.
 *
 * The ViewModel is obtained through `by viewModels { }`, not constructed directly,
 * so it's a real, lifecycle-managed Android ViewModel and `onCleared()` is reliably
 * tied to this Activity's ViewModel store (including closing the runtime).
 */
class MainActivity : ComponentActivity() {

    private val engineModeStore by lazy { EngineModeStore(this) }

    private val viewModel by viewModels<FitnessCoachViewModel> {
        val components = InsightEngineFactory.create(engineModeStore.mode)
        FitnessCoachViewModelFactory(
            engine = components.engine,
            readinessManager = components.readinessManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                FitnessCoachRoute(
                    viewModel = viewModel,
                    engineMode = engineModeStore.mode,
                    onEngineModeChange = ::onEngineModeChange
                )
            }
        }
    }

    private fun onEngineModeChange(mode: EngineMode) {
        if (mode == engineModeStore.mode) return
        engineModeStore.mode = mode
        // Clear so onCleared() closes the old engine and recreate builds a new ViewModel.
        viewModelStore.clear()
        recreate()
    }
}
