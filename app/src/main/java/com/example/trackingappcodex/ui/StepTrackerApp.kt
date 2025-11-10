package com.example.trackingappcodex.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.trackingappcodex.R
import com.example.trackingappcodex.StepTrackerViewModel.StepTrackerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepTrackerApp(
    uiState: StepTrackerUiState,
    onToggleTracking: (Boolean) -> Unit,
    onResetSteps: () -> Unit,
    onDismissPermissionDialog: () -> Unit,
    onRetryPermissionRequest: () -> Unit,
    showPermissionDialog: Boolean,
    showPermissionRationale: Boolean,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(
                        onClick = onResetSteps,
                        enabled = uiState.currentSteps > 0
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsWalk,
                            contentDescription = stringResource(id = R.string.reset_steps)
                        )
                    }
                }
            )
        }
    ) { padding ->
        StepTrackerContent(
            uiState = uiState,
            onToggleTracking = onToggleTracking,
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
        )
    }

    if (showPermissionDialog) {
        PermissionRequestDialog(
            showRationale = showPermissionRationale,
            onDismiss = onDismissPermissionDialog,
            onConfirm = onRetryPermissionRequest
        )
    }
}

@Composable
private fun StepTrackerContent(
    uiState: StepTrackerUiState,
    onToggleTracking: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StepSummaryCard(
            steps = uiState.currentSteps,
            isTracking = uiState.isTracking,
            onToggleTracking = onToggleTracking,
            sensorAvailable = uiState.sensorAvailable,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        InsightsCard(
            steps = uiState.currentSteps,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun StepSummaryCard(
    steps: Int,
    isTracking: Boolean,
    onToggleTracking: (Boolean) -> Unit,
    sensorAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.DirectionsWalk,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .height(80.dp)
                    .semantics { contentDescription = stringResource(id = R.string.walking_icon) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$steps",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (sensorAvailable) {
                    if (isTracking) stringResource(id = R.string.status_tracking)
                    else stringResource(id = R.string.status_idle)
                } else {
                    stringResource(id = R.string.status_sensor_unavailable)
                },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onToggleTracking(!isTracking) },
                enabled = sensorAvailable
            ) {
                Text(
                    text = if (isTracking) stringResource(id = R.string.action_stop) else stringResource(
                        id = R.string.action_start
                    )
                )
            }
        }
    }
}

@Composable
private fun InsightsCard(
    steps: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.insights_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(id = R.string.insights_distance, stepsToKilometers(steps)),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(id = R.string.insights_calories, stepsToCalories(steps)),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PermissionRequestDialog(
    showRationale: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        title = { Text(text = stringResource(id = R.string.permission_title)) },
        text = {
            Text(
                text = if (showRationale) {
                    stringResource(id = R.string.permission_rationale)
                } else {
                    stringResource(id = R.string.permission_message)
                }
            )
        }
    )
}

private fun stepsToKilometers(steps: Int): String {
    val averageStepLengthMeters = 0.78f
    val kilometers = steps * averageStepLengthMeters / 1000f
    return String.format("%.2f", kilometers)
}

private fun stepsToCalories(steps: Int): String {
    val caloriesPerStep = 0.04f
    val calories = steps * caloriesPerStep
    return String.format("%.0f", calories)
}
