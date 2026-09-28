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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.model.PhotoAspectRatio
import com.example.model.PhotoResolution
import com.example.model.PortraitBlurStyle
import com.example.model.PortraitLightingMode
import com.example.model.PortraitModeSettings
import com.example.model.SkinToneMode
import com.example.ui.components.PeachActivePill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortraitModeSettingsSheet(
    settings: PortraitModeSettings,
    onUpdateSettings: (PortraitModeSettings) -> Unit,
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
        modifier = Modifier.testTag("portrait_mode_settings_sheet")
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
                    text = "Portrait Settings",
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
                        .testTag("btn_more_settings_from_portrait"),
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

            // 1. Aperture & Blur Intensity
            val apertureLabel = when {
                settings.blurIntensity >= 0.85f -> "f/1.4 (Max Bokeh)"
                settings.blurIntensity >= 0.65f -> "f/2.0 (Prime)"
                settings.blurIntensity >= 0.45f -> "f/2.8 (Portrait)"
                settings.blurIntensity >= 0.25f -> "f/4.0 (Balanced)"
                else -> "f/8.0 (Deep Field)"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SettingSectionTitle("Aperture / Background Blur")
                Text(text = apertureLabel, color = PeachActivePill, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Slider(
                value = settings.blurIntensity,
                onValueChange = { onUpdateSettings(settings.copy(blurIntensity = it)) },
                valueRange = 0.1f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = PeachActivePill,
                    activeTrackColor = PeachActivePill,
                    inactiveTrackColor = Color(0xFF2A2D33)
                ),
                modifier = Modifier.testTag("slider_portrait_blur")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Blur Style
            SettingSectionTitle("Bokeh Blur Style")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PortraitBlurStyle.entries) { style ->
                    val isSelected = settings.blurStyle == style
                    PillChoice(
                        title = style.title,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(blurStyle = style)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Portrait Studio Lighting
            SettingSectionTitle("Studio Portrait Lighting")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PortraitLightingMode.entries) { light ->
                    val isSelected = settings.lightingMode == light
                    PillChoice(
                        title = light.title,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(lightingMode = light)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Skin Tone Processing
            SettingSectionTitle("Skin Tone Retouch")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SkinToneMode.entries) { skin ->
                    val isSelected = settings.skinTone == skin
                    PillChoice(
                        title = skin.title,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(skinTone = skin)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Resolution & Aspect Ratio
            SettingSectionTitle("Portrait Resolution")
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

            SettingSectionTitle("Aspect Ratio")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf(PhotoAspectRatio.RATIO_4_3, PhotoAspectRatio.RATIO_1_1)) { ar ->
                    val isSelected = settings.aspectRatio == ar
                    PillChoice(
                        title = ar.label,
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(aspectRatio = ar)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Face Detection Toggle
            SettingToggleRow(
                title = "Face Detection Priority",
                subtitle = "Locks AF/AE tracking on eyes and facial contours",
                checked = settings.faceDetectionEnabled,
                onCheckedChange = { onUpdateSettings(settings.copy(faceDetectionEnabled = it)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleRow(
                title = "Composition Grid",
                subtitle = "Rule of thirds overlay for framing portraits",
                checked = settings.gridEnabled,
                onCheckedChange = { onUpdateSettings(settings.copy(gridEnabled = it)) }
            )
        }
    }
}
