package com.example.roverpulse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roverpulse.model.DriveDirection
import com.example.roverpulse.model.SteerDirection
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.SafetyRed
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Responsive momentary touch controller for Drive (Forward/Reverse) and
 * Steering (Left/Right) with Emergency Stop and optional PWM speed slider.
 */
@Composable
fun TouchControlPad(
    activeDrive: DriveDirection,
    activeSteer: SteerDirection,
    isEmergencyStopped: Boolean,
    enableSpeedControl: Boolean,
    speedPercent: Int,
    onDrivePress: (DriveDirection) -> Unit,
    onDriveRelease: () -> Unit,
    onSteerPress: (SteerDirection) -> Unit,
    onSteerRelease: () -> Unit,
    onEmergencyStop: () -> Unit,
    onResetEmergencyStop: () -> Unit,
    onSpeedChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = SurfaceCardDark,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // E-STOP Banner & Button
            if (isEmergencyStopped) {
                Surface(
                    color = SafetyRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SafetyRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "E-Stop Engaged",
                                tint = SafetyRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "FAILSAFE: EMERGENCY STOP ENGAGED",
                                color = SafetyRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SafetyRed)
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = { onResetEmergencyStop() })
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("btn_reset_estop")
                        ) {
                            Text(
                                text = "RESET",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // Dual Control Stations: Steering Pad (Left) & Drive Pad (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT: Steering Control Cluster
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "STEERING (DC MOTOR)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MomentaryControlPadButton(
                            label = "L",
                            icon = Icons.Default.ArrowBack,
                            isActive = activeSteer == SteerDirection.LEFT,
                            isDisabled = isEmergencyStopped,
                            testTag = "btn_steer_left",
                            onPress = { onSteerPress(SteerDirection.LEFT) },
                            onRelease = onSteerRelease
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        MomentaryControlPadButton(
                            label = "R",
                            icon = Icons.Default.ArrowForward,
                            isActive = activeSteer == SteerDirection.RIGHT,
                            isDisabled = isEmergencyStopped,
                            testTag = "btn_steer_right",
                            onPress = { onSteerPress(SteerDirection.RIGHT) },
                            onRelease = onSteerRelease
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (activeSteer != SteerDirection.CENTER) "STEER: ${activeSteer.label}" else "STEER: CENTERED",
                        color = if (activeSteer != SteerDirection.CENTER) CyanNeon else TextSecondary.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // CENTER: Big Emergency Stop Control
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    BigEStopButton(
                        isEngaged = isEmergencyStopped,
                        onClick = onEmergencyStop
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "E-STOP",
                        color = SafetyRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // RIGHT: Drive Motor Cluster (Forward / Reverse)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "DRIVE (TB6612FNG)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MomentaryControlPadButton(
                            label = "FWD",
                            icon = Icons.Default.ArrowUpward,
                            isActive = activeDrive == DriveDirection.FORWARD,
                            isDisabled = isEmergencyStopped,
                            testTag = "btn_drive_fwd",
                            onPress = { onDrivePress(DriveDirection.FORWARD) },
                            onRelease = onDriveRelease
                        )
                        MomentaryControlPadButton(
                            label = "REV",
                            icon = Icons.Default.ArrowDownward,
                            isActive = activeDrive == DriveDirection.REVERSE,
                            isDisabled = isEmergencyStopped,
                            testTag = "btn_drive_rev",
                            onPress = { onDrivePress(DriveDirection.REVERSE) },
                            onRelease = onDriveRelease
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (activeDrive != DriveDirection.STOP) "DRIVE: ${activeDrive.label}" else "DRIVE: IDLE",
                        color = if (activeDrive != DriveDirection.STOP) CyanNeon else TextSecondary.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Speed Control (Only visible when speed / PWM mode is enabled by user)
            if (enableSpeedControl) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PWM SPEED THROTTLE",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$speedPercent%",
                            color = CyanNeon,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = speedPercent.toFloat(),
                        onValueChange = { onSpeedChange(it.toInt()) },
                        valueRange = 20f..100f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanNeon,
                            activeTrackColor = CyanNeon,
                            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.testTag("slider_speed_throttle")
                    )
                }
            }
        }
    }
}

/**
 * Momentary button component with touch-down / touch-up detection.
 * Enforces touch target accessibility standards (minimum 48dp x 48dp).
 */
@Composable
private fun MomentaryControlPadButton(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    isDisabled: Boolean,
    testTag: String,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    var isPressedLocal by remember { mutableStateOf(false) }

    val bgColor by animateColorAsState(
        targetValue = when {
            isDisabled -> SurfaceDark.copy(alpha = 0.4f)
            isActive || isPressedLocal -> CyanNeon.copy(alpha = 0.25f)
            else -> SurfaceDark
        },
        label = "bgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isDisabled -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            isActive || isPressedLocal -> CyanNeon
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        label = "borderColor"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isDisabled -> TextSecondary.copy(alpha = 0.4f)
            isActive || isPressedLocal -> CyanNeon
            else -> TextPrimary
        },
        label = "contentColor"
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .pointerInput(isDisabled) {
                if (!isDisabled) {
                    detectTapGestures(
                        onPress = {
                            isPressedLocal = true
                            onPress()
                            tryAwaitRelease()
                            isPressedLocal = false
                            onRelease()
                        }
                    )
                }
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

/**
 * Prominent physical-style emergency stop button.
 */
@Composable
private fun BigEStopButton(
    isEngaged: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = if (isEngaged) listOf(Color(0xFF881313), SafetyRed)
                    else listOf(SafetyRed, Color(0xFF991B1B))
                )
            )
            .border(
                width = 3.dp,
                color = if (isEngaged) Color.White else Color(0xFFFF8888),
                shape = CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            }
            .testTag("btn_emergency_stop"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ReportProblem,
                contentDescription = "Emergency Stop",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "STOP",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }
    }
}
