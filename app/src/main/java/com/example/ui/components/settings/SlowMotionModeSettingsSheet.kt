package com.example.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SlowMotionFps
import com.example.model.SlowMotionModeSettings
import com.example.model.SlowMotionPlaybackSpeed
import com.example.model.VideoResolution
import com.example.model.VideoStabilization
import com.example.ui.components.PeachActivePill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlowMotionModeSettingsSheet(
    settings: SlowMotionModeSettings,
    onUpdateSettings: (SlowMotionModeSettings) -> Unit,
    onOpenMoreSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF16181B),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("slow_motion_settings_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Slow Motion Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF26292E))
                        .clickable {
                            onDismiss()
                            onOpenMoreSettings()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("btn_more_settings_from_slowmo"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "More", color = PeachActivePill, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "More Settings",
                        tint = PeachActivePill,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. High Frame Rate
            SettingSectionTitle("Capture Frame Rate (Camera2 High-Speed)")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SlowMotionFps.entries) { fps ->
                    val isSelected = settings.frameRate == fps
                    PillChoice(
                        title = fps.label,
                        subtitle = "${fps.fps} frames/sec",
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(frameRate = fps)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Playback Speed
            SettingSectionTitle("Playback Speed")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SlowMotionPlaybackSpeed.entries) { spd ->
                    val isSelected = settings.playbackSpeed == spd
                    PillChoice(
                        title = spd.label,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(playbackSpeed = spd)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Resolution
            SettingSectionTitle("Recording Resolution")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val resolutions = listOf(VideoResolution.FHD_1080P, VideoResolution.HD_720P)
                items(resolutions) { res ->
                    val isSelected = settings.resolution == res
                    PillChoice(
                        title = res.label,
                        subtitle = "${res.width}x${res.height}",
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(resolution = res)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Bitrate
            SettingSectionTitle("Slow Motion Bitrate")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val bitrates = listOf(30 to "30 Mbps", 40 to "40 Mbps (Standard)", 60 to "60 Mbps (High Quality)")
                items(bitrates) { (rate, label) ->
                    val isSelected = settings.bitrateMbps == rate
                    PillChoice(
                        title = label,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(bitrateMbps = rate)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Stabilization
            SettingSectionTitle("High Speed Stabilization")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val stabs = listOf(VideoStabilization.AUTO, VideoStabilization.EIS, VideoStabilization.OIS)
                items(stabs) { stab ->
                    val isSelected = settings.stabilization == stab
                    PillChoice(
                        title = stab.label,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(stabilization = stab)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Toggles
            SettingToggleRow(
                title = "High Speed Exposure Bias",
                subtitle = "Boosts sensor sensitivity for fast shutter cycles",
                checked = settings.highSpeedExposureBias,
                onCheckedChange = { onUpdateSettings(settings.copy(highSpeedExposureBias = it)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleRow(
                title = "Lock Focus during Recording",
                subtitle = "Prevents focus hunting on high-speed moving targets",
                checked = settings.focusLock,
                onCheckedChange = { onUpdateSettings(settings.copy(focusLock = it)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleRow(
                title = "Mute Audio in Slow Motion",
                subtitle = "Eliminates pitch-dropped audio artifacts",
                checked = settings.muteAudio,
                onCheckedChange = { onUpdateSettings(settings.copy(muteAudio = it)) }
            )
        }
    }
}
