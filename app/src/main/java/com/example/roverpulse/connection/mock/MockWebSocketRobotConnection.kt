package com.example.roverpulse.connection.mock

import com.example.roverpulse.connection.RobotConnection
import com.example.roverpulse.connection.RobotConnectionState
import com.example.roverpulse.connection.protocol.DriveJsonCommand
import com.example.roverpulse.connection.protocol.EmergencyStopJsonCommand
import com.example.roverpulse.connection.protocol.PingJsonMessage
import com.example.roverpulse.connection.protocol.PongJsonMessage
import com.example.roverpulse.connection.protocol.SteerJsonCommand
import com.example.roverpulse.connection.protocol.StopJsonCommand
import com.example.roverpulse.connection.protocol.TelemetryJsonMessage
import com.example.roverpulse.model.DriveDirection
import com.example.roverpulse.model.RoverTelemetry
import com.example.roverpulse.model.SteerDirection
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

/**
 * High-fidelity in-process simulator adapter for [RobotConnection].
 * Performs real JSON serialization and deserialization using Moshi, mimicking
 * the exact ESP32 WebSocket server state machine and command timeout watchdog.
 */
class MockWebSocketRobotConnection(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : RobotConnection {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val driveAdapter = moshi.adapter(DriveJsonCommand::class.java)
    private val steerAdapter = moshi.adapter(SteerJsonCommand::class.java)
    private val stopAdapter = moshi.adapter(StopJsonCommand::class.java)
    private val eStopAdapter = moshi.adapter(EmergencyStopJsonCommand::class.java)
    private val pingAdapter = moshi.adapter(PingJsonMessage::class.java)
    private val telemetryAdapter = moshi.adapter(TelemetryJsonMessage::class.java)
    private val pongAdapter = moshi.adapter(PongJsonMessage::class.java)

    private val _connectionState = MutableStateFlow<RobotConnectionState>(RobotConnectionState.Disconnected)
    override val connectionState: StateFlow<RobotConnectionState> = _connectionState.asStateFlow()

    private val _telemetry = MutableStateFlow<RoverTelemetry?>(null)
    override val telemetry: StateFlow<RoverTelemetry?> = _telemetry.asStateFlow()

    private val _diagnosticsLog = MutableSharedFlow<String>(extraBufferCapacity = 50)
    override val diagnosticsLog: SharedFlow<String> = _diagnosticsLog.asSharedFlow()

    private val sequenceCounter = AtomicLong(1)
    private var telemetryJob: Job? = null
    private var watchdogJob: Job? = null

    // Simulated ESP32 hardware state
    private var simulatedDriveDir = DriveDirection.STOP
    private var simulatedSteerDir = SteerDirection.CENTER
    private var simulatedSpeed = 0
    private var isEmergencyStopActive = false
    private var lastCommandTimestamp = System.currentTimeMillis()
    private val watchdogTimeoutMs = 300L

    override fun connect(targetUrl: String) {
        scope.launch {
            _connectionState.value = RobotConnectionState.Connecting(targetUrl)
            _diagnosticsLog.tryEmit("[WS-SIM] Handshake initiating with $targetUrl...")
            delay(400) // Simulated network handshake latency

            _connectionState.value = RobotConnectionState.Connected(
                endpoint = targetUrl,
                pingMs = 12,
                connectedAt = System.currentTimeMillis()
            )
            _diagnosticsLog.tryEmit("[WS-SIM] WebSocket connection established (101 Switching Protocols)")
            _diagnosticsLog.tryEmit("[WS-SIM] ESP32 firmware: RoverPulse-TB6612-v1.0 (SIM)")

            startTelemetryBroadcast()
            startWatchdogMonitor()
        }
    }

    override fun disconnect(reason: String) {
        telemetryJob?.cancel()
        watchdogJob?.cancel()
        simulatedDriveDir = DriveDirection.STOP
        simulatedSteerDir = SteerDirection.CENTER
        _connectionState.value = RobotConnectionState.Disconnected
        _telemetry.value = null
        _diagnosticsLog.tryEmit("[WS-SIM] WebSocket disconnected: $reason")
    }

    override fun sendDriveCommand(direction: DriveDirection, speedPercent: Int) {
        if (isEmergencyStopActive) return
        val seq = sequenceCounter.getAndIncrement()
        val command = DriveJsonCommand(
            direction = direction.label,
            speed = speedPercent,
            sequenceNumber = seq
        )
        val jsonPayload = driveAdapter.toJson(command)
        _diagnosticsLog.tryEmit("[TX-JSON] $jsonPayload")

        // ESP32 state update simulation
        lastCommandTimestamp = System.currentTimeMillis()
        simulatedDriveDir = direction
        simulatedSpeed = if (direction == DriveDirection.STOP) 0 else speedPercent
    }

    override fun sendSteerCommand(direction: SteerDirection) {
        if (isEmergencyStopActive) return
        val seq = sequenceCounter.getAndIncrement()
        val command = SteerJsonCommand(
            direction = direction.label,
            sequenceNumber = seq
        )
        val jsonPayload = steerAdapter.toJson(command)
        _diagnosticsLog.tryEmit("[TX-JSON] $jsonPayload")

        lastCommandTimestamp = System.currentTimeMillis()
        simulatedSteerDir = direction
    }

    override fun sendStopCommand() {
        val seq = sequenceCounter.getAndIncrement()
        val command = StopJsonCommand(sequenceNumber = seq)
        val jsonPayload = stopAdapter.toJson(command)
        _diagnosticsLog.tryEmit("[TX-JSON] $jsonPayload")

        lastCommandTimestamp = System.currentTimeMillis()
        simulatedDriveDir = DriveDirection.STOP
        simulatedSpeed = 0
    }

    override fun sendEmergencyStop(active: Boolean) {
        val seq = sequenceCounter.getAndIncrement()
        val command = EmergencyStopJsonCommand(
            active = active,
            sequenceNumber = seq
        )
        val jsonPayload = eStopAdapter.toJson(command)
        _diagnosticsLog.tryEmit("[TX-JSON] $jsonPayload")

        isEmergencyStopActive = active
        if (active) {
            simulatedDriveDir = DriveDirection.STOP
            simulatedSteerDir = SteerDirection.CENTER
            simulatedSpeed = 0
            _diagnosticsLog.tryEmit("[WS-SIM] TB6612FNG standby pin pulled LOW. Motors killed.")
        } else {
            _diagnosticsLog.tryEmit("[WS-SIM] Emergency stop reset. Motors armed.")
        }
    }

    override fun sendPing() {
        val seq = sequenceCounter.getAndIncrement()
        val now = System.currentTimeMillis()
        val ping = PingJsonMessage(sequenceNumber = seq, timestamp = now)
        val jsonPayload = pingAdapter.toJson(ping)

        scope.launch {
            delay(12) // Roundtrip simulated latency
            val pong = PongJsonMessage(echoTimestamp = now, firmwareUptimeMs = 120000)
            val pongJson = pongAdapter.toJson(pong)
            _diagnosticsLog.tryEmit("[RX-JSON] $pongJson")
        }
    }

    private fun startTelemetryBroadcast() {
        telemetryJob?.cancel()
        telemetryJob = scope.launch {
            var batteryVoltage = 5.25f
            while (isActive) {
                // Slight simulated voltage sag when drive motor runs
                val loadDrop = if (simulatedDriveDir != DriveDirection.STOP) 0.18f else 0.0f
                val currentVoltage = (batteryVoltage - loadDrop).coerceIn(4.2f, 6.0f)
                val batteryPct = (((currentVoltage - 4.4f) / 1.6f) * 100).toInt().coerceIn(0, 100)

                val telemetryMsg = TelemetryJsonMessage(
                    batteryVoltage = currentVoltage,
                    batteryPercent = batteryPct,
                    wifiRssi = -52,
                    driveMotorPwm = if (simulatedDriveDir == DriveDirection.STOP) 0 else (simulatedSpeed * 2.55f).toInt(),
                    steerActive = simulatedSteerDir != SteerDirection.CENTER,
                    failsafeActive = isEmergencyStopActive,
                    firmwareVersion = "v1.0-SIM",
                    uptimeMs = System.currentTimeMillis()
                )

                val serialized = telemetryAdapter.toJson(telemetryMsg)
                val parsed = telemetryAdapter.fromJson(serialized)

                parsed?.let {
                    _telemetry.value = RoverTelemetry(
                        batteryVoltage = it.batteryVoltage,
                        batteryPercentage = it.batteryPercent,
                        wifiSignalDbm = it.wifiRssi,
                        driveMotorPwm = it.driveMotorPwm,
                        steeringActive = it.steerActive,
                        isEmergencyStopped = it.failsafeActive,
                        firmwareVersion = it.firmwareVersion,
                        commandLatencyMs = 12,
                        lastHeartbeatTimestamp = System.currentTimeMillis(),
                        isSimulated = true
                    )
                }

                delay(200) // 5Hz telemetry stream
            }
        }
    }

    /**
     * ESP32 firmware watchdog emulation:
     * If Android stops sending keepalive/movement commands for > 300ms, the firmware
     * automatically halts all motors to prevent a runaway vehicle.
     */
    private fun startWatchdogMonitor() {
        watchdogJob?.cancel()
        watchdogJob = scope.launch {
            while (isActive) {
                val elapsed = System.currentTimeMillis() - lastCommandTimestamp
                if (elapsed > watchdogTimeoutMs && simulatedDriveDir != DriveDirection.STOP) {
                    _diagnosticsLog.tryEmit("[WATCHDOG] No command for ${elapsed}ms > ${watchdogTimeoutMs}ms. Auto-braking drive motor!")
                    simulatedDriveDir = DriveDirection.STOP
                    simulatedSpeed = 0
                }
                delay(100)
            }
        }
    }
}
