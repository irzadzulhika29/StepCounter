package com.example.trackingappcodex

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.trackingappcodex.ui.StepTrackerApp
import com.example.trackingappcodex.ui.theme.StepTrackerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: StepTrackerViewModel by viewModels {
        StepTrackerViewModel.Factory(application)
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.onPermissionGranted()
            } else {
                viewModel.onPermissionDenied()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handlePermissionIfNeeded()
        setContent {
            StepTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    var showPermissionRationale by remember { mutableStateOf(false) }
                    StepTrackerApp(
                        uiState = uiState,
                        onToggleTracking = { enabled ->
                            if (enabled) {
                                if (checkRecognitionPermission()) {
                                    viewModel.startTracking()
                                } else {
                                    showPermissionRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION)
                                    requestRecognitionPermission()
                                }
                            } else {
                                viewModel.stopTracking()
                            }
                        },
                        onResetSteps = viewModel::resetBaseline,
                        onDismissPermissionDialog = {
                            showPermissionRationale = false
                            viewModel.onPermissionDialogDismissed()
                        },
                        onRetryPermissionRequest = {
                            showPermissionRationale = false
                            requestRecognitionPermission()
                        },
                        showPermissionDialog = uiState.showPermissionDialog,
                        showPermissionRationale = showPermissionRationale
                    )
                }
            }
        }
    }

    private fun handlePermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !checkRecognitionPermission()) {
            requestRecognitionPermission()
        } else {
            viewModel.onPermissionGranted()
        }
    }

    private fun checkRecognitionPermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }
}
