package com.example.ui.components

import android.graphics.Matrix
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.camera.Camera2Controller
import com.example.camera.LevelState
import com.example.model.CinemaModeSettings
import com.example.model.VideoMode
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun ViewfinderComponent(
    cameraController: Camera2Controller,
    levelState: LevelState,
    activeZoomPreset: String,
    hasUltraWide: Boolean = false,
    ultraWideLabel: String = "0.6×",
    isVideoMode: Boolean,
    currentVideoMode: VideoMode,
    cinemaSettings: CinemaModeSettings,
    liveHistogram: FloatArray,
    isProcessingNight: Boolean,
    isLevelEnabled: Boolean,
    isGridEnabled: Boolean,
    events: SharedFlow<com.example.ui.CameraUiEvent>,
    onSelectZoomPreset: (String) -> Unit,
    onZoomFactorChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val shutterFlashAlpha = remember { Animatable(0f) }

    LaunchedEffect(events) {
        events.collect { event ->
            if (event is com.example.ui.CameraUiEvent.ShutterFlash) {
                shutterFlashAlpha.snapTo(0.85f)
                shutterFlashAlpha.animateTo(0f, animationSpec = tween(150))
            }
        }
    }

    var currentZoomScale by remember { mutableFloatStateOf(1f) }

    // Outer Viewfinder Box: Rounded corners (24.dp)
    // Occupies the available screen area cleanly without distortion or side black bars
    Box(
        modifier = modifier
            .testTag("camera_viewfinder_container")
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0C0D0F))
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    currentZoomScale = (currentZoomScale * zoom).coerceIn(0.6f, 8.0f)
                    onZoomFactorChanged(currentZoomScale)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // TextureView Camera Preview with Center-Cropping (Zero Distortion & Zero Black Side Bars)
        AndroidView(
            factory = { context ->
                TextureView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // Configure center-crop transform to guarantee no stretching/distortion
                    addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
                        val viewW = (right - left).toFloat()
                        val viewH = (bottom - top).toFloat()
                        if (viewW > 0 && viewH > 0) {
                            val matrix = Matrix()
                            val viewAspect = viewW / viewH
                            val bufferAspect = if (isVideoMode) (9f / 16f) else (3f / 4f)
                            var scaleX = 1f
                            var scaleY = 1f
                            if (viewAspect > bufferAspect) {
                                scaleY = viewAspect / bufferAspect
                            } else {
                                scaleX = bufferAspect / viewAspect
                            }
                            matrix.setScale(scaleX, scaleY, viewW / 2f, viewH / 2f)
                            setTransform(matrix)
                        }
                    }

                    cameraController.attachTextureView(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Composition Grid (Rule of Thirds)
        if (isGridEnabled) {
            CameraGridOverlay(modifier = Modifier.fillMaxSize())
        }

        // Horizon Leveling Indicator (0° yellow line)
        if (isLevelEnabled) {
            LevelIndicator(
                levelState = levelState,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Cinema Framing Guides (2.39:1 Anamorphic / 1.85:1 Flat)
        if (isVideoMode && currentVideoMode == VideoMode.CINEMA && cinemaSettings.framingGuidesEnabled) {
            CinemaGuidesOverlay(
                maskFactor = cinemaSettings.aspectRatio.maskFactor,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Cinema Live Histogram Monitoring Overlay
        if (isVideoMode && currentVideoMode == VideoMode.CINEMA && cinemaSettings.histogramEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x99000000))
                    .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                HistogramChart(
                    histogram = liveHistogram,
                    modifier = Modifier.size(width = 80.dp, height = 40.dp)
                )
            }
        }

        // Night Sight processing banner
        AnimatedVisibility(
            visible = isProcessingNight,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Hold still... Capturing light",
                    color = PeachActivePill,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Shutter Flash Animation overlay
        if (shutterFlashAlpha.value > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = shutterFlashAlpha.value))
            )
        }

        // Zoom Selector: Floating near the bottom of the viewfinder
        ZoomSelector(
            activePreset = activeZoomPreset,
            hasUltraWide = hasUltraWide,
            ultraWideLabel = ultraWideLabel,
            onPresetSelected = onSelectZoomPreset,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun HistogramChart(histogram: FloatArray, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val barCount = histogram.size
        if (barCount == 0) return@Canvas
        val barWidth = w / barCount

        val path = Path()
        path.moveTo(0f, h)

        for (i in 0 until barCount) {
            val normalized = histogram[i].coerceIn(0f, 1f)
            val y = h - (normalized * h)
            val x = i * barWidth
            path.lineTo(x, y)
        }
        path.lineTo(w, h)
        path.close()

        drawPath(path, color = Color(0xB3FFFFFF), style = Fill)
    }
}

@Composable
fun CameraGridOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val gridColor = Color(0x33FFFFFF)
        val stroke = 1.dp.toPx()

        // 2 vertical lines
        drawLine(gridColor, Offset(w / 3f, 0f), Offset(w / 3f, h), stroke)
        drawLine(gridColor, Offset(2f * w / 3f, 0f), Offset(2f * w / 3f, h), stroke)

        // 2 horizontal lines
        drawLine(gridColor, Offset(0f, h / 3f), Offset(w, h / 3f), stroke)
        drawLine(gridColor, Offset(0f, 2f * h / 3f), Offset(w, 2f * h / 3f), stroke)
    }
}

@Composable
fun CinemaGuidesOverlay(maskFactor: Float, modifier: Modifier = Modifier) {
    if (maskFactor <= 0.01f) return
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val letterboxH = h * maskFactor

        // Top mask
        drawRect(
            color = Color(0xCC000000),
            topLeft = Offset(0f, 0f),
            size = Size(w, letterboxH)
        )
        // Bottom mask
        drawRect(
            color = Color(0xCC000000),
            topLeft = Offset(0f, h - letterboxH),
            size = Size(w, letterboxH)
        )
        // White boundary lines
        drawLine(
            color = Color(0x66FFFFFF),
            start = Offset(0f, letterboxH),
            end = Offset(w, letterboxH),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color(0x66FFFFFF),
            start = Offset(0f, h - letterboxH),
            end = Offset(w, h - letterboxH),
            strokeWidth = 1.dp.toPx()
        )
    }
}
