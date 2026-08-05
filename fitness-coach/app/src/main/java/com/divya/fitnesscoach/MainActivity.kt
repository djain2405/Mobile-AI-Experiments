package com.divya.fitnesscoach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.divya.fitnesscoach.data.GeminiNanoInsightEngine
import com.divya.fitnesscoach.ui.FitnessCoachRoute
import com.divya.fitnesscoach.ui.FitnessCoachViewModel
import com.divya.fitnesscoach.ui.FitnessCoachViewModelFactory

/**
 * Wires up the real [GeminiNanoInsightEngine] by default. On unsupported hardware
 * (including any emulator) the engine itself reports FitnessInsightResult.Unavailable
 * rather than crashing, so the app is safe to run everywhere even before you have a
 * Gemini Nano-supported device in hand.
 *
 * The ViewModel is obtained through `by viewModels { }`, not constructed directly,
 * so it's a real, lifecycle-managed Android ViewModel and `onCleared()` is reliably
 * tied to this Activity's ViewModel store.
 *
 * To develop against a guaranteed-working response instead, swap the engine for
 * `FakeInsightEngine()` from com.divya.fitnesscoach.data.
 */
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<FitnessCoachViewModel> {
        FitnessCoachViewModelFactory(engine = GeminiNanoInsightEngine())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                FitnessCoachRoute(viewModel = viewModel)
            }
        }
    }
}
