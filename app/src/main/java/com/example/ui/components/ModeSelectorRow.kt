package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.example.model.CameraModeGroup
import com.example.model.PhotoMode
import com.example.model.VideoMode

@Composable
fun ModeSelectorRow(
    modeGroup: CameraModeGroup,
    selectedPhotoMode: PhotoMode,
    selectedVideoMode: VideoMode,
    onSelectPhotoMode: (PhotoMode) -> Unit,
    onSelectVideoMode: (VideoMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (modeGroup == CameraModeGroup.PHOTO) {
            // PHOTO GROUP: Portrait | Photo | Night Sight
            val photoModes = listOf(PhotoMode.PORTRAIT, PhotoMode.PHOTO, PhotoMode.NIGHT_SIGHT)
            photoModes.forEach { mode ->
                val isSelected = mode == selectedPhotoMode
                ModePillItem(
                    label = mode.title,
                    isSelected = isSelected,
                    onClick = { onSelectPhotoMode(mode) },
                    modifier = Modifier.testTag("mode_${mode.name.lowercase()}")
                )
            }
        } else {
            // VIDEO GROUP: Video | Slow Motion | Cinema
            val videoModes = listOf(VideoMode.VIDEO, VideoMode.SLOW_MOTION, VideoMode.CINEMA)
            videoModes.forEach { mode ->
                val isSelected = mode == selectedVideoMode
                ModePillItem(
                    label = mode.title,
                    isSelected = isSelected,
                    onClick = { onSelectVideoMode(mode) },
                    modifier = Modifier.testTag("mode_${mode.name.lowercase()}")
                )
            }
        }
    }
}

@Composable
private fun ModePillItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = animateColorAsState(
        targetValue = if (isSelected) PeachActivePill else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "mode_pill_bg"
    )
    val textColor = if (isSelected) Color(0xFF111214) else Color.White

    Box(
        modifier = modifier
            .padding(horizontal = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
