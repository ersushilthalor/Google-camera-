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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CameraModeGroup

val DarkButtonBg = Color(0xFF222428)
val DarkPillContainerBg = Color(0xFF1E2024)

@Composable
fun BottomControlBar(
    selectedGroup: CameraModeGroup,
    onSelectGroup: (CameraModeGroup) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTune: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Settings button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(DarkButtonBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenSettings
                )
                .testTag("bottom_settings_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.camera_settings),
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Center: Dual Mode Selector (Photo Camera | Video Camera)
        // Two separate clickable buttons inside a pill container as shown in screenshots
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(26.dp))
                .background(DarkPillContainerBg)
                .padding(4.dp)
                .testTag("center_mode_group_container")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // LEFT BUTTON: PHOTO CAMERA ICON
                val isPhotoActive = selectedGroup == CameraModeGroup.PHOTO
                val photoBgColor = animateColorAsState(
                    targetValue = if (isPhotoActive) PeachActivePill else Color.Transparent,
                    animationSpec = tween(durationMillis = 200),
                    label = "photo_bg"
                )
                val photoIconTint = if (isPhotoActive) Color(0xFF1A1C1E) else Color.White

                Box(
                    modifier = Modifier
                        .size(width = 54.dp, height = 44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(photoBgColor.value)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectGroup(CameraModeGroup.PHOTO) }
                        )
                        .testTag("bottom_photo_group_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Photo Mode",
                        tint = photoIconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // RIGHT BUTTON: VIDEO CAMERA ICON
                val isVideoActive = selectedGroup == CameraModeGroup.VIDEO
                val videoBgColor = animateColorAsState(
                    targetValue = if (isVideoActive) PeachActivePill else Color.Transparent,
                    animationSpec = tween(durationMillis = 200),
                    label = "video_bg"
                )
                val videoIconTint = if (isVideoActive) Color(0xFF1A1C1E) else Color.White

                Box(
                    modifier = Modifier
                        .size(width = 54.dp, height = 44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(videoBgColor.value)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectGroup(CameraModeGroup.VIDEO) }
                        )
                        .testTag("bottom_video_group_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Videocam,
                        contentDescription = "Video Mode",
                        tint = videoIconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Right: Pro Tune / Filters button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(DarkButtonBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenTune
                )
                .testTag("bottom_tune_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Tune,
                contentDescription = stringResource(R.string.camera_tuning),
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
