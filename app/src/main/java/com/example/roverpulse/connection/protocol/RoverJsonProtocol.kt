package com.example.roverpulse.connection.protocol

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * =========================================================================
 * RoverPulse ESP32 WebSocket JSON Protocol Specification (v1.0)
 * =========================================================================
 *
 * All messages exchanged over the WebSocket link are UTF-8 formatted JSON objects.
 * Every message carries a "type" discriminant field.
 *
 * Outgoing Messages (Android -> ESP32):
 *  - "drive"   : Momentary drive motor control (Forward/Reverse/Stop + PWM speed)
 *  - "steer"   : Momentary steering DC motor control (Left/Right/Center)
 *  - "stop"    : Explicit motor stop command
 *  - "estop"   : Hardware emergency stop and fail-safe lock
 *  - "ping"    : Heartbeat / latency estimation packet
 *
 * Incoming Messages (ESP32 -> Android):
 *  - "telemetry" : Periodic telemetry broadcast (battery voltage, RSSI, motor PWM)
 *  - "pong"      : Latency echo response
 *  - "ack"       : Command execution confirmation
 *  - "alert"     : Failsafe or hardware warning alerts
 */

// -------------------------------------------------------------------------
// Outbound Command Models (Android App -> ESP32)
// -------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class DriveJsonCommand(
    @Json(name = "type") val type: String = "drive",
    @Json(name = "direction") val direction: String, // "FWD", "REV", "STOP"
    @Json(name = "speed") val speed: Int,            // 0..100 (%)
    @Json(name = "seq") val sequenceNumber: Long,
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class SteerJsonCommand(
    @Json(name = "type") val type: String = "steer",
    @Json(name = "direction") val direction: String, // "LEFT", "RIGHT", "CENTER"
    @Json(name = "seq") val sequenceNumber: Long,
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class StopJsonCommand(
    @Json(name = "type") val type: String = "stop",
    @Json(name = "seq") val sequenceNumber: Long,
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class EmergencyStopJsonCommand(
    @Json(name = "type") val type: String = "estop",
    @Json(name = "active") val active: Boolean,
    @Json(name = "reason") val reason: String = "USER_ACTION",
    @Json(name = "seq") val sequenceNumber: Long,
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class PingJsonMessage(
    @Json(name = "type") val type: String = "ping",
    @Json(name = "seq") val sequenceNumber: Long,
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)

// -------------------------------------------------------------------------
// Inbound Telemetry & Status Models (ESP32 -> Android App)
// -------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class TelemetryJsonMessage(
    @Json(name = "type") val type: String = "telemetry",
    @Json(name = "vBat") val batteryVoltage: Float? = null,
    @Json(name = "batPct") val batteryPercent: Int? = null,
    @Json(name = "rssi") val wifiRssi: Int? = null,
    @Json(name = "pwmDrive") val driveMotorPwm: Int = 0,
    @Json(name = "steerActive") val steerActive: Boolean = false,
    @Json(name = "failsafe") val failsafeActive: Boolean = false,
    @Json(name = "fw") val firmwareVersion: String? = null,
    @Json(name = "uptimeMs") val uptimeMs: Long = 0
)

@JsonClass(generateAdapter = true)
data class PongJsonMessage(
    @Json(name = "type") val type: String = "pong",
    @Json(name = "echoTimestamp") val echoTimestamp: Long,
    @Json(name = "firmwareUptimeMs") val firmwareUptimeMs: Long = 0
)

@JsonClass(generateAdapter = true)
data class AlertJsonMessage(
    @Json(name = "type") val type: String = "alert",
    @Json(name = "level") val level: String, // "INFO", "WARN", "CRITICAL"
    @Json(name = "code") val code: String,
    @Json(name = "message") val message: String
)
