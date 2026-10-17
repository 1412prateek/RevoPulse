package com.example.roverpulse.controller

import com.example.roverpulse.connection.RobotConnection
import com.example.roverpulse.connection.RobotConnectionState
import com.example.roverpulse.connection.mock.MockWebSocketRobotConnection
import com.example.roverpulse.model.CameraStreamState
import com.example.roverpulse.model.ConnectionStatus
import com.example.roverpulse.model.DriveDirection
import com.example.roverpulse.model.OperatingMode
import com.example.roverpulse.model.RoverConnectionConfig
import com.example.roverpulse.model.RoverTelemetry
import com.example.roverpulse.model.SteerDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Safe Simulation & Demo implementation of [RoverController].
 * Uses [MockWebSocketRobotConnection] under the hood to ensure all commands
 * and telemetry are validated through the exact JSON protocol schema.
 */
class MockRoverController(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val robotConnection: RobotConnection = MockWebSocketRobotConnection(scope)
) : RoverController {

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    override val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _operatingMode = MutableStateFlow(OperatingMode.SIMULATION_DEMO)
    override val operatingMode: StateFlow<OperatingMode> = _operatingMode.asStateFlow()

    private val _telemetry = MutableStateFlow<RoverTelemetry?>(null)
    override val telemetry: StateFlow<RoverTelemetry?> = _telemetry.asStateFlow()

    private val _activeDriveDirection = MutableStateFlow(DriveDirection.STOP)
    override val activeDriveDirection: StateFlow<DriveDirection> = _activeDriveDirection.asStateFlow()

    private val _activeSteerDirection = MutableStateFlow(SteerDirection.CENTER)
    override val activeSteerDirection: StateFlow<SteerDirection> = _activeSteerDirection.asStateFlow()

    private val _cameraStreamState = MutableStateFlow<CameraStreamState>(CameraStreamState.Disconnected)
    override val cameraStreamState: StateFlow<CameraStreamState> = _cameraStreamState.asStateFlow()

    private val _connectionConfig = MutableStateFlow(RoverConnectionConfig())
    override val connectionConfig: StateFlow<RoverConnectionConfig> = _connectionConfig.asStateFlow()

    private var connectionStateJob: Job? = null
    private var telemetryCollectionJob: Job? = null
    private var isEStopped: Boolean = false

    init {
        // Collect telemetry from the underlying JSON robot connection
        telemetryCollectionJob = scope.launch {
            robotConnection.telemetry.collect { telem ->
                if (_operatingMode.value == OperatingMode.SIMULATION_DEMO && _connectionStatus.value is ConnectionStatus.Connected) {
                    _telemetry.value = telem
                } else if (_operatingMode.value == OperatingMode.REAL_HARDWARE) {
                    // Strictly null if real hardware is not reporting telemetry
                    _telemetry.value = telem?.takeIf { !it.isSimulated }
                }
            }
        }

        // Collect state from robot connection
        connectionStateJob = scope.launch {
            robotConnection.connectionState.collect { state ->
                if (_operatingMode.value == OperatingMode.SIMULATION_DEMO) {
                    when (state) {
                        is RobotConnectionState.Connected -> {
                            _connectionStatus.value = ConnectionStatus.Connected(
                                host = state.endpoint,
                                pingMs = state.pingMs,
                                connectedAtMs = state.connectedAt
                            )
                        }
                        is RobotConnectionState.Connecting -> {
                            _connectionStatus.value = ConnectionStatus.Connecting(state.targetUrl)
                        }
                        is RobotConnectionState.Failed -> {
                            _connectionStatus.value = ConnectionStatus.Error(state.error, state.canRetry)
                        }
                        is RobotConnectionState.Reconnecting -> {
                            _connectionStatus.value = ConnectionStatus.Connecting("Reconnecting #${state.attempt}...")
                        }
                        is RobotConnectionState.Disconnected -> {
                            _connectionStatus.value = ConnectionStatus.Disconnected
                        }
                    }
                }
            }
        }
    }

    override fun connect(config: RoverConnectionConfig) {
        _connectionConfig.value = config
        scope.launch {
            if (_operatingMode.value == OperatingMode.SIMULATION_DEMO) {
                val wsUrl = "ws://${config.ipAddress}:${config.port}/ws (SIMULATOR)"
                robotConnection.connect(wsUrl)
                _cameraStreamState.value = CameraStreamState.Connecting("Virtual ESP32-CAM Test Pattern")
                delay(400)
                _cameraStreamState.value = CameraStreamState.Streaming("demo://test-pattern", fps = 24)
            } else {
                // Real Hardware mode requires explicit transport client
                _connectionStatus.value = ConnectionStatus.Error(
                    message = "Hardware protocol configured for WebSocket JSON. Connect physical ESP32 or configure Wokwi bridge.",
                    canRetry = true
                )
                _telemetry.value = null
                _cameraStreamState.value = CameraStreamState.Error(
                    "Camera stream endpoint not verified. Real ESP32-CAM stream URL pending configuration."
                )
            }
        }
    }

    override fun disconnect() {
        robotConnection.disconnect()
        _activeDriveDirection.value = DriveDirection.STOP
        _activeSteerDirection.value = SteerDirection.CENTER
        _connectionStatus.value = ConnectionStatus.Disconnected
        _cameraStreamState.value = CameraStreamState.Disconnected
        _telemetry.value = null
    }

    override fun sendDriveCommand(direction: DriveDirection, speedPercent: Int) {
        if (isEStopped) return
        _activeDriveDirection.value = direction
        robotConnection.sendDriveCommand(direction, speedPercent)
    }

    override fun sendSteerCommand(direction: SteerDirection) {
        if (isEStopped) return
        _activeSteerDirection.value = direction
        robotConnection.sendSteerCommand(direction)
    }

    override fun emergencyStop() {
        isEStopped = true
        _activeDriveDirection.value = DriveDirection.STOP
        _activeSteerDirection.value = SteerDirection.CENTER
        robotConnection.sendEmergencyStop(true)
    }

    fun resetEmergencyStop() {
        isEStopped = false
        robotConnection.sendEmergencyStop(false)
    }

    override fun setOperatingMode(mode: OperatingMode) {
        if (_operatingMode.value != mode) {
            disconnect()
            _operatingMode.value = mode
        }
    }

    override fun updateConfig(config: RoverConnectionConfig) {
        _connectionConfig.value = config
    }
}
