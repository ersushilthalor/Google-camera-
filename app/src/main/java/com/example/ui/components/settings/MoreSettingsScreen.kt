package com.example.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.GlobalMoreSettings
import com.example.ui.components.PeachActivePill

@Composable
fun MoreSettingsScreen(
    settings: GlobalMoreSettings,
    onUpdateSettings: (GlobalMoreSettings) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("more_settings_dialog_screen"),
            color = Color(0xFF101214)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Camera Settings",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_more_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFF222428))

                // Scrollable Category List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        CategoryHeader("GENERAL")
                        SettingToggleRow(
                            title = "Framing Level",
                            subtitle = "Show 0° horizon line",
                            checked = settings.globalLevel,
                            onCheckedChange = { onUpdateSettings(settings.copy(globalLevel = it)) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingToggleRow(
                            title = "Composition Grid",
                            subtitle = "Show 3x3 grid lines",
                            checked = settings.globalGrid,
                            onCheckedChange = { onUpdateSettings(settings.copy(globalGrid = it)) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingToggleRow(
                            title = "Camera Shutter Sound",
                            subtitle = "Audible feedback on capture",
                            checked = settings.shutterSound,
                            onCheckedChange = { onUpdateSettings(settings.copy(shutterSound = it)) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        InfoValueRow(title = "Default Camera Mode", value = settings.defaultCameraMode)
                        InfoValueRow(title = "Save Location", value = settings.saveLocation)
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        CategoryHeader("CAMERA HARDWARE (CAMERA2)")
                        InfoValueRow(title = "Hardware Support Level", value = settings.hardwareLevel)
                        InfoValueRow(title = "Sensor Resolution", value = settings.sensorMegapixels)
                        InfoValueRow(title = "High Speed Frame Rates", value = settings.supportedFpsList)
                        InfoValueRow(title = "Lenses Available", value = settings.availableLenses)
                        InfoValueRow(title = "Ultra-Wide Lens", value = settings.ultraWideLensReport)
                        InfoValueRow(title = "Optical Stabilization (OIS)", value = if (settings.oisSupported) "Supported & Active" else "Unsupported")
                        InfoValueRow(title = "RAW DNG Output", value = if (settings.rawDngSupported) "Supported" else "Unsupported")
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        CategoryHeader("PHOTO DEFAULTS")
                        InfoValueRow(title = "Default Photo Resolution", value = settings.defaultPhotoResolution.title)
                        InfoValueRow(title = "Default Photo Format", value = settings.defaultPhotoFormat)
                        InfoValueRow(title = "Color Science Profile", value = settings.defaultColorTone.title)
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        CategoryHeader("VIDEO DEFAULTS")
                        InfoValueRow(title = "Default Video Resolution", value = settings.defaultVideoResolution.label)
                        InfoValueRow(title = "Default Frame Rate", value = settings.defaultVideoFps.label)
                        InfoValueRow(title = "Default Video Codec", value = settings.defaultVideoCodec.label)
                        InfoValueRow(title = "Default Video Stabilization", value = settings.defaultVideoStabilization.label)
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        CategoryHeader("CINEMA DEFAULTS")
                        InfoValueRow(title = "Default Color Profile", value = settings.defaultCinemaProfile.displayName)
                        InfoValueRow(title = "Default 3D LUT", value = settings.defaultCinemaLut.displayName)
                        InfoValueRow(title = "Default Master Resolution", value = settings.defaultCinemaResolution.label)
                        InfoValueRow(title = "Master Frame Rate", value = settings.defaultCinemaFps.label)
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        CategoryHeader("STORAGE & FILES")
                        InfoValueRow(title = "Internal Storage Available", value = settings.internalFreeSpace)
                        InfoValueRow(title = "File Naming Format", value = settings.fileNamingScheme)
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        CategoryHeader("ADVANCED & PERFORMANCE")
                        SettingToggleRow(
                            title = "High Performance Mode",
                            subtitle = "Maximizes GPU acceleration for pipeline processing",
                            checked = settings.highPerformanceMode,
                            onCheckedChange = { onUpdateSettings(settings.copy(highPerformanceMode = it)) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingToggleRow(
                            title = "Multi-Frame Neural Denoise",
                            subtitle = "AI noise reduction for low-light frames",
                            checked = settings.neuralDenoise,
                            onCheckedChange = { onUpdateSettings(settings.copy(neuralDenoise = it)) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingToggleRow(
                            title = "Camera2 Debug Overlay",
                            subtitle = "Shows real-time FPS, shutter, and buffer stats",
                            checked = settings.debugOverlay,
                            onCheckedChange = { onUpdateSettings(settings.copy(debugOverlay = it)) }
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(title: String) {
    Text(
        text = title,
        color = PeachActivePill,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun InfoValueRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = Color.White, fontSize = 14.sp)
        Text(text = value, color = Color(0xFFA5A9AF), fontSize = 13.sp)
    }
}
