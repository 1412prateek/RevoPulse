package com.example.roverpulse.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roverpulse.model.CameraStreamState
import com.example.roverpulse.model.OperatingMode
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.SafetyRed
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

/**
 * FPV Camera Viewport component.
 * Separated cleanly from motor controls. Manages Disconnected, Connecting,
 * Streaming (Demo test pattern / real stream placeholder), and Error states.
 */
@Composable
fun LiveCameraViewport(
    streamState: CameraStreamState,
    operatingMode: OperatingMode,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (!isFullscreen) Modifier.aspectRatio(16f / 9f) else Modifier.fillMaxSize())
            .clip(RoundedCornerShape(if (isFullscreen) 0.dp else 16.dp))
            .border(
                width = if (isFullscreen) 0.dp else 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(if (isFullscreen) 0.dp else 16.dp)
            ),
        color = Color(0xFF070B11)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (streamState) {
                is CameraStreamState.Disconnected -> {
                    CameraDisconnectedState()
                }
                is CameraStreamState.Connecting -> {
                    CameraConnectingState(target = streamState.endpoint)
                }
                is CameraStreamState.Streaming -> {
                    CameraStreamingState(
                        endpoint = streamState.endpoint,
                        fps = streamState.fps,
                        operatingMode = operatingMode
                    )
                }
                is CameraStreamState.Error -> {
                    CameraErrorState(error = streamState.message)
                }
            }

            // Top HUD Overlay: Stream Status & Fullscreen Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Stream Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    when (streamState) {
                                        is CameraStreamState.Streaming -> CyanNeon
                                        is CameraStreamState.Connecting -> Color(0xFFF59E0B)
                                        else -> Color(0xFF64748B)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (streamState) {
                                is CameraStreamState.Streaming -> "LIVE FPV (DEMO)"
                                is CameraStreamState.Connecting -> "CONNECTING..."
                                is CameraStreamState.Error -> "STREAM FAULT"
                                is CameraStreamState.Disconnected -> "CAM OFFLINE"
                            },
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Fullscreen button
                IconButton(
                    onClick = onToggleFullscreen,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .testTag("btn_toggle_fullscreen")
                ) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = "Toggle Fullscreen",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraDisconnectedState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.VideocamOff,
            contentDescription = "Camera Disconnected",
            tint = TextTertiary,
            modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "ESP32-CAM STREAM STANDBY",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Video stream awaits hardware connection & protocol confirmation",
            color = TextTertiary,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun CameraConnectingState(target: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = CyanNeon,
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "HANDSHAKE: $target",
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CameraStreamingState(
    endpoint: String,
    fps: Int,
    operatingMode: OperatingMode
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // High-tech FPV HUD Crosshairs & Radar Grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Center targeting reticle
            drawCircle(
                color = CyanNeon.copy(alpha = 0.25f),
                radius = 28f,
                center = Offset(cx, cy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
            )
            // Center point
            drawCircle(
                color = CyanNeon,
                radius = 3f,
                center = Offset(cx, cy)
            )
            // Crosshairs
            drawLine(
                color = CyanNeon.copy(alpha = 0.4f),
                start = Offset(cx - 50f, cy),
                end = Offset(cx - 35f, cy),
                strokeWidth = 2f
            )
            drawLine(
                color = CyanNeon.copy(alpha = 0.4f),
                start = Offset(cx + 35f, cy),
                end = Offset(cx + 50f, cy),
                strokeWidth = 2f
            )
            drawLine(
                color = CyanNeon.copy(alpha = 0.4f),
                start = Offset(cx, cy - 50f),
                end = Offset(cx, cy - 35f),
                strokeWidth = 2f
            )
            drawLine(
                color = CyanNeon.copy(alpha = 0.4f),
                start = Offset(cx, cy + 35f),
                end = Offset(cx, cy + 50f),
                strokeWidth = 2f
            )
        }

        // Bottom HUD info banner
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column {
                Text(
                    text = if (operatingMode == OperatingMode.SIMULATION_DEMO)
                        "SIMULATED TEST PATTERN • $fps FPS"
                    else "STREAM ACTIVE • $fps FPS",
                    color = CyanNeon,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = endpoint,
                    color = TextSecondary,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun CameraErrorState(error: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Stream Error",
            tint = SafetyRed,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "CAMERA STREAM PENDING",
            color = SafetyRed,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = error,
            color = TextSecondary,
            fontSize = 11.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
