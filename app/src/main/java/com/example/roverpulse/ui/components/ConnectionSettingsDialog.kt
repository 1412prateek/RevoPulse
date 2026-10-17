package com.example.roverpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roverpulse.model.RoverConnectionConfig
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

/**
 * Settings dialog for configuring ESP32 network endpoints, watchdog limits,
 * and pending protocol preferences.
 */
@Composable
fun ConnectionSettingsDialog(
    currentConfig: RoverConnectionConfig,
    onDismiss: () -> Unit,
    onSave: (RoverConnectionConfig) -> Unit
) {
    var ipAddress by remember { mutableStateOf(currentConfig.ipAddress) }
    var port by remember { mutableStateOf(currentConfig.port.toString()) }
    var cameraUrl by remember { mutableStateOf(currentConfig.cameraStreamUrl) }
    var timeoutMs by remember { mutableStateOf(currentConfig.commandTimeoutMs.toString()) }
    var enableSpeed by remember { mutableStateOf(currentConfig.enableSpeedControl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = CyanNeon
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hardware & Network Config",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Pending Decision Banner
                Surface(
                    color = SurfaceElevatedDark,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Pending Protocol Notice",
                            tint = CyanNeon,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Communication Architecture",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Protocol currently open (HTTP REST / WebSocket / UDP). Hardware commands and endpoints will adapt to your chosen firmware design.",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // IP Address
                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    label = { Text("ESP32 IP Address") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        focusedLabelColor = CyanNeon,
                        unfocusedTextColor = TextPrimary,
                        focusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_esp32_ip")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Port
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Control Port") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        focusedLabelColor = CyanNeon,
                        unfocusedTextColor = TextPrimary,
                        focusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_esp32_port")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Camera Stream URL
                OutlinedTextField(
                    value = cameraUrl,
                    onValueChange = { cameraUrl = it },
                    label = { Text("ESP32-CAM Stream URL") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        focusedLabelColor = CyanNeon,
                        unfocusedTextColor = TextPrimary,
                        focusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_camera_url")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Command Watchdog Timeout
                OutlinedTextField(
                    value = timeoutMs,
                    onValueChange = { timeoutMs = it },
                    label = { Text("Failsafe Watchdog Timeout (ms)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        focusedLabelColor = CyanNeon,
                        unfocusedTextColor = TextPrimary,
                        focusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_watchdog_timeout")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // PWM Speed Control Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PWM Speed Regulation",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Enable throttle slider only if firmware supports PWM calibrated drive.",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = enableSpeed,
                        onCheckedChange = { enableSpeed = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanNeon,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("switch_enable_pwm")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedPort = port.toIntOrNull() ?: 8080
                    val parsedTimeout = timeoutMs.toLongOrNull() ?: 300L
                    onSave(
                        currentConfig.copy(
                            ipAddress = ipAddress.trim(),
                            port = parsedPort,
                            cameraStreamUrl = cameraUrl.trim(),
                            commandTimeoutMs = parsedTimeout,
                            enableSpeedControl = enableSpeed
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanNeon,
                    contentColor = Color(0xFF00363D)
                ),
                modifier = Modifier.testTag("btn_save_config")
            ) {
                Text("Save Config", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_config")
            ) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceCardDark
    )
}
