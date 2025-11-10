package com.example.trackingappcodex

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StepTrackerViewModel(
    application: Application,
    private val sensorManager: SensorManager? =
        application.getSystemService(SensorManager::class.java)
) : AndroidViewModel(application), SensorEventListener {

    private val _uiState = MutableStateFlow(StepTrackerUiState())
    val uiState: StateFlow<StepTrackerUiState> = _uiState.asStateFlow()

    private var stepSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private var baselineStepCount: Float? = null

    init {
        ensureSensorAvailable()
    }

    fun startTracking() {
        if (!ensureSensorAvailable()) {
            return
        }
        if (_uiState.value.isTracking) return
        val manager = sensorManager ?: return
        stepSensor?.also {
            manager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            _uiState.value = _uiState.value.copy(isTracking = true)
        }
    }

    fun stopTracking() {
        if (_uiState.value.isTracking) {
            sensorManager?.unregisterListener(this)
            _uiState.value = _uiState.value.copy(isTracking = false)
        }
    }

    fun resetBaseline() {
        baselineStepCount = null
        _uiState.value = _uiState.value.copy(currentSteps = 0)
    }

    fun onPermissionGranted() {
        _uiState.value = _uiState.value.copy(showPermissionDialog = false)
        ensureSensorAvailable()
    }

    fun onPermissionDenied() {
        _uiState.value = _uiState.value.copy(showPermissionDialog = true)
    }

    fun onPermissionDialogDismissed() {
        _uiState.value = _uiState.value.copy(showPermissionDialog = false)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val totalSteps = event.values.firstOrNull() ?: return
        if (baselineStepCount == null) {
            baselineStepCount = totalSteps
        }
        val currentSteps = (totalSteps - (baselineStepCount ?: totalSteps)).toInt().coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(currentSteps = currentSteps)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
    }

    private fun ensureSensorAvailable(): Boolean {
        val available = sensorManager != null && stepSensor != null
        if (!available) {
            sensorManager?.unregisterListener(this)
        }
        _uiState.value = _uiState.value.copy(
            sensorAvailable = available,
            isTracking = if (available) _uiState.value.isTracking else false
        )
        return available
    }

    data class StepTrackerUiState(
        val currentSteps: Int = 0,
        val isTracking: Boolean = false,
        val sensorAvailable: Boolean = true,
        val showPermissionDialog: Boolean = false
    )

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StepTrackerViewModel::class.java)) {
                return StepTrackerViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
