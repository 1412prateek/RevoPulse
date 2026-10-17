package com.example.roverpulse.controller

import com.example.roverpulse.model.CameraStreamState
import com.example.roverpulse.model.ConnectionStatus
import com.example.roverpulse.model.DriveDirection
import com.example.roverpulse.model.OperatingMode
import com.example.roverpulse.model.RoverConnectionConfig
import com.example.roverpulse.model.RoverTelemetry
import com.example.roverpulse.model.SteerDirection
import kotlinx.coroutines.flow.StateFlow

/**
 * Transport-agnostic communication interface for the RoverPulse RC car.
 * Concrete implementations can target HTTP REST, WebSockets, UDP datagrams,
 * or simulation/demo without modifying the UI layer.
 */
interface RoverController {
    val connectionStatus: StateFlow<ConnectionStatus>
    val operatingMode: StateFlow<OperatingMode>
    val telemetry: StateFlow<RoverTelemetry?>
    val activeDriveDirection: StateFlow<DriveDirection>
    val activeSteerDirection: StateFlow<SteerDirection>
    val cameraStreamState: StateFlow<CameraStreamState>
    val connectionConfig: StateFlow<RoverConnectionConfig>

    /**
     * Connect to the vehicle or start simulation session.
     */
    fun connect(config: RoverConnectionConfig)

    /**
     * Terminate the connection and execute safe stop.
     */
    fun disconnect()

    /**
     * Momentary drive command: Forward or Reverse.
     * Speed is only applied when hardware PWM is enabled.
     */
    fun sendDriveCommand(direction: DriveDirection, speedPercent: Int = 100)

    /**
     * Momentary steering command: Left, Right, or Center.
     * Standard toy DC steering returns to Center on release.
     */
    fun sendSteerCommand(direction: SteerDirection)

    /**
     * Emergency Stop: Immediate motor cutoff and fail-safe lock.
     */
    fun emergencyStop()

    /**
     * Toggle between Simulation/Demo mode and Real Hardware mode.
     */
    fun setOperatingMode(mode: OperatingMode)

    /**
     * Update connection parameters.
     */
    fun updateConfig(config: RoverConnectionConfig)
}
