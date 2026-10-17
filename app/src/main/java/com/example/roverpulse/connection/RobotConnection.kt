package com.example.roverpulse.connection

import com.example.roverpulse.model.DriveDirection
import com.example.roverpulse.model.RoverTelemetry
import com.example.roverpulse.model.SteerDirection
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Connection states for the transport layer.
 */
sealed interface RobotConnectionState {
    object Disconnected : RobotConnectionState
    data class Connecting(val targetUrl: String) : RobotConnectionState
    data class Connected(
        val endpoint: String,
        val pingMs: Long,
        val connectedAt: Long
    ) : RobotConnectionState
    data class Reconnecting(val attempt: Int, val nextRetryMs: Long) : RobotConnectionState
    data class Failed(val error: String, val canRetry: Boolean) : RobotConnectionState
}

/**
 * Transport-independent abstraction for robot hardware communication.
 * The UI layer binds only to this interface, decoupling Jetpack Compose and ViewModels
 * from whether the underlying transport is a WebSocket client, UDP socket, or mock simulator.
 */
interface RobotConnection {
    /** Observable connection state */
    val connectionState: StateFlow<RobotConnectionState>

    /** Real-time telemetry from connected robot (null when disconnected or unverified) */
    val telemetry: StateFlow<RoverTelemetry?>

    /** Rolling stream of hardware logs and diagnostics */
    val diagnosticsLog: SharedFlow<String>

    /** Initiates connection to the given endpoint (e.g. "ws://192.168.4.1:8080/ws") */
    fun connect(targetUrl: String)

    /** Closes connection and issues immediate safe stop */
    fun disconnect(reason: String = "User requested disconnect")

    /** Momentary drive command: Forward / Reverse / Stop */
    fun sendDriveCommand(direction: DriveDirection, speedPercent: Int = 100)

    /** Momentary steering command: Left / Right / Center */
    fun sendSteerCommand(direction: SteerDirection)

    /** Immediate stop command for drive motors */
    fun sendStopCommand()

    /** Emergency stop: instantly disables TB6612FNG standby pin and locks drive */
    fun sendEmergencyStop(active: Boolean)

    /** Heartbeat latency measurement ping */
    fun sendPing()
}
