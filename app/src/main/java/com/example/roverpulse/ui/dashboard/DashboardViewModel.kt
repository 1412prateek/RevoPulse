package com.example.roverpulse.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roverpulse.controller.MockRoverController
import com.example.roverpulse.controller.RoverController
import com.example.roverpulse.model.CameraStreamState
import com.example.roverpulse.model.ConnectionStatus
import com.example.roverpulse.model.DriveDirection
import com.example.roverpulse.model.OperatingMode
import com.example.roverpulse.model.RoverConnectionConfig
import com.example.roverpulse.model.RoverTelemetry
import com.example.roverpulse.model.SteerDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected,
    val operatingMode: OperatingMode = OperatingMode.SIMULATION_DEMO,
    val telemetry: RoverTelemetry? = null,
    val activeDriveDirection: DriveDirection = DriveDirection.STOP,
    val activeSteerDirection: SteerDirection = SteerDirection.CENTER,
    val cameraStreamState: CameraStreamState = CameraStreamState.Disconnected,
    val connectionConfig: RoverConnectionConfig = RoverConnectionConfig(),
    val isSettingsDialogOpen: Boolean = false,
    val isCameraFullscreen: Boolean = false,
    val isEmergencyStopped: Boolean = false,
    val driveSpeedPercent: Int = 100
)

class DashboardViewModel(
    private val controller: RoverController = MockRoverController()
) : ViewModel() {

    private val _isSettingsDialogOpen = MutableStateFlow(false)
    private val _isCameraFullscreen = MutableStateFlow(false)
    private val _driveSpeedPercent = MutableStateFlow(100)
    private val _isEmergencyStopped = MutableStateFlow(false)

    val uiState: StateFlow<DashboardUiState> = combine(
        controller.connectionStatus,
        controller.operatingMode,
        controller.telemetry,
        controller.activeDriveDirection,
        controller.activeSteerDirection,
        controller.cameraStreamState,
        controller.connectionConfig,
        _isSettingsDialogOpen,
        _isCameraFullscreen,
        _isEmergencyStopped,
        _driveSpeedPercent
    ) { params ->
        val conn = params[0] as ConnectionStatus
        val mode = params[1] as OperatingMode
        val telem = params[2] as? RoverTelemetry
        val drive = params[3] as DriveDirection
        val steer = params[4] as SteerDirection
        val cam = params[5] as CameraStreamState
        val cfg = params[6] as RoverConnectionConfig
        val dialog = params[7] as Boolean
        val fullscreen = params[8] as Boolean
        val eStop = params[9] as Boolean
        val speed = params[10] as Int

        DashboardUiState(
            connectionStatus = conn,
            operatingMode = mode,
            telemetry = telem,
            activeDriveDirection = drive,
            activeSteerDirection = steer,
            cameraStreamState = cam,
            connectionConfig = cfg,
            isSettingsDialogOpen = dialog,
            isCameraFullscreen = fullscreen,
            isEmergencyStopped = eStop || (telem?.isEmergencyStopped == true),
            driveSpeedPercent = speed
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun onConnectClicked() {
        controller.connect(uiState.value.connectionConfig)
    }

    fun onDisconnectClicked() {
        controller.disconnect()
    }

    fun onToggleOperatingMode() {
        val nextMode = if (uiState.value.operatingMode == OperatingMode.SIMULATION_DEMO) {
            OperatingMode.REAL_HARDWARE
        } else {
            OperatingMode.SIMULATION_DEMO
        }
        controller.setOperatingMode(nextMode)
    }

    fun onDriveButtonPressed(direction: DriveDirection) {
        if (_isEmergencyStopped.value) return
        controller.sendDriveCommand(direction, _driveSpeedPercent.value)
    }

    fun onDriveButtonReleased() {
        // Crucial safety requirement: Momentary release always commands STOP
        controller.sendDriveCommand(DriveDirection.STOP, 0)
    }

    fun onSteerButtonPressed(direction: SteerDirection) {
        if (_isEmergencyStopped.value) return
        controller.sendSteerCommand(direction)
    }

    fun onSteerButtonReleased() {
        // DC steering spring/motor safety: Return to center on release
        controller.sendSteerCommand(SteerDirection.CENTER)
    }

    fun onEmergencyStop() {
        _isEmergencyStopped.value = true
        controller.emergencyStop()
    }

    fun onResetEmergencyStop() {
        _isEmergencyStopped.value = false
        (controller as? MockRoverController)?.resetEmergencyStop()
    }

    fun onToggleCameraFullscreen() {
        _isCameraFullscreen.value = !_isCameraFullscreen.value
    }

    fun onOpenSettings() {
        _isSettingsDialogOpen.value = true
    }

    fun onCloseSettings() {
        _isSettingsDialogOpen.value = false
    }

    fun onSaveSettings(updatedConfig: RoverConnectionConfig) {
        controller.updateConfig(updatedConfig)
        _isSettingsDialogOpen.value = false
    }

    fun onSpeedChanged(newSpeed: Int) {
        _driveSpeedPercent.value = newSpeed.coerceIn(0, 100)
    }

    override fun onCleared() {
        super.onCleared()
        // Fail-safe: Always stop and disconnect on lifecycle teardown
        controller.sendDriveCommand(DriveDirection.STOP, 0)
        controller.sendSteerCommand(SteerDirection.CENTER)
        controller.disconnect()
    }
}
