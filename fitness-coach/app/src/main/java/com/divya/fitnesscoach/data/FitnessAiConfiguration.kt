package com.divya.fitnesscoach.data

import com.google.mlkit.genai.prompt.GenerationConfig
import com.google.mlkit.genai.prompt.ModelPreference
import com.google.mlkit.genai.prompt.ModelReleaseStage
import com.google.mlkit.genai.prompt.generationConfig
import com.google.mlkit.genai.prompt.modelConfig

/**
 * The versioned pieces of the AI integration that Fitness Coach actually owns. Gemini
 * Nano itself is managed by AICore, so there's no model file version to point at; when
 * output behavior changes, this (plus app and ML Kit versions) is what you compare.
 *
 * Bump [promptVersion] when the prompt text in [GeminiNanoInsightEngine] changes,
 * [summarySchemaVersion] when the activity summary shape changes, and
 * [validationVersion] when the output acceptance rules change.
 */
data class FitnessAiConfiguration(
    val promptVersion: Int,
    val summarySchemaVersion: Int,
    val validationVersion: Int,
    @ModelReleaseStage val releaseStage: Int = ModelReleaseStage.STABLE,
    @ModelPreference val modelPreference: Int = ModelPreference.FULL
) {

    /**
     * Not every release stage / preference combination exists on every device, so the
     * client is created with this config and availability is still decided by checkStatus().
     */
    fun toGenerationConfig(): GenerationConfig {
        val stage = releaseStage
        val preference = modelPreference
        return generationConfig {
            modelConfig = modelConfig {
                releaseStage = stage
                this.preference = preference
            }
        }
    }

    companion object {
        /** STABLE is the default release stage and the one recommended for production. */
        val Current = FitnessAiConfiguration(
            promptVersion = 1,
            summarySchemaVersion = 1,
            validationVersion = 1
        )
    }
}
