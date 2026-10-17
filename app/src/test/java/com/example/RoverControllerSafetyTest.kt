package com.example

import com.example.roverpulse.connection.protocol.DriveJsonCommand
import com.example.roverpulse.connection.protocol.EmergencyStopJsonCommand
import com.example.roverpulse.connection.protocol.SteerJsonCommand
import com.example.roverpulse.connection.protocol.StopJsonCommand
import com.example.roverpulse.connection.protocol.TelemetryJsonMessage
import com.example.roverpulse.controller.MockRoverController
import com.example.roverpulse.model.DriveDirection
import com.example.roverpulse.model.RoverConnectionConfig
import com.example.roverpulse.model.SteerDirection
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoverControllerSafetyTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @Test
    fun `drive stop command resets active direction`() = runTest(testDispatcher) {
        val controller = MockRoverController()
        controller.connect(RoverConnectionConfig())

        controller.sendDriveCommand(DriveDirection.FORWARD, 100)
        assertEquals(DriveDirection.FORWARD, controller.activeDriveDirection.value)

        // Safety requirement: Momentary release commands STOP
        controller.sendDriveCommand(DriveDirection.STOP, 0)
        assertEquals(DriveDirection.STOP, controller.activeDriveDirection.value)
    }

    @Test
    fun `steering releases back to center`() = runTest(testDispatcher) {
        val controller = MockRoverController()
        controller.connect(RoverConnectionConfig())

        controller.sendSteerCommand(SteerDirection.LEFT)
        assertEquals(SteerDirection.LEFT, controller.activeSteerDirection.value)

        // Safety requirement: Steering returns to center
        controller.sendSteerCommand(SteerDirection.CENTER)
        assertEquals(SteerDirection.CENTER, controller.activeSteerDirection.value)
    }

    @Test
    fun `emergency stop halts motors and locks drive`() = runTest(testDispatcher) {
        val controller = MockRoverController()
        controller.connect(RoverConnectionConfig())

        controller.sendDriveCommand(DriveDirection.FORWARD, 100)
        controller.sendSteerCommand(SteerDirection.RIGHT)

        controller.emergencyStop()
        assertEquals(DriveDirection.STOP, controller.activeDriveDirection.value)
        assertEquals(SteerDirection.CENTER, controller.activeSteerDirection.value)

        // Subsequent drive commands must be rejected while E-Stopped
        controller.sendDriveCommand(DriveDirection.FORWARD, 100)
        assertEquals(DriveDirection.STOP, controller.activeDriveDirection.value)

        // Reset allows driving again
        controller.resetEmergencyStop()
        controller.sendDriveCommand(DriveDirection.FORWARD, 100)
        assertEquals(DriveDirection.FORWARD, controller.activeDriveDirection.value)
    }

    @Test
    fun `json drive and telemetry protocol serialization roundtrip`() {
        val driveCmd = DriveJsonCommand(direction = "FWD", speed = 80, sequenceNumber = 42)
        val driveJson = moshi.adapter(DriveJsonCommand::class.java).toJson(driveCmd)
        assertTrue(driveJson.contains("\"type\":\"drive\""))
        assertTrue(driveJson.contains("\"direction\":\"FWD\""))
        assertTrue(driveJson.contains("\"speed\":80"))

        val parsedDrive = moshi.adapter(DriveJsonCommand::class.java).fromJson(driveJson)
        assertNotNull(parsedDrive)
        assertEquals("FWD", parsedDrive?.direction)
        assertEquals(80, parsedDrive?.speed)

        val telemetryJson = """
            {"type":"telemetry","vBat":5.12,"batPct":78,"rssi":-58,"pwmDrive":204,"steerActive":false,"failsafe":false,"fw":"v1.0","uptimeMs":12345}
        """.trimIndent()
        val parsedTelemetry = moshi.adapter(TelemetryJsonMessage::class.java).fromJson(telemetryJson)
        assertNotNull(parsedTelemetry)
        assertEquals(5.12f, parsedTelemetry?.batteryVoltage ?: 0f, 0.01f)
        assertEquals(78, parsedTelemetry?.batteryPercent)
        assertEquals(-58, parsedTelemetry?.wifiRssi)
    }
}
