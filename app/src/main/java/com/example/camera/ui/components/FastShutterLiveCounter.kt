package com.example.camera.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FastShutterLiveCounter(
    burstCount: Int,
    isBursting: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isBursting && burstCount > 0,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xCC000000), RoundedCornerShape(12.dp))
                .border(1.5.dp, Color(0xFFF6E8DC), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$burstCount BURST",
                color = Color(0xFFF6E8DC),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
