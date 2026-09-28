package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
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
import com.example.model.CameraModeGroup
import com.example.model.FlashMode

@Composable
fun TopControlBar(
    modeGroup: CameraModeGroup,
    isRecording: Boolean,
    recordingSeconds: Int,
    is4k: Boolean,
    flashMode: FlashMode,
    onToggle4k: () -> Unit,
    onToggleFlash: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: 4K / FHD badge (as shown in reference screenshot top-left!)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFF6E7175), RoundedCornerShape(6.dp))
                .background(Color(0xFF141618))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle4k
                )
                .padding(horizontal = 8.dp, vertical = 3.dp)
                .testTag("resolution_badge"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (is4k) "4K" else "FHD",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Center: Recording duration pill when active
        AnimatedVisibility(visible = isRecording) {
            val min = recordingSeconds / 60
            val sec = recordingSeconds % 60
            val timeFormatted = String.format("%02d:%02d", min, sec)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEA4335))
                )
                Text(
                    text = timeFormatted,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Right: Flash toggle button
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E2024))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggleFlash
                )
                .testTag("flash_toggle_button"),
            contentAlignment = Alignment.Center
        ) {
            val (icon, tint) = when (flashMode) {
                FlashMode.OFF -> Pair(Icons.Filled.FlashOff, Color(0xFFB0B3B8))
                FlashMode.AUTO -> Pair(Icons.Filled.FlashAuto, Color.White)
                FlashMode.ON, FlashMode.TORCH -> Pair(Icons.Filled.FlashOn, Color(0xFFFBC02D))
            }
            Icon(
                imageVector = icon,
                contentDescription = "Flash ${flashMode.name}",
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
