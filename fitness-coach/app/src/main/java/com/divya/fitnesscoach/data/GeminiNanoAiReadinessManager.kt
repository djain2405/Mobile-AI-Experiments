package com.divya.fitnesscoach.data

import android.util.Log
import com.divya.fitnesscoach.domain.AiReadinessState
import com.divya.fitnesscoach.domain.FitnessAiReadinessManager
import com.divya.fitnesscoach.domain.ReadinessFailure
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.GenerativeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Readiness for Gemini Nano through the ML Kit GenAI Prompt API. Shares its
 * [GenerativeModel] with [GeminiNanoInsightEngine], so the client that was checked,
 * downloaded, and warmed up is the same one that runs inference.
 *
 * All of this is ordinary coroutine work scoped to the screen that collects it. AICore
 * owns the actual model download, so there's no WorkManager job layered on top.
 */
class GeminiNanoAiReadinessManager(
    private val model: GenerativeModel,
    private val configuration: FitnessAiConfiguration
) : FitnessAiReadinessManager {

    override suspend fun checkReadiness(): AiReadinessState {
        return try {
            readinessStateFor(model.checkStatus())
        } catch (e: GenAiException) {
            Log.w(TAG, "checkStatus failed: code=${e.errorCode}", e)
            AiReadinessState.Failed(ReadinessFailure.StatusCheckFailed)
        }
    }

    override fun prepare(): Flow<AiReadinessState> = flow {
        emit(AiReadinessState.Checking)
        when (val state = checkReadiness()) {
            AiReadinessState.Ready -> emitAll(warmUp())
            else -> emit(state)
        }
    }

    override fun download(): Flow<AiReadinessState> = flow {
        var bytesToDownload: Long? = null
        var completed = false
        var failed = false

        model.download()
            .catch { e ->
                if (e !is GenAiException) throw e
                Log.w(TAG, "download failed: code=${e.errorCode}", e)
                failed = true
            }
            .collect { status ->
                when (status) {
                    is DownloadStatus.DownloadStarted -> {
                        bytesToDownload = status.bytesToDownload
                        emit(AiReadinessState.Downloading(0L, bytesToDownload))
                    }
                    is DownloadStatus.DownloadProgress ->
                        emit(AiReadinessState.Downloading(status.totalBytesDownloaded, bytesToDownload))
                    is DownloadStatus.DownloadCompleted ->
                        completed = true
                    is DownloadStatus.DownloadFailed -> {
                        Log.w(TAG, "download failed", status.e)
                        failed = true
                    }
                }
            }

        when {
            failed -> emit(AiReadinessState.Failed(ReadinessFailure.DownloadFailed))
            completed -> emitAll(warmUp())
            else -> emit(checkReadiness())
        }
    }

    private fun warmUp(): Flow<AiReadinessState> = flow {
        emit(AiReadinessState.WarmingUp)
        val result = try {
            model.warmup()
            logConfiguration()
            AiReadinessState.Ready
        } catch (e: GenAiException) {
            Log.w(TAG, "warmup failed: code=${e.errorCode}", e)
            AiReadinessState.Failed(ReadinessFailure.WarmupFailed)
        }
        emit(result)
    }

    private suspend fun logConfiguration() {
        val baseModel = try {
            model.getBaseModelName()
        } catch (e: GenAiException) {
            "unknown"
        }
        Log.i(TAG, "Ready: baseModel=$baseModel config=$configuration")
    }

    override fun close() {
        model.close()
    }

    companion object {
        private const val TAG = "GeminiNanoReadiness"
    }
}
