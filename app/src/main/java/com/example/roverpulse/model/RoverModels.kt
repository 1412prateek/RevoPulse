package com.example.roverpulse.model

/**
 * Drive motor direction command for the TB6612FNG H-bridge driver.
 */
enum class DriveDirection(val label: String) {
    STOP("STOP"),
    FORWARD("FWD"),
    REVERSE("REV")
}

/**
 * Steering motor direction command for the standard DC toy steering motor.
 * Steering returns to center when released.
 */
enum class SteerDirection(val label: String) {
    CENTER("CTR"),
    LEFT("LEFT"),
    RIGHT("RIGHT")
}

/**
 * Operating mode: Visibly separates real physical hardware from simulation/demo.
 */
enum class OperatingMode(val title: String) {
    SIMULATION_DEMO("Demo / Simulation Mode"),
    REAL_HARDWARE("Real Hardware Mode")
}

/**
 * Connection states for ESP32 link.
 */
sealed interface ConnectionStatus {
    object Disconnected : ConnectionStatus
    data class Connecting(val target: String) : ConnectionStatus
    data class Connected(
        val host: String,
        val pingMs: Long,
        val connectedAtMs: Long
    ) : ConnectionStatus
    data class Error(val message: String, val canRetry: Boolean = true) : ConnectionStatus
}

/**
 * Camera stream states for the ESP32-CAM module.
 */
sealed interface CameraStreamState {
    object Disconnected : CameraStreamState
    data class Connecting(val endpoint: String) : CameraStreamState
    data class Streaming(val endpoint: String, val fps: Int) : CameraStreamState
    data class Error(val message: String) : CameraStreamState
}

/**
 * Telemetry received from the rover.
 * All hardware values are nullable — never faked as real when disconnected.
 */
data class RoverTelemetry(
    val batteryVoltage: Float? = null,
    val batteryPercentage: Int? = null,
    val wifiSignalDbm: Int? = null,
    val firmwareVersion: String? = null,
    val driveMotorPwm: Int = 0,
    val steeringActive: Boolean = false,
    val isEmergencyStopped: Boolean = false,
    val commandLatencyMs: Long = 0,
    val lastHeartbeatTimestamp: Long = 0,
    val isSimulated: Boolean = false
)

/**
 * Configurable connection parameters.
 */
data class RoverConnectionConfig(
    val ipAddress: String = "192.168.4.1",
    val port: Int = 8080,
    val transportProtocol: String = "Pending Protocol Confirmation (HTTP / WebSocket / UDP)",
    val cameraStreamUrl: String = "http://192.168.4.2:81/stream",
    val commandTimeoutMs: Long = 300,
    val enableSpeedControl: Boolean = false // Off by default for standard toy DC motors without PWM calibration
)
