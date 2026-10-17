package com.example.roverpulse.ui.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roverpulse.model.ConnectionStatus
import com.example.roverpulse.model.OperatingMode
import com.example.roverpulse.ui.components.ConnectionSettingsDialog
import com.example.roverpulse.ui.components.LiveCameraViewport
import com.example.roverpulse.ui.components.TelemetryBar
import com.example.roverpulse.ui.components.TouchControlPad
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DemoModeBadge
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SafetyRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun RoverDashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle back button when fullscreen camera is active
    BackHandler(enabled = uiState.isCameraFullscreen) {
        viewModel.onToggleCameraFullscreen()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianBg
    ) { innerPadding ->
        if (uiState.isCameraFullscreen) {
            // Fullscreen camera mode
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LiveCameraViewport(
                    streamState = uiState.cameraStreamState,
                    operatingMode = uiState.operatingMode,
                    isFullscreen = true,
                    onToggleFullscreen = { viewModel.onToggleCameraFullscreen() }
                )

                // Overlay minimal touch controls at bottom for driving during fullscreen FPV
                TouchControlPad(
                    activeDrive = uiState.activeDriveDirection,
                    activeSteer = uiState.activeSteerDirection,
                    isEmergencyStopped = uiState.isEmergencyStopped,
                    enableSpeedControl = uiState.connectionConfig.enableSpeedControl,
                    speedPercent = uiState.driveSpeedPercent,
                    onDrivePress = { viewModel.onDriveButtonPressed(it) },
                    onDriveRelease = { viewModel.onDriveButtonReleased() },
                    onSteerPress = { viewModel.onSteerButtonPressed(it) },
                    onSteerRelease = { viewModel.onSteerButtonReleased() },
                    onEmergencyStop = { viewModel.onEmergencyStop() },
                    onResetEmergencyStop = { viewModel.onResetEmergencyStop() },
                    onSpeedChange = { viewModel.onSpeedChanged(it) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                        .navigationBarsPadding()
                )
            }
        } else {
            // Standard Dashboard layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header Bar: App Name & Mode Switcher
                DashboardHeader(
                    operatingMode = uiState.operatingMode,
                    onToggleMode = { viewModel.onToggleOperatingMode() },
                    onOpenSettings = { viewModel.onOpenSettings() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Prominent Mode Indicator Banner
                ModeBanner(
                    operatingMode = uiState.operatingMode,
                    onToggleMode = { viewModel.onToggleOperatingMode() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Connection Status Card
                ConnectionCard(
                    status = uiState.connectionStatus,
                    ipAddress = uiState.connectionConfig.ipAddress,
                    port = uiState.connectionConfig.port,
                    onConnect = { viewModel.onConnectClicked() },
                    onDisconnect = { viewModel.onDisconnectClicked() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Real-time Telemetry Bar
                TelemetryBar(
                    telemetry = uiState.telemetry,
                    operatingMode = uiState.operatingMode
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Live FPV Camera Viewport
                LiveCameraViewport(
                    streamState = uiState.cameraStreamState,
                    operatingMode = uiState.operatingMode,
                    isFullscreen = false,
                    onToggleFullscreen = { viewModel.onToggleCameraFullscreen() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Touch Driving & Steering Control Station
                TouchControlPad(
                    activeDrive = uiState.activeDriveDirection,
                    activeSteer = uiState.activeSteerDirection,
                    isEmergencyStopped = uiState.isEmergencyStopped,
                    enableSpeedControl = uiState.connectionConfig.enableSpeedControl,
                    speedPercent = uiState.driveSpeedPercent,
                    onDrivePress = { viewModel.onDriveButtonPressed(it) },
                    onDriveRelease = { viewModel.onDriveButtonReleased() },
                    onSteerPress = { viewModel.onSteerButtonPressed(it) },
                    onSteerRelease = { viewModel.onSteerButtonReleased() },
                    onEmergencyStop = { viewModel.onEmergencyStop() },
                    onResetEmergencyStop = { viewModel.onResetEmergencyStop() },
                    onSpeedChange = { viewModel.onSpeedChanged(it) }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Settings Dialog
    if (uiState.isSettingsDialogOpen) {
        ConnectionSettingsDialog(
            currentConfig = uiState.connectionConfig,
            onDismiss = { viewModel.onCloseSettings() },
            onSave = { viewModel.onSaveSettings(it) }
        )
    }
}

@Composable
private fun DashboardHeader(
    operatingMode: OperatingMode,
    onToggleMode: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyanNeon.copy(alpha = 0.15f))
                    .border(1.dp, CyanNeon, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "RoverPulse Logo",
                    tint = CyanNeon,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "ROVERPULSE",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "ESP32 RC Car Controller",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Mode toggle chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (operatingMode == OperatingMode.SIMULATION_DEMO) DemoModeBadge.copy(alpha = 0.2f)
                        else CyanNeon.copy(alpha = 0.2f)
                    )
                    .border(
                        1.dp,
                        if (operatingMode == OperatingMode.SIMULATION_DEMO) DemoModeBadge else CyanNeon,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onToggleMode() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("btn_mode_switch")
            ) {
                Text(
                    text = if (operatingMode == OperatingMode.SIMULATION_DEMO) "DEMO" else "HARDWARE",
                    color = if (operatingMode == OperatingMode.SIMULATION_DEMO) DemoModeBadge else CyanNeon,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevatedDark)
                    .testTag("btn_open_settings")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configure Connection",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ModeBanner(
    operatingMode: OperatingMode,
    onToggleMode: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = if (operatingMode == OperatingMode.SIMULATION_DEMO)
            DemoModeBadge.copy(alpha = 0.12f)
        else
            CyanNeon.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (operatingMode == OperatingMode.SIMULATION_DEMO) DemoModeBadge.copy(alpha = 0.6f) else CyanNeon.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (operatingMode == OperatingMode.SIMULATION_DEMO) Icons.Default.Sensors else Icons.Default.CastConnected,
                    contentDescription = "Mode Status",
                    tint = if (operatingMode == OperatingMode.SIMULATION_DEMO) DemoModeBadge else CyanNeon,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (operatingMode == OperatingMode.SIMULATION_DEMO)
                            "ACTIVE: SIMULATION / DEMO MODE"
                        else
                            "ACTIVE: REAL HARDWARE MODE",
                        color = if (operatingMode == OperatingMode.SIMULATION_DEMO) DemoModeBadge else CyanNeon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (operatingMode == OperatingMode.SIMULATION_DEMO)
                            "Safe sandbox with virtual TB6612FNG and synthetic telemetry"
                        else
                            "Real Wi-Fi communication with ESP32 board",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceElevatedDark)
                    .clickable { onToggleMode() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "SWITCH",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ConnectionCard(
    status: ConnectionStatus,
    ipAddress: String,
    port: Int,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = SurfaceCardDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (status) {
                                    is ConnectionStatus.Connected -> SuccessGreen
                                    is ConnectionStatus.Connecting -> DemoModeBadge
                                    is ConnectionStatus.Error -> SafetyRed
                                    is ConnectionStatus.Disconnected -> TextTertiary
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = when (status) {
                                is ConnectionStatus.Connected -> "CONNECTED"
                                is ConnectionStatus.Connecting -> "CONNECTING..."
                                is ConnectionStatus.Error -> "CONNECTION FAULT"
                                is ConnectionStatus.Disconnected -> "STANDBY / OFFLINE"
                            },
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (status) {
                                is ConnectionStatus.Connected -> "${status.host} • ${status.pingMs}ms ping"
                                is ConnectionStatus.Connecting -> "Target: ${status.target}"
                                is ConnectionStatus.Error -> "Hardware protocol unconfigured"
                                is ConnectionStatus.Disconnected -> "$ipAddress:$port (Target)"
                            },
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Connect / Disconnect Action Button
                when (status) {
                    is ConnectionStatus.Connected -> {
                        Button(
                            onClick = onDisconnect,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceElevatedDark,
                                contentColor = SafetyRed
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_disconnect")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LinkOff,
                                contentDescription = "Disconnect",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Disconnect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    is ConnectionStatus.Connecting -> {
                        CircularProgressIndicator(
                            color = CyanNeon,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    else -> {
                        Button(
                            onClick = onConnect,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanNeon,
                                contentColor = Color(0xFF00363D)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_connect")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Connect",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Error notice banner if connection error
            if (status is ConnectionStatus.Error) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = SafetyRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SafetyRed.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = SafetyRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = status.message,
                            color = SafetyRed,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}
