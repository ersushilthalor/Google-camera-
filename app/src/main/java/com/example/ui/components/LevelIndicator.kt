package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.LevelState
import kotlin.math.abs

val GoldenLevelColor = Color(0xFFFBC02D)
val WhiteLevelColor = Color(0xB3FFFFFF)

@Composable
fun LevelIndicator(
    levelState: LevelState,
    modifier: Modifier = Modifier
) {
    val isLevel = levelState.isLevel || abs(levelState.rollAngle) < 1.0f
    val lineColor = if (isLevel) GoldenLevelColor else WhiteLevelColor
    val angleText = if (isLevel) "0°" else "${levelState.rollAngle.toInt()}°"

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val lineWidth = size.width * 0.38f
            val gapRadius = 24.dp.toPx()

            // Left horizontal line
            drawLine(
                color = lineColor,
                start = Offset(cx - lineWidth, cy),
                end = Offset(cx - gapRadius, cy),
                strokeWidth = 2.dp.toPx()
            )

            // Right horizontal line
            drawLine(
                color = lineColor,
                start = Offset(cx + gapRadius, cy),
                end = Offset(cx + lineWidth, cy),
                strokeWidth = 2.dp.toPx()
            )

            // Center vertical tick
            drawLine(
                color = lineColor,
                start = Offset(cx, cy - 6.dp.toPx()),
                end = Offset(cx, cy + 6.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Degree readout right above center crosshair (matching screenshot with 0°)
        Text(
            text = angleText,
            color = lineColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(y = (-14).dp)
        )
    }
}
