package com.divya.fitnesscoach.data

import com.divya.fitnesscoach.domain.ActivitySummary
import com.divya.fitnesscoach.domain.FitnessInsightEngine
import com.divya.fitnesscoach.domain.FitnessInsightFailure
import com.divya.fitnesscoach.domain.FitnessInsightResult
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Generation
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
 * Kit's supported list). It will NOT run on an emulator, emulators aren't on the
 * supported device list. On one, [generateInsight] correctly returns
 * [FitnessInsightResult.Unavailable], which is exactly how the unsupported-device
 * state gets verified without needing real hardware. Use [FakeInsightEngine] for
 * previews, tests, and everyday development.
 *
 * Note on system instructions: ML Kit's dedicated `SystemInstruction` type currently
 * requires Gemini Nano V3+, so a device could support the Prompt API generally without
 * supporting that specific feature. To keep this sample working across more supported
 * devices, the coaching behavior is folded directly into the prompt text instead of
 * using `SystemInstruction`.
 *
 * This class deliberately does not retry, fall back to cloud, or apply confidence
 * scoring, that reliability layer belongs in a later post.
 */
class GeminiNanoInsightEngine : FitnessInsightEngine {

    private val model: GenerativeModel = Generation.getClient()

    private val instructionPreamble =
        "You are a supportive, non-diagnostic fitness coach. Given a short summary " +
            "of someone's recent activity, respond with exactly one short, encouraging " +
            "sentence noticing a pattern and suggesting a reasonable next step. Never " +
            "give medical, injury, or nutrition advice.\n\nActivity summary: "

    override suspend fun generateInsight(summary: ActivitySummary): FitnessInsightResult {
        return try {
            when (model.checkStatus()) {
                FeatureStatus.UNAVAILABLE -> FitnessInsightResult.Unavailable

                FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> {
                    // A later post on model lifecycle is where downloading gets its own state
                    // and UI. For this milestone, an in-progress or not-yet-started
                    // download is just reported as unavailable.
                    FitnessInsightResult.Unavailable
                }

                FeatureStatus.AVAILABLE -> {
                    val request = generateContentRequest(
                        TextPart(instructionPreamble + summary.value)
                    ) {
                        temperature = 0.4f
                    }
                    val response = model.generateContent(request)
                    val text = response.candidates.firstOrNull()?.text

                    if (text.isNullOrBlank()) {
                        FitnessInsightResult.Failed(FitnessInsightFailure.GenerationFailed)
                    } else {
                        FitnessInsightResult.Success(text.trim())
                    }
                }

                else -> FitnessInsightResult.Unavailable
            }
        } catch (e: GenAiException) {
            FitnessInsightResult.Failed(mapToFailureReason(e))
        }
    }

    /**
     * Maps SDK-specific error codes to a stable, UI-safe reason. Never forward
     * [GenAiException.getMessage] to the UI, log it privately for debugging instead.
     *
     * Note: verify these ErrorCode constants against the ML Kit version you're
     * building against, this mapping only needs to be broadly right for the
     * architecture to hold, the exact code list may shift across Beta releases.
     */
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
}
