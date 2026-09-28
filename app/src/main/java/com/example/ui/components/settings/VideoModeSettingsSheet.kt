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
import com.example.model.AudioRecordingMode
import com.example.model.VideoCodec
import com.example.model.VideoFps
import com.example.model.VideoModeSettings
import com.example.model.VideoResolution
import com.example.model.VideoStabilization
import com.example.ui.components.PeachActivePill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoModeSettingsSheet(
    settings: VideoModeSettings,
    onUpdateSettings: (VideoModeSettings) -> Unit,
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
        modifier = Modifier.testTag("video_mode_settings_sheet")
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
                    text = "Video Settings",
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
                        .testTag("btn_more_settings_from_video"),
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

            // 1. Video Resolution
            SettingSectionTitle("Video Resolution")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(VideoResolution.entries) { res ->
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

            // 2. Frame Rate (FPS)
            SettingSectionTitle("Frame Rate")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(VideoFps.entries) { fps ->
                    val isSelected = settings.frameRate == fps
                    PillChoice(
                        title = fps.label,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(frameRate = fps)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Video Stabilization
            SettingSectionTitle("Stabilization Mode")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(VideoStabilization.entries) { stab ->
                    val isSelected = settings.stabilization == stab
                    PillChoice(
                        title = stab.label,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(stabilization = stab)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Video Bitrate
            SettingSectionTitle("Video Encoding Bitrate")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val bitrates = listOf(16 to "16 Mbps (Standard)", 35 to "35 Mbps (High)", 50 to "50 Mbps (Ultra)")
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

            // 5. Video Codec
            SettingSectionTitle("Compression Codec")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(VideoCodec.entries) { codec ->
                    val isSelected = settings.codec == codec
                    PillChoice(
                        title = codec.label,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(codec = codec)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Audio Mode
            SettingSectionTitle("Audio Recording")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AudioRecordingMode.entries) { audio ->
                    val isSelected = settings.audioMode == audio
                    PillChoice(
                        title = audio.label,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(audioMode = audio)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 7. Toggles
            SettingToggleRow(
                title = "Recording Torch",
                subtitle = "Keep LED light on during video recording",
                checked = settings.torchEnabled,
                onCheckedChange = { onUpdateSettings(settings.copy(torchEnabled = it)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleRow(
                title = "Continuous Autofocus (AF)",
                subtitle = "Smooth real-time subject focal tracking",
                checked = settings.continuousAf,
                onCheckedChange = { onUpdateSettings(settings.copy(continuousAf = it)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleRow(
                title = "Auto-Exposure Lock (AE)",
                subtitle = "Prevents sudden brightness fluctuations during panning",
                checked = settings.autoExposureLock,
                onCheckedChange = { onUpdateSettings(settings.copy(autoExposureLock = it)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleRow(
                title = "Framing Level",
                subtitle = "0° Horizon level guide overlay",
                checked = settings.levelEnabled,
                onCheckedChange = { onUpdateSettings(settings.copy(levelEnabled = it)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleRow(
                title = "Grid Lines",
                subtitle = "3x3 alignment grid",
                checked = settings.gridEnabled,
                onCheckedChange = { onUpdateSettings(settings.copy(gridEnabled = it)) }
            )
        }
    }
}
