package com.divya.fitnesscoach.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * The stateful entry point. This is the only place in the UI layer that knows a
 * [FitnessCoachViewModel] exists, collects its state in a lifecycle-aware way (so
 * collection stops when the screen isn't visible), and hands plain data down to the
 * pure [FitnessCoachScreen] below.
 */
@Composable
fun FitnessCoachRoute(viewModel: FitnessCoachViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FitnessCoachScreen(
        uiState = uiState,
        onRequestInsight = viewModel::requestInsight,
        modifier = modifier
    )
}

/**
 * Pure rendering, nothing else. This composable knows only the state it renders and
 * the event it emits, no ViewModel, no use case, no engine, no Gemini Nano. That's
 * what "the runtime is hidden from the UI" actually looks like in code, not just
 * in an architecture diagram.
 */
@Composable
fun FitnessCoachScreen(
    uiState: FitnessCoachUiState,
    onRequestInsight: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            when (uiState) {
                is FitnessCoachUiState.Idle -> {
                    Text("Tap below for today's insight.")
                }

                is FitnessCoachUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is FitnessCoachUiState.Insight -> {
                    Text(
                        text = uiState.text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is FitnessCoachUiState.Unavailable -> {
                    Text(
                        text = "On-device AI isn't available on this device right now. " +
                            "(Expected on an emulator, Gemini Nano needs real supported hardware.)",
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
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("Get today's insight")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FitnessCoachScreenPreview() {
    // No ViewModel, no engine, real or fake, needed to preview this screen anymore.
    FitnessCoachScreen(
        uiState = FitnessCoachUiState.Insight(
            "Shorter weekday workouts have been easier to sustain, worth trying " +
                "that again this week."
        ),
        onRequestInsight = {}
    )
}

@Preview(showBackground = true, name = "Unavailable state")
@Composable
private fun FitnessCoachScreenUnavailablePreview() {
    FitnessCoachScreen(
        uiState = FitnessCoachUiState.Unavailable,
        onRequestInsight = {}
    )
}
