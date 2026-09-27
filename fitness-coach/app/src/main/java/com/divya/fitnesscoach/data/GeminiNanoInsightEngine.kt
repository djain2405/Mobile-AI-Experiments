package com.divya.fitnesscoach.data

import android.util.Log
import com.divya.fitnesscoach.domain.ActivitySummary
import com.divya.fitnesscoach.domain.FitnessInsightEngine
import com.divya.fitnesscoach.domain.FitnessInsightFailure
import com.divya.fitnesscoach.domain.FitnessInsightResult
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest

/**
 * The real ML Kit GenAI Prompt API (`com.google.mlkit:genai-prompt`) implementation
 * used by this sample. Worth noting plainly: the Prompt API is currently Beta, not
 * covered by an SLA or deprecation policy, so this is "the implementation this sample
 * uses today," not a claim that the API surface is frozen.
 *
 * Requires a Gemini Nano-supported device (e.g. Pixel 9/10 or another device on ML
 * Kit's supported list). It will NOT run on an emulator. On unsupported hardware,
 * [generateInsight] returns [FitnessInsightResult.Unavailable], which the use case
 * maps to rule-based [FitnessInsightResult.Fallback].
 *
 * Checking, downloading, and warming up the model belong to
 * [GeminiNanoAiReadinessManager], which shares this [model]. The status check below
 * is only a guard in case the runtime changed after readiness said Ready.
 *
 * Note on system instructions: ML Kit's dedicated `SystemInstruction` type currently
 * requires Gemini Nano V3+, so coaching behavior is folded into the prompt text.
 */
class GeminiNanoInsightEngine(
    private val model: GenerativeModel
) : FitnessInsightEngine {

    override suspend fun generateInsight(summary: ActivitySummary): FitnessInsightResult {
        return try {
            when (model.checkStatus()) {
                FeatureStatus.AVAILABLE -> generateFromSummary(summary)

                FeatureStatus.DOWNLOADABLE,
                FeatureStatus.DOWNLOADING,
                FeatureStatus.UNAVAILABLE -> FitnessInsightResult.Unavailable

                else -> FitnessInsightResult.Unavailable
            }
        } catch (e: GenAiException) {
            Log.w(TAG, "GenAI call failed: code=${e.errorCode}", e)
            FitnessInsightResult.Failed(mapToFailureReason(e))
        }
    }

    private suspend fun generateFromSummary(summary: ActivitySummary): FitnessInsightResult {
        val prompt = """
            You are a supportive fitness reflection assistant.

            Based only on the activity summary:
            - Return one concise observation
            - Suggest one realistic next step
            - Use no more than two sentences
            - Do not diagnose, prescribe, or make medical claims

            Activity summary:
            ${summary.value}
        """.trimIndent()

        val request = generateContentRequest(TextPart(prompt)) {
            temperature = 0.4f
        }
        val response = model.generateContent(request)
        val text = response.candidates.firstOrNull()?.text?.trim()

        return if (text.isNullOrBlank() || !isAcceptableInsight(text)) {
            // Product refuses to treat blank / overlong output as a real result.
            // Use case will not turn Failed into Fallback — blank is a generation miss.
            FitnessInsightResult.Failed(FitnessInsightFailure.GenerationFailed)
        } else {
            FitnessInsightResult.Success(text)
        }
    }

    /** Tiny gate so generated text is not automatically a product result. */
    private fun isAcceptableInsight(text: String): Boolean {
        return text.length in 20..500
    }

    private fun mapToFailureReason(e: GenAiException): FitnessInsightFailure {
        return when (e.errorCode) {
            GenAiException.ErrorCode.BUSY ->
                FitnessInsightFailure.TemporarilyUnavailable
            GenAiException.ErrorCode.NOT_AVAILABLE,
            GenAiException.ErrorCode.AICORE_INCOMPATIBLE ->
                FitnessInsightFailure.DeviceUnsupported
            GenAiException.ErrorCode.NOT_ENOUGH_DISK_SPACE ->
                FitnessInsightFailure.InsufficientStorage
            else ->
                FitnessInsightFailure.GenerationFailed
        }
    }

    override fun close() {
        model.close()
    }

    companion object {
        private const val TAG = "GeminiNanoEngine"
    }
}
