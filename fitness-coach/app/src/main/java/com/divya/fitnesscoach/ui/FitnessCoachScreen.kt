package com.divya.fitnesscoach.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.divya.fitnesscoach.domain.AiReadinessState
import com.divya.fitnesscoach.domain.EngineMode

/**
 * The stateful entry point. This is the only place in the UI layer that knows a
 * [FitnessCoachViewModel] exists, collects its state in a lifecycle-aware way, and
 * hands plain data down to the pure [FitnessCoachScreen] below.
 */
@Composable
fun FitnessCoachRoute(
    viewModel: FitnessCoachViewModel,
    engineMode: EngineMode,
    onEngineModeChange: (EngineMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val readiness by viewModel.readiness.collectAsStateWithLifecycle()

    FitnessCoachScreen(
        uiState = uiState,
        readiness = readiness,
        activitySummary = viewModel.activitySummary,
        engineMode = engineMode,
        onEngineModeChange = onEngineModeChange,
        onRequestInsight = viewModel::requestInsight,
        onDownload = viewModel::downloadModel,
        onRetryReadiness = viewModel::retryReadiness,
        modifier = modifier
    )
}

/**
 * Pure rendering. Knows only state and events — no ViewModel, use case, or Gemini Nano.
 */
@Composable
fun FitnessCoachScreen(
    uiState: FitnessCoachUiState,
    readiness: AiReadinessState,
    activitySummary: String,
    engineMode: EngineMode,
    onEngineModeChange: (EngineMode) -> Unit,
    onRequestInsight: () -> Unit,
    onDownload: () -> Unit,
    onRetryReadiness: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canRequestInsight = readiness.allowsInsightRequest()

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Fitness Coach",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "On-device insight, powered by Gemini Nano",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            EngineModeSelector(
                selected = engineMode,
                onSelected = onEngineModeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )

            Text(
                text = "Recent activity",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Text(
                text = activitySummary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            ReadinessSection(
                readiness = readiness,
                onDownload = onDownload,
                onRetry = onRetryReadiness,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            when (uiState) {
                is FitnessCoachUiState.Idle -> {
                    if (canRequestInsight) {
                        Text("Tap below for today's insight.")
                    }
                }

                is FitnessCoachUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is FitnessCoachUiState.Insight -> {
                    Text(
                        text = uiState.text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    ProcessedOnDeviceLabel()
                }

                is FitnessCoachUiState.Fallback -> {
                    Text(
                        text = "On-device coaching is not available on this device right now.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Rule-based suggestion",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = uiState.text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    ProcessedOnDeviceLabel()
                }

                is FitnessCoachUiState.Unavailable -> {
                    Text(
                        text = "On-device coaching is not available on this device right now.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                is FitnessCoachUiState.Error -> {
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = onRequestInsight,
                enabled = canRequestInsight,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("Generate insight")
            }
        }
    }
}

/**
 * Every readiness state gets a visible consequence: an explanation, progress, or an
 * action. Ready renders nothing; the enabled Generate button says it all.
 */
@Composable
private fun ReadinessSection(
    readiness: AiReadinessState,
    onDownload: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val message = readiness.toUserMessage() ?: return

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )

        when (readiness) {
            AiReadinessState.Checking,
            AiReadinessState.WarmingUp -> {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            AiReadinessState.DownloadRequired -> {
                OutlinedButton(
                    onClick = onDownload,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Download")
                }
            }

            is AiReadinessState.Downloading -> {
                val total = readiness.bytesToDownload
                if (total != null && total > 0) {
                    LinearProgressIndicator(
                        progress = { (readiness.bytesDownloaded.toFloat() / total).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                } else {
                    // Download already running in AICore; its progress isn't observable here.
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                    TextButton(onClick = onRetry) {
                        Text("Check again")
                    }
                }
            }

            is AiReadinessState.Failed -> {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Try again")
                }
            }

            AiReadinessState.Ready,
            AiReadinessState.Unavailable -> Unit
        }
    }
}

@Composable
private fun ProcessedOnDeviceLabel(modifier: Modifier = Modifier) {
    Text(
        text = "Processed on this device",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.tertiary,
        modifier = modifier.padding(top = 12.dp)
    )
}

@Composable
private fun EngineModeSelector(
    selected: EngineMode,
    onSelected: (EngineMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.selectableGroup()) {
        Text(
            text = "Engine (debug)",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        EngineMode.entries.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = mode == selected,
                        onClick = { onSelected(mode) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = mode == selected,
                    onClick = null
                )
                Text(
                    text = mode.toDebugLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

private fun EngineMode.toDebugLabel(): String = when (this) {
    EngineMode.Gemini -> "Gemini"
    EngineMode.Fake -> "Fake"
    EngineMode.FakeNeedsDownload -> "Fake (needs download)"
    EngineMode.Unavailable -> "Unavailable"
}

private val previewSummary =
    "Active days: 4 of the last 7\n" +
        "Average workout duration: 31 minutes\n" +
        "Most consistent range: 20–35 minutes\n" +
        "Rest days: 3\n" +
        "Pattern: longer planned workouts were skipped more often"

@Preview(showBackground = true)
@Composable
private fun FitnessCoachScreenPreview() {
    FitnessCoachScreen(
        uiState = FitnessCoachUiState.Insight(
            "Your shorter workouts have been easier to sustain. A 25-minute session " +
                "tomorrow may help you keep the rhythm going."
        ),
        readiness = AiReadinessState.Ready,
        activitySummary = previewSummary,
        engineMode = EngineMode.Fake,
        onEngineModeChange = {},
        onRequestInsight = {},
        onDownload = {},
        onRetryReadiness = {}
    )
}

@Preview(showBackground = true, name = "Unavailable + fallback")
@Composable
private fun FitnessCoachScreenFallbackPreview() {
    FitnessCoachScreen(
        uiState = FitnessCoachUiState.Fallback(
            "You completed four active days this week. A shorter session tomorrow " +
                "may help you stay consistent."
        ),
        readiness = AiReadinessState.Unavailable,
        activitySummary = previewSummary,
        engineMode = EngineMode.Unavailable,
        onEngineModeChange = {},
        onRequestInsight = {},
        onDownload = {},
        onRetryReadiness = {}
    )
}

@Preview(showBackground = true, name = "Download required")
@Composable
private fun FitnessCoachScreenDownloadRequiredPreview() {
    FitnessCoachScreen(
        uiState = FitnessCoachUiState.Idle,
        readiness = AiReadinessState.DownloadRequired,
        activitySummary = previewSummary,
        engineMode = EngineMode.FakeNeedsDownload,
        onEngineModeChange = {},
        onRequestInsight = {},
        onDownload = {},
        onRetryReadiness = {}
    )
}

@Preview(showBackground = true, name = "Downloading")
@Composable
private fun FitnessCoachScreenDownloadingPreview() {
    FitnessCoachScreen(
        uiState = FitnessCoachUiState.Idle,
        readiness = AiReadinessState.Downloading(
            bytesDownloaded = 800_000_000L,
            bytesToDownload = 2_000_000_000L
        ),
        activitySummary = previewSummary,
        engineMode = EngineMode.FakeNeedsDownload,
        onEngineModeChange = {},
        onRequestInsight = {},
        onDownload = {},
        onRetryReadiness = {}
    )
}
