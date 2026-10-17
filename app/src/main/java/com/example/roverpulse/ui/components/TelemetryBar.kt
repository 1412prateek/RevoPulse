package com.example.roverpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roverpulse.model.OperatingMode
import com.example.roverpulse.model.RoverTelemetry
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DemoModeBadge
import com.example.ui.theme.SafetyRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WarningAmber

/**
 * Real-time telemetry overview bar showing battery voltage, Wi-Fi RSSI,
 * motor state, and heartbeat latency.
 * Null values display clear "No Telemetry" placeholders to prevent misleading readings.
 */
@Composable
fun TelemetryBar(
    telemetry: RoverTelemetry?,
    operatingMode: OperatingMode,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = SurfaceCardDark,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Mode Banner if in Simulation / Demo
            if (operatingMode == OperatingMode.SIMULATION_DEMO) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(DemoModeBadge)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SIMULATION / DEMO TELEMETRY",
                            color = DemoModeBadge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "VIRTUAL HARDWARE",
                        color = TextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Grid of Telemetry Sensors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Battery
                TelemetryPill(
                    icon = if (telemetry?.batteryPercentage != null) Icons.Default.BatteryFull else Icons.Default.BatteryAlert,
                    title = "BATTERY",
                    value = when {
                        telemetry?.batteryVoltage != null -> "%.2f V (%d%%)".format(
                            telemetry.batteryVoltage,
                            telemetry.batteryPercentage ?: 0
                        )
                        else -> "No Telemetry"
                    },
                    statusColor = when {
                        telemetry?.batteryVoltage == null -> TextTertiary
                        (telemetry.batteryPercentage ?: 0) < 20 -> SafetyRed
                        (telemetry.batteryPercentage ?: 0) < 50 -> WarningAmber
                        else -> SuccessGreen
                    },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Wi-Fi Signal
                TelemetryPill(
                    icon = if (telemetry?.wifiSignalDbm != null) Icons.Default.Wifi else Icons.Default.WifiOff,
                    title = "WI-FI RSSI",
                    value = telemetry?.wifiSignalDbm?.let { "$it dBm" } ?: "Disconnected",
                    statusColor = if (telemetry?.wifiSignalDbm != null) CyanNeon else TextTertiary,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Latency / Ping
                TelemetryPill(
                    icon = Icons.Default.Speed,
                    title = "PING",
                    value = telemetry?.commandLatencyMs?.let { "${it}ms" } ?: "--",
                    statusColor = if (telemetry != null) SuccessGreen else TextTertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TelemetryPill(
    icon: ImageVector,
    title: String,
    value: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 8.dp, horizontal = 8.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = statusColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}
