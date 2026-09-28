package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Active highlight color matching reference screenshots
val PeachActivePill = Color(0xFFF6E8DC)
val DarkZoomPillBg = Color(0xCC1A1C1E)

@Composable
fun ZoomSelector(
    activePreset: String,
    hasUltraWide: Boolean,
    ultraWideLabel: String = "0.6×",
    onPresetSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Matches the reference screenshot layout:
    // A single horizontal dark translucent pill centered near the bottom of the viewfinder.
    // Contains rounded buttons for 1× and 2× (and real ultra-wide if exposed by hardware).
    // If ultra-wide is NOT exposed by device hardware, no fake ultra-wide option is displayed.
    Box(
        modifier = modifier
            .testTag("zoom_selector_container")
            .clip(RoundedCornerShape(22.dp))
            .background(DarkZoomPillBg)
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(22.dp))
            .padding(horizontal = 4.dp, vertical = 3.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Real physical ultra-wide lens button (only shown when device hardware exposes ultra-wide)
            if (hasUltraWide) {
                val isUwSelected = activePreset.startsWith("0.") || activePreset == "UW"
                ZoomCircleButton(
                    label = ultraWideLabel,
                    isSelected = isUwSelected,
                    onClick = { onPresetSelected("0.6×") }
                )
            }

            // 1× Main Camera
            val is1xSelected = !activePreset.startsWith("0.") && (activePreset == "1x" || activePreset == "1×" || activePreset == "1.0" || activePreset == "1")
            ZoomCircleButton(
                label = "1×",
                isSelected = is1xSelected,
                onClick = { onPresetSelected("1×") }
            )

            // 2× Zoom / Telephoto
            val is2xSelected = activePreset == "2" || activePreset == "2x" || activePreset == "2×" || activePreset == "2.0"
            ZoomCircleButton(
                label = "2×",
                isSelected = is2xSelected,
                onClick = { onPresetSelected("2×") }
            )
        }
    }
}

@Composable
private fun ZoomCircleButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = animateColorAsState(
        targetValue = if (isSelected) PeachActivePill else Color.Transparent,
        animationSpec = tween(durationMillis = 180),
        label = "zoom_bg"
    )
    val textColor = if (isSelected) Color(0xFF141618) else Color.White

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(bgColor.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("zoom_btn_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
