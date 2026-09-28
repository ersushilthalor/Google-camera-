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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.model.FlashMode
import com.example.model.HdrMode
import com.example.model.ImageProcessingTone
import com.example.model.NoiseReductionMode
import com.example.model.PhotoAspectRatio
import com.example.model.PhotoModeSettings
import com.example.model.PhotoResolution
import com.example.model.RawCaptureMode
import com.example.model.SharpnessMode
import com.example.ui.components.PeachActivePill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoModeSettingsSheet(
    settings: PhotoModeSettings,
    onUpdateSettings: (PhotoModeSettings) -> Unit,
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
        modifier = Modifier.testTag("photo_mode_settings_sheet")
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
                    text = "Photo Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // More Settings Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF26292E))
                        .clickable {
                            onDismiss()
                            onOpenMoreSettings()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("btn_more_settings_from_photo"),
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

            // 1. Resolution
            SettingSectionTitle("Photo Resolution")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PhotoResolution.entries) { res ->
                    val isSelected = settings.resolution == res
                    PillChoice(
                        title = res.title,
                        subtitle = res.megapixels,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(resolution = res)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Aspect Ratio
            SettingSectionTitle("Aspect Ratio")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PhotoAspectRatio.entries) { ar ->
                    val isSelected = settings.aspectRatio == ar
                    PillChoice(
                        title = ar.label,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(aspectRatio = ar)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. HDR / Auto HDR
            SettingSectionTitle("HDR Mode")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(HdrMode.entries) { hdr ->
                    val isSelected = settings.hdrMode == hdr
                    PillChoice(
                        title = hdr.title,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(hdrMode = hdr)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Timer
            SettingSectionTitle("Self-Timer")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val timerOptions = listOf(0 to "Off", 3 to "3 sec", 10 to "10 sec")
                items(timerOptions) { (secs, label) ->
                    val isSelected = settings.timerSeconds == secs
                    PillChoice(
                        title = label,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(timerSeconds = secs)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. RAW Capture Mode
            SettingSectionTitle("RAW Capture (DNG)")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RawCaptureMode.entries) { raw ->
                    val isSelected = settings.rawCapture == raw
                    PillChoice(
                        title = raw.title,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(rawCapture = raw)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Image Processing Tone
            SettingSectionTitle("Color Tone")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ImageProcessingTone.entries) { tone ->
                    val isSelected = settings.imageProcessing == tone
                    PillChoice(
                        title = tone.title,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(imageProcessing = tone)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Noise Reduction
            SettingSectionTitle("Noise Reduction")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(NoiseReductionMode.entries) { nr ->
                    val isSelected = settings.noiseReduction == nr
                    PillChoice(
                        title = nr.title,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(noiseReduction = nr)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 8. Sharpness
            SettingSectionTitle("Sharpness")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SharpnessMode.entries) { sh ->
                    val isSelected = settings.sharpness == sh
                    PillChoice(
                        title = sh.title,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(sharpness = sh)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 9. Grid & Horizon Toggles
            SettingToggleRow(
                title = "Composition Grid (3x3)",
                subtitle = "Align framing with rule of thirds",
                checked = settings.gridEnabled,
                onCheckedChange = { onUpdateSettings(settings.copy(gridEnabled = it)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleRow(
                title = "Framing Level (0° Horizon)",
                subtitle = "Dual-axis roll leveling sensor indicator",
                checked = settings.levelEnabled,
                onCheckedChange = { onUpdateSettings(settings.copy(levelEnabled = it)) }
            )
        }
    }
}

@Composable
fun SettingSectionTitle(title: String) {
    Text(
        text = title,
        color = Color(0xFFA5A8AD),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun PillChoice(
    title: String,
    subtitle: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) PeachActivePill else Color(0xFF222428))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = if (isSelected) Color(0xFF141618) else Color.White,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = if (isSelected) Color(0xFF333538) else Color(0xFF888B90),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = Color(0xFF8A8D92), fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF1E2024),
                checkedTrackColor = PeachActivePill
            )
        )
    }
}
