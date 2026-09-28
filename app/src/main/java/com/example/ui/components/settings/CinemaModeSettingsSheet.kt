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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.CinemaAspectRatio
import com.example.model.CinemaColorSpace
import com.example.model.CinemaFocusControl
import com.example.model.CinemaFps
import com.example.model.CinemaLogProfile
import com.example.model.CinemaLut
import com.example.model.CinemaModeSettings
import com.example.model.VideoCodec
import com.example.model.VideoResolution
import com.example.model.VideoStabilization
import com.example.ui.components.PeachActivePill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaModeSettingsSheet(
    settings: CinemaModeSettings,
    onUpdateSettings: (CinemaModeSettings) -> Unit,
    onOpenMoreSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Recording", "Log Profiles", "3D LUTs", "Color Science", "Monitoring", "Stabilization")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141619),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("cinema_mode_settings_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cinema Pro Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF24272D))
                        .clickable {
                            onDismiss()
                            onOpenMoreSettings()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("btn_more_settings_from_cinema"),
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

            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF141619),
                contentColor = PeachActivePill,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PeachActivePill
                    )
                },
                edgePadding = 20.dp
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index) PeachActivePill else Color(0xFF8E9298),
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                when (selectedTab) {
                    0 -> CinemaRecordingSection(settings, onUpdateSettings)
                    1 -> CinemaLogProfilesSection(settings, onUpdateSettings)
                    2 -> CinemaLutSection(settings, onUpdateSettings)
                    3 -> CinemaColorScienceSection(settings, onUpdateSettings)
                    4 -> CinemaMonitoringSection(settings, onUpdateSettings)
                    5 -> CinemaStabilizationSection(settings, onUpdateSettings)
                }
            }
        }
    }
}

@Composable
private fun CinemaRecordingSection(
    settings: CinemaModeSettings,
    onUpdate: (CinemaModeSettings) -> Unit
) {
    SettingSectionTitle("Cinema Frame Rate (Target Lock)")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(CinemaFps.entries) { fps ->
            val isSelected = settings.frameRate == fps
            PillChoice(
                title = fps.label,
                subtitle = null,
                isSelected = isSelected,
                onClick = { onUpdate(settings.copy(frameRate = fps)) }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    SettingSectionTitle("Master Resolution")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val resolutions = listOf(VideoResolution.UHD_4K, VideoResolution.FHD_1080P)
        items(resolutions) { res ->
            val isSelected = settings.resolution == res
            PillChoice(
                title = res.label,
                subtitle = "${res.width}x${res.height}",
                isSelected = isSelected,
                onClick = { onUpdate(settings.copy(resolution = res)) }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    SettingSectionTitle("Aspect Ratio & Matte Guides")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(CinemaAspectRatio.entries) { ar ->
            val isSelected = settings.aspectRatio == ar
            PillChoice(
                title = ar.label,
                subtitle = null,
                isSelected = isSelected,
                onClick = { onUpdate(settings.copy(aspectRatio = ar)) }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    SettingSectionTitle("Encoding Codec & Bit Depth")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(VideoCodec.entries) { codec ->
            val isSelected = settings.codec == codec
            PillChoice(
                title = codec.label,
                subtitle = null,
                isSelected = isSelected,
                onClick = { onUpdate(settings.copy(codec = codec)) }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    SettingSectionTitle("Cinema Bitrate (All-Intra / High)")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val bitrates = listOf(50 to "50 Mbps (Cinema High)", 100 to "100 Mbps (All-Intra Master)")
        items(bitrates) { (mbps, label) ->
            val isSelected = settings.bitrateMbps == mbps
            PillChoice(
                title = label,
                subtitle = null,
                isSelected = isSelected,
                onClick = { onUpdate(settings.copy(bitrateMbps = mbps)) }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    SettingSectionTitle("Cinema Focus Control")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(CinemaFocusControl.entries) { foc ->
            val isSelected = settings.focusControl == foc
            PillChoice(
                title = foc.label,
                subtitle = null,
                isSelected = isSelected,
                onClick = { onUpdate(settings.copy(focusControl = foc)) }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    SettingSectionTitle("Shutter Angle Standard")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val angles = listOf("180° (1/48s Film Standard)", "90° (1/96s Action)", "360° (1/24s Low Light)")
        items(angles) { ang ->
            val isSelected = settings.shutterAngle == ang
            PillChoice(
                title = ang,
                subtitle = null,
                isSelected = isSelected,
                onClick = { onUpdate(settings.copy(shutterAngle = ang)) }
            )
        }
    }
}

@Composable
private fun CinemaLogProfilesSection(
    settings: CinemaModeSettings,
    onUpdate: (CinemaModeSettings) -> Unit
) {
    SettingToggleRow(
        title = "Log Recording Pipeline",
        subtitle = "Encodes sensor dynamic range using wide-gamut log gamma",
        checked = settings.logRecordingEnabled,
        onCheckedChange = { onUpdate(settings.copy(logRecordingEnabled = it)) }
    )

    Spacer(modifier = Modifier.height(16.dp))

    SettingSectionTitle("Cinema Log Gamma Curves")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CinemaLogProfile.entries.forEach { profile ->
            val isSelected = settings.logProfile == profile
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) PeachActivePill else Color(0xFF222428))
                    .clickable { onUpdate(settings.copy(logProfile = profile)) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = profile.displayName,
                        color = if (isSelected) Color(0xFF141618) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = profile.description,
                        color = if (isSelected) Color(0xFF333538) else Color(0xFF888B90),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    SettingSectionTitle("Color Space Gamut")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(CinemaColorSpace.entries) { cs ->
            val isSelected = settings.colorSpace == cs
            PillChoice(
                title = cs.label,
                subtitle = null,
                isSelected = isSelected,
                onClick = { onUpdate(settings.copy(colorSpace = cs)) }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    SettingToggleRow(
        title = "10-bit HDR Quantization",
        subtitle = "1.07 billion colors preventing color banding in sky gradients",
        checked = settings.bitDepth10Bit,
        onCheckedChange = { onUpdate(settings.copy(bitDepth10Bit = it)) }
    )
}

@Composable
private fun CinemaLutSection(
    settings: CinemaModeSettings,
    onUpdate: (CinemaModeSettings) -> Unit
) {
    SettingToggleRow(
        title = "Enable 3D LUT Color Grading",
        subtitle = "Real-time look-up table color transformation",
        checked = settings.lutEnabled,
        onCheckedChange = { onUpdate(settings.copy(lutEnabled = it)) }
    )

    Spacer(modifier = Modifier.height(12.dp))

    SettingToggleRow(
        title = "Bake LUT into Exported Video (Requirement 8)",
        subtitle = "Applies active LUT directly to final MP4 video file",
        checked = settings.applyLutToExportedVideo,
        onCheckedChange = { onUpdate(settings.copy(applyLutToExportedVideo = it)) }
    )

    Spacer(modifier = Modifier.height(16.dp))

    // LUT Intensity
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingSectionTitle("LUT Intensity Blend")
        Text(
            text = "${(settings.lutIntensity * 100).toInt()}%",
            color = PeachActivePill,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Slider(
        value = settings.lutIntensity,
        onValueChange = { onUpdate(settings.copy(lutIntensity = it)) },
        valueRange = 0.0f..1.0f,
        colors = SliderDefaults.colors(
            thumbColor = PeachActivePill,
            activeTrackColor = PeachActivePill,
            inactiveTrackColor = Color(0xFF2A2D33)
        ),
        modifier = Modifier.testTag("slider_lut_intensity")
    )

    Spacer(modifier = Modifier.height(16.dp))

    SettingSectionTitle("Select 3D LUT Profile")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CinemaLut.entries.forEach { lut ->
            val isSelected = settings.activeLut == lut
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) PeachActivePill else Color(0xFF222428))
                    .clickable { onUpdate(settings.copy(activeLut = lut)) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = lut.displayName,
                        color = if (isSelected) Color(0xFF141618) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = lut.description,
                        color = if (isSelected) Color(0xFF333538) else Color(0xFF888B90),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Import .CUBE LUT simulation button
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF26292E))
            .clickable {
                onUpdate(settings.copy(activeLut = CinemaLut.CUSTOM_CUBE, customLutLoaded = true))
            }
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Import .cube 3D LUT File",
            color = PeachActivePill,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CinemaColorScienceSection(
    settings: CinemaModeSettings,
    onUpdate: (CinemaModeSettings) -> Unit
) {
    // White Balance Kelvin
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingSectionTitle("White Balance (Kelvin)")
        Text(
            text = "${settings.whiteBalanceKelvin}K",
            color = PeachActivePill,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Slider(
        value = settings.whiteBalanceKelvin.toFloat(),
        onValueChange = { onUpdate(settings.copy(whiteBalanceKelvin = it.toInt())) },
        valueRange = 3200f..6500f,
        steps = 32,
        colors = SliderDefaults.colors(
            thumbColor = PeachActivePill,
            activeTrackColor = PeachActivePill,
            inactiveTrackColor = Color(0xFF2A2D33)
        )
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Tint (-10 to +10)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingSectionTitle("Color Tint (Green / Magenta)")
        Text(
            text = "${if (settings.tint > 0) "+" else ""}${settings.tint}",
            color = PeachActivePill,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Slider(
        value = settings.tint.toFloat(),
        onValueChange = { onUpdate(settings.copy(tint = it.toInt())) },
        valueRange = -10f..10f,
        steps = 20,
        colors = SliderDefaults.colors(
            thumbColor = PeachActivePill,
            activeTrackColor = PeachActivePill,
            inactiveTrackColor = Color(0xFF2A2D33)
        )
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Contrast
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingSectionTitle("Contrast Curve")
        Text(
            text = "${(settings.contrast * 100).toInt()}%",
            color = PeachActivePill,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Slider(
        value = settings.contrast,
        onValueChange = { onUpdate(settings.copy(contrast = it)) },
        valueRange = 0.6f..1.4f,
        colors = SliderDefaults.colors(
            thumbColor = PeachActivePill,
            activeTrackColor = PeachActivePill,
            inactiveTrackColor = Color(0xFF2A2D33)
        )
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Saturation
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingSectionTitle("Chroma Saturation")
        Text(
            text = "${(settings.saturation * 100).toInt()}%",
            color = PeachActivePill,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Slider(
        value = settings.saturation,
        onValueChange = { onUpdate(settings.copy(saturation = it)) },
        valueRange = 0.0f..1.6f,
        colors = SliderDefaults.colors(
            thumbColor = PeachActivePill,
            activeTrackColor = PeachActivePill,
            inactiveTrackColor = Color(0xFF2A2D33)
        )
    )
}

@Composable
private fun CinemaMonitoringSection(
    settings: CinemaModeSettings,
    onUpdate: (CinemaModeSettings) -> Unit
) {
    SettingToggleRow(
        title = "Real-Time Histogram",
        subtitle = "Luminance RGB distribution graph overlay",
        checked = settings.histogramEnabled,
        onCheckedChange = { onUpdate(settings.copy(histogramEnabled = it)) }
    )

    Spacer(modifier = Modifier.height(10.dp))

    SettingToggleRow(
        title = "Waveform Monitor",
        subtitle = "IRE exposure scale across the frame",
        checked = settings.waveformEnabled,
        onCheckedChange = { onUpdate(settings.copy(waveformEnabled = it)) }
    )

    Spacer(modifier = Modifier.height(10.dp))

    SettingToggleRow(
        title = "Zebra Exposure Warning",
        subtitle = "Diagonal stripes indicate 95%+ clipped highlights",
        checked = settings.zebraWarningEnabled,
        onCheckedChange = { onUpdate(settings.copy(zebraWarningEnabled = it)) }
    )

    Spacer(modifier = Modifier.height(10.dp))

    SettingToggleRow(
        title = "Focus Peaking",
        subtitle = "Cyan edge highlight over in-focus high-frequency regions",
        checked = settings.focusPeakingEnabled,
        onCheckedChange = { onUpdate(settings.copy(focusPeakingEnabled = it)) }
    )

    Spacer(modifier = Modifier.height(10.dp))

    SettingToggleRow(
        title = "Framing Guides",
        subtitle = "Cinematic 2.39:1 letterbox matte overlay",
        checked = settings.framingGuidesEnabled,
        onCheckedChange = { onUpdate(settings.copy(framingGuidesEnabled = it)) }
    )

    Spacer(modifier = Modifier.height(10.dp))

    SettingToggleRow(
        title = "Horizon Level (0° Line)",
        subtitle = "Gyroscope roll sensor leveling indicator",
        checked = settings.horizonLevelEnabled,
        onCheckedChange = { onUpdate(settings.copy(horizonLevelEnabled = it)) }
    )
}

@Composable
private fun CinemaStabilizationSection(
    settings: CinemaModeSettings,
    onUpdate: (CinemaModeSettings) -> Unit
) {
    SettingSectionTitle("Cinema Stabilization Engine")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        VideoStabilization.entries.forEach { stab ->
            val isSelected = settings.stabilization == stab
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) PeachActivePill else Color(0xFF222428))
                    .clickable { onUpdate(settings.copy(stabilization = stab)) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = stab.label,
                    color = if (isSelected) Color(0xFF141618) else Color.White,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
