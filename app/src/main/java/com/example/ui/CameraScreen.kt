package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CameraModeGroup
import com.example.model.PhotoMode
import com.example.model.VideoMode
import com.example.ui.components.BottomControlBar
import com.example.ui.components.GalleryViewerDialog
import com.example.ui.components.ModeSelectorRow
import com.example.ui.components.PeachActivePill
import com.example.ui.components.ProTuningSheet
import com.example.ui.components.ShutterControlsRow
import com.example.ui.components.TopControlBar
import com.example.ui.components.ViewfinderComponent
import com.example.ui.components.settings.CinemaModeSettingsSheet
import com.example.ui.components.settings.MoreSettingsScreen
import com.example.ui.components.settings.NightModeSettingsSheet
import com.example.ui.components.settings.PhotoModeSettingsSheet
import com.example.ui.components.settings.PortraitModeSettingsSheet
import com.example.ui.components.settings.SlowMotionModeSettingsSheet
import com.example.ui.components.settings.VideoModeSettingsSheet

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasCameraPermission = perms[Manifest.permission.CAMERA] == true
        if (hasCameraPermission) {
            viewModel.startCamera()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        } else {
            viewModel.startCamera()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopCamera()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("camera_screen_root")
    ) {
        if (!hasCameraPermission) {
            // Permission request fallback card
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera Permission Required",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Allow access to the camera to take high-resolution photos and record videos.",
                    color = Color(0xFFA0A3A8),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PeachActivePill)
                ) {
                    Text(text = "Grant Permission", color = Color(0xFF141618), fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Main Camera UI matching the reference screenshots
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Top Control Bar (4K badge, Recording timer, Flash)
                TopControlBar(
                    modeGroup = uiState.modeGroup,
                    isRecording = uiState.isRecording,
                    recordingSeconds = uiState.recordingDurationSec,
                    is4k = uiState.proSettings.resolution4k,
                    flashMode = uiState.flashMode,
                    onToggle4k = { viewModel.toggle4kResolution() },
                    onToggleFlash = { viewModel.toggleFlash() }
                )

                // 2. Camera Viewfinder (Matches reference image: full layout without side black bars)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val isVideoGroup = uiState.modeGroup == CameraModeGroup.VIDEO

                    val isGridOn = when (uiState.modeGroup) {
                        CameraModeGroup.PHOTO -> {
                            when (uiState.photoMode) {
                                PhotoMode.PHOTO -> uiState.photoSettings.gridEnabled
                                PhotoMode.PORTRAIT -> uiState.portraitSettings.gridEnabled
                                PhotoMode.NIGHT_SIGHT -> uiState.nightSettings.gridEnabled
                            }
                        }
                        CameraModeGroup.VIDEO -> {
                            when (uiState.videoMode) {
                                VideoMode.VIDEO -> uiState.videoSettings.gridEnabled
                                VideoMode.SLOW_MOTION -> false
                                VideoMode.CINEMA -> false
                            }
                        }
                    }

                    val isLevelOn = when (uiState.modeGroup) {
                        CameraModeGroup.PHOTO -> {
                            when (uiState.photoMode) {
                                PhotoMode.PHOTO -> uiState.photoSettings.levelEnabled
                                PhotoMode.PORTRAIT -> false
                                PhotoMode.NIGHT_SIGHT -> uiState.nightSettings.levelEnabled
                            }
                        }
                        CameraModeGroup.VIDEO -> {
                            when (uiState.videoMode) {
                                VideoMode.VIDEO -> uiState.videoSettings.levelEnabled
                                VideoMode.SLOW_MOTION -> false
                                VideoMode.CINEMA -> uiState.cinemaSettings.horizonLevelEnabled
                            }
                        }
                    }

                    ViewfinderComponent(
                        cameraController = viewModel.cameraController,
                        levelState = uiState.levelState,
                        activeZoomPreset = uiState.activeZoomPreset,
                        hasUltraWide = uiState.capabilities.hasUltraWideLens,
                        ultraWideLabel = "0.6×",
                        isVideoMode = isVideoGroup,
                        currentVideoMode = uiState.videoMode,
                        cinemaSettings = uiState.cinemaSettings,
                        liveHistogram = uiState.liveHistogram,
                        isProcessingNight = uiState.isProcessing && uiState.photoMode == PhotoMode.NIGHT_SIGHT,
                        isLevelEnabled = isLevelOn,
                        isGridEnabled = isGridOn,
                        events = viewModel.events,
                        onSelectZoomPreset = { preset -> viewModel.setZoomPreset(preset) },
                        onZoomFactorChanged = { factor -> viewModel.setCustomZoom(factor) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 3. Lower Camera Controls Area (Pure Black Background)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Shutter, Gallery thumbnail, Camera Flip
                    ShutterControlsRow(
                        modeGroup = uiState.modeGroup,
                        isRecording = uiState.isRecording,
                        isProcessing = uiState.isProcessing,
                        latestMedia = uiState.latestMedia,
                        onShutterClick = { viewModel.onShutterClicked() },
                        onGalleryClick = {
                            uiState.latestMedia?.let { viewModel.openMediaViewer(it) }
                        },
                        onFlipCameraClick = { viewModel.toggleCameraFacing() }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Mode Selector Row
                    ModeSelectorRow(
                        modeGroup = uiState.modeGroup,
                        selectedPhotoMode = uiState.photoMode,
                        selectedVideoMode = uiState.videoMode,
                        onSelectPhotoMode = { mode -> viewModel.selectPhotoMode(mode) },
                        onSelectVideoMode = { mode -> viewModel.selectVideoMode(mode) }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bottom Control Bar (Settings | Two-Icon Group Selector: Photo & Video | Pro Tune)
                    BottomControlBar(
                        selectedGroup = uiState.modeGroup,
                        onSelectGroup = { group -> viewModel.selectModeGroup(group) },
                        onOpenSettings = { viewModel.openModeSettings() },
                        onOpenTune = { viewModel.openProSheet() }
                    )
                }
            }
        }

        // ==========================================
        // DEDICATED MODE-SPECIFIC SETTINGS SHEETS (Requirements 1-9)
        // ==========================================
        if (uiState.showModeSettingsSheet) {
            when (uiState.modeGroup) {
                CameraModeGroup.PHOTO -> {
                    when (uiState.photoMode) {
                        PhotoMode.PHOTO -> {
                            PhotoModeSettingsSheet(
                                settings = uiState.photoSettings,
                                onUpdateSettings = { viewModel.updatePhotoSettings(it) },
                                onOpenMoreSettings = { viewModel.openMoreSettingsScreen() },
                                onDismiss = { viewModel.closeModeSettings() }
                            )
                        }
                        PhotoMode.PORTRAIT -> {
                            PortraitModeSettingsSheet(
                                settings = uiState.portraitSettings,
                                onUpdateSettings = { viewModel.updatePortraitSettings(it) },
                                onOpenMoreSettings = { viewModel.openMoreSettingsScreen() },
                                onDismiss = { viewModel.closeModeSettings() }
                            )
                        }
                        PhotoMode.NIGHT_SIGHT -> {
                            NightModeSettingsSheet(
                                settings = uiState.nightSettings,
                                onUpdateSettings = { viewModel.updateNightSettings(it) },
                                onOpenMoreSettings = { viewModel.openMoreSettingsScreen() },
                                onDismiss = { viewModel.closeModeSettings() }
                            )
                        }
                    }
                }
                CameraModeGroup.VIDEO -> {
                    when (uiState.videoMode) {
                        VideoMode.VIDEO -> {
                            VideoModeSettingsSheet(
                                settings = uiState.videoSettings,
                                onUpdateSettings = { viewModel.updateVideoSettings(it) },
                                onOpenMoreSettings = { viewModel.openMoreSettingsScreen() },
                                onDismiss = { viewModel.closeModeSettings() }
                            )
                        }
                        VideoMode.SLOW_MOTION -> {
                            SlowMotionModeSettingsSheet(
                                settings = uiState.slowMotionSettings,
                                onUpdateSettings = { viewModel.updateSlowMotionSettings(it) },
                                onOpenMoreSettings = { viewModel.openMoreSettingsScreen() },
                                onDismiss = { viewModel.closeModeSettings() }
                            )
                        }
                        VideoMode.CINEMA -> {
                            CinemaModeSettingsSheet(
                                settings = uiState.cinemaSettings,
                                onUpdateSettings = { viewModel.updateCinemaSettings(it) },
                                onOpenMoreSettings = { viewModel.openMoreSettingsScreen() },
                                onDismiss = { viewModel.closeModeSettings() }
                            )
                        }
                    }
                }
            }
        }

        // Common Full More Settings Page (Requirement 10)
        if (uiState.showMoreSettingsScreen) {
            MoreSettingsScreen(
                settings = uiState.globalMoreSettings,
                onUpdateSettings = { viewModel.updateGlobalMoreSettings(it) },
                onDismiss = { viewModel.closeMoreSettingsScreen() }
            )
        }

        // Pro Tuning Bottom Sheet
        if (uiState.showProSheet) {
            ProTuningSheet(
                proSettings = uiState.proSettings,
                isCinemaMode = uiState.modeGroup == CameraModeGroup.VIDEO && uiState.videoMode == VideoMode.CINEMA,
                onUpdateSettings = { viewModel.updateProSettings(it) },
                onDismiss = { viewModel.closeProSheet() }
            )
        }

        // Full Screen Gallery Media Viewer
        uiState.selectedMediaForViewer?.let { media ->
            GalleryViewerDialog(
                media = media,
                onDismiss = { viewModel.closeMediaViewer() },
                onDelete = { viewModel.deleteMedia(it) }
            )
        }
    }
}
