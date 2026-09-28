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
import com.example.model.FlashMode
import com.example.model.NightExposureDuration
import com.example.model.NightModeSettings
import com.example.model.NightProcessingMode
import com.example.model.PhotoAspectRatio
import com.example.model.PhotoResolution
import com.example.ui.components.PeachActivePill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NightModeSettingsSheet(
    settings: NightModeSettings,
    onUpdateSettings: (NightModeSettings) -> Unit,
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
        modifier = Modifier.testTag("night_mode_settings_sheet")
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
                    text = "Night Sight Settings",
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
                        .testTag("btn_more_settings_from_night"),
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

            // 1. Exposure Duration
            SettingSectionTitle("Long Exposure Duration")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(NightExposureDuration.entries) { dur ->
                    val isSelected = settings.exposureDuration == dur
                    PillChoice(
                        title = dur.label,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(exposureDuration = dur)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Processing Pipeline
            SettingSectionTitle("Night Multi-Frame Pipeline")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(NightProcessingMode.entries) { proc ->
                    val isSelected = settings.nightProcessing == proc
                    PillChoice(
                        title = proc.label,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(nightProcessing = proc)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Resolution
            SettingSectionTitle("Night Sight Resolution")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf(PhotoResolution.RES_12MP, PhotoResolution.RES_24MP, PhotoResolution.RES_50MP)) { res ->
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

            // 4. Aspect Ratio
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

            // 5. Timer
            SettingSectionTitle("Tripod / Self-Timer")
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

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Toggles
            SettingToggleRow(
                title = "Auto Night Detection",
                subtitle = "Automatically calculates exposure time based on ambient lux",
                checked = settings.autoNight,
                onCheckedChange = { onUpdateSettings(settings.copy(autoNight = it)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleRow(
                title = "Deep Noise Reduction",
                subtitle = "Neural shadow denoising while preserving fine textures",
                checked = settings.strongNoiseReduction,
                onCheckedChange = { onUpdateSettings(settings.copy(strongNoiseReduction = it)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleRow(
                title = "Framing Level Indicator",
                subtitle = "Ensures horizon stability during long exposures",
                checked = settings.levelEnabled,
                onCheckedChange = { onUpdateSettings(settings.copy(levelEnabled = it)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleRow(
                title = "Composition Grid",
                subtitle = "3x3 alignment lines for night landscape composition",
                checked = settings.gridEnabled,
                onCheckedChange = { onUpdateSettings(settings.copy(gridEnabled = it)) }
            )
        }
    }
}
