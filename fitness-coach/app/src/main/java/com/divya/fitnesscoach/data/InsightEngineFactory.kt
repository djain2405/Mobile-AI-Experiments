package com.divya.fitnesscoach.data

import com.divya.fitnesscoach.domain.AiReadinessState
import com.divya.fitnesscoach.domain.EngineMode
import com.divya.fitnesscoach.domain.FitnessAiReadinessManager
import com.divya.fitnesscoach.domain.FitnessInsightEngine
import com.google.mlkit.genai.prompt.Generation

/** Inference and readiness for one runtime. Both halves must describe the same runtime. */
data class FitnessAiComponents(
    val engine: FitnessInsightEngine,
    val readinessManager: FitnessAiReadinessManager
)

/** Builds the [FitnessAiComponents] for the selected [EngineMode]. */
object InsightEngineFactory {

    fun create(
        mode: EngineMode,
        configuration: FitnessAiConfiguration = FitnessAiConfiguration.Current
    ): FitnessAiComponents = when (mode) {
        EngineMode.Gemini -> {
            // One client, so the model that was checked and warmed up is the one that runs inference.
            val model = Generation.getClient(configuration.toGenerationConfig())
            FitnessAiComponents(
                engine = GeminiNanoInsightEngine(model),
                readinessManager = GeminiNanoAiReadinessManager(model, configuration)
            )
        }
        EngineMode.Fake -> FitnessAiComponents(
            engine = FakeInsightEngine(),
            readinessManager = FakeAiReadinessManager()
        )
        EngineMode.FakeNeedsDownload -> FitnessAiComponents(
            engine = FakeInsightEngine(),
            readinessManager = FakeAiReadinessManager(initialStatus = AiReadinessState.DownloadRequired)
        )
        EngineMode.Unavailable -> FitnessAiComponents(
            engine = UnavailableInsightEngine(),
            readinessManager = UnavailableAiReadinessManager()
        )
    }
}
