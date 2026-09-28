package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.camera.Camera2Controller
import com.example.camera.CameraHardwareCapabilities
import com.example.camera.LevelSensorManager
import com.example.camera.LevelState
import com.example.camera.MediaRepository
import com.example.model.CameraModeGroup
import com.example.model.CapturedMedia
import com.example.model.CinemaLut
import com.example.model.CinemaModeSettings
import com.example.model.FlashMode
import com.example.model.GlobalMoreSettings
import com.example.model.NightModeSettings
import com.example.model.PhotoMode
import com.example.model.PhotoModeSettings
import com.example.model.PortraitModeSettings
import com.example.model.ProSettings
import com.example.model.SlowMotionModeSettings
import com.example.model.VideoMode
import com.example.model.VideoModeSettings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class CameraUiState(
    val modeGroup: CameraModeGroup = CameraModeGroup.PHOTO,
    val photoMode: PhotoMode = PhotoMode.PHOTO,
    val videoMode: VideoMode = VideoMode.VIDEO,
    val zoomFactor: Float = 1.0f,
    val activeZoomPreset: String = "1×",
    val flashMode: FlashMode = FlashMode.OFF,
    val isRecording: Boolean = false,
    val recordingDurationSec: Int = 0,
    val isProcessing: Boolean = false,
    val levelState: LevelState = LevelState(0f, true),
    val capabilities: CameraHardwareCapabilities = CameraHardwareCapabilities(),
    val latestMedia: CapturedMedia? = null,
    val mediaGallery: List<CapturedMedia> = emptyList(),

    // Mode-specific settings (persisted across switches)
    val photoSettings: PhotoModeSettings = PhotoModeSettings(),
    val portraitSettings: PortraitModeSettings = PortraitModeSettings(),
    val videoSettings: VideoModeSettings = VideoModeSettings(),
    val slowMotionSettings: SlowMotionModeSettings = SlowMotionModeSettings(),
    val cinemaSettings: CinemaModeSettings = CinemaModeSettings(),
    val nightSettings: NightModeSettings = NightModeSettings(),
    val globalMoreSettings: GlobalMoreSettings = GlobalMoreSettings(),
    val proSettings: ProSettings = ProSettings(),

    // Sheet / Dialog visibility
    val showModeSettingsSheet: Boolean = false,
    val showMoreSettingsScreen: Boolean = false,
    val showProSheet: Boolean = false,
    val selectedMediaForViewer: CapturedMedia? = null,
    val isFrontFacing: Boolean = false,
    val liveHistogram: FloatArray = FloatArray(64)
)

sealed class CameraUiEvent {
    object ShutterFlash : CameraUiEvent()
    data class ShowToast(val message: String) : CameraUiEvent()
}

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    val mediaRepository = MediaRepository(application)
    val levelSensorManager = LevelSensorManager(application)
    val cameraController = Camera2Controller(application, viewModelScope, mediaRepository)

    // Saved state for each group (Requirement 3: remember the last selected mode in each group)
    private var lastSelectedPhotoMode: PhotoMode = PhotoMode.PHOTO
    private var lastSelectedVideoMode: VideoMode = VideoMode.VIDEO

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CameraUiEvent>()
    val events: SharedFlow<CameraUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            mediaRepository.initialize()
        }

        // Observe MediaRepository
        viewModelScope.launch {
            mediaRepository.mediaList.collect { list ->
                _uiState.value = _uiState.value.copy(
                    mediaGallery = list,
                    latestMedia = list.firstOrNull()
                )
            }
        }

        // Observe Level Sensor
        viewModelScope.launch {
            levelSensorManager.levelState.collect { level ->
                _uiState.value = _uiState.value.copy(levelState = level)
            }
        }

        // Observe Controller State & Live Histogram
        viewModelScope.launch {
            combine(
                cameraController.isRecording,
                cameraController.recordingSeconds,
                cameraController.isProcessing,
                cameraController.capabilities,
                cameraController.liveHistogram
            ) { rec, sec, proc, caps, histo ->
                val currentSettings = _uiState.value.globalMoreSettings
                val updatedSettings = currentSettings.copy(
                    availableLenses = caps.availableLensesDescription,
                    ultraWideLensReport = caps.ultraWideStatusDescription,
                    oisSupported = caps.supportsOis,
                    rawDngSupported = caps.supportsRawDng
                )
                _uiState.value = _uiState.value.copy(
                    isRecording = rec,
                    recordingDurationSec = sec,
                    isProcessing = proc,
                    capabilities = caps,
                    liveHistogram = histo,
                    globalMoreSettings = updatedSettings
                )
            }.collect {}
        }
    }

    fun startCamera() {
        levelSensorManager.startListening()
        cameraController.start()
    }

    fun stopCamera() {
        levelSensorManager.stopListening()
        cameraController.stop()
    }

    // ==========================================
    // GROUP SWITCHING (Crucial Requirement 2, 3 & 5)
    // ==========================================

    fun selectModeGroup(group: CameraModeGroup) {
        if (_uiState.value.modeGroup == group) return

        if (group == CameraModeGroup.PHOTO) {
            _uiState.value = _uiState.value.copy(
                modeGroup = CameraModeGroup.PHOTO,
                photoMode = lastSelectedPhotoMode
            )
            cameraController.setModeGroup(CameraModeGroup.PHOTO)
            cameraController.setPhotoMode(lastSelectedPhotoMode)
        } else {
            _uiState.value = _uiState.value.copy(
                modeGroup = CameraModeGroup.VIDEO,
                videoMode = lastSelectedVideoMode
            )
            cameraController.setModeGroup(CameraModeGroup.VIDEO)
            cameraController.setVideoMode(lastSelectedVideoMode)
        }
    }

    fun selectPhotoMode(mode: PhotoMode) {
        lastSelectedPhotoMode = mode
        _uiState.value = _uiState.value.copy(photoMode = mode)
        cameraController.setPhotoMode(mode)
    }

    fun selectVideoMode(mode: VideoMode) {
        lastSelectedVideoMode = mode
        _uiState.value = _uiState.value.copy(videoMode = mode)
        cameraController.setVideoMode(mode)
    }

    // ==========================================
    // ZOOM CONTROL (Real ultra-wide, 1×, 2×)
    // ==========================================

    fun setZoomPreset(preset: String) {
        when {
            preset.startsWith("0.") || preset == "UW" -> {
                if (cameraController.hasPhysicalUltraWide) {
                    cameraController.switchToUltraWide()
                    _uiState.value = _uiState.value.copy(
                        zoomFactor = cameraController.ultraWideZoomRatio,
                        activeZoomPreset = preset
                    )
                } else {
                    viewModelScope.launch {
                        _events.emit(CameraUiEvent.ShowToast("Ultra-wide lens is not available on this device"))
                    }
                }
            }
            preset == "2" || preset == "2x" || preset == "2×" || preset == "2.0" -> {
                cameraController.switchToMainWide()
                cameraController.setZoom(2.0f)
                _uiState.value = _uiState.value.copy(
                    zoomFactor = 2.0f,
                    activeZoomPreset = "2×"
                )
            }
            else -> {
                // 1× Main Camera
                cameraController.switchToMainWide()
                cameraController.setZoom(1.0f)
                _uiState.value = _uiState.value.copy(
                    zoomFactor = 1.0f,
                    activeZoomPreset = "1×"
                )
            }
        }
    }

    fun setCustomZoom(factor: Float) {
        val clamped = factor.coerceIn(0.6f, 8.0f)
        val preset = when {
            clamped <= 0.85f -> if (cameraController.hasPhysicalUltraWide) "0.6×" else "1×"
            clamped in 0.86f..1.4f -> "1×"
            clamped >= 1.8f && clamped <= 2.3f -> "2×"
            else -> "${(clamped * 10).toInt() / 10f}×"
        }
        _uiState.value = _uiState.value.copy(
            zoomFactor = clamped,
            activeZoomPreset = preset
        )
        cameraController.setZoom(clamped)
    }

    // ==========================================
    // SHUTTER & RECORD ACTIONS
    // ==========================================

    fun onShutterClicked() {
        if (_uiState.value.modeGroup == CameraModeGroup.PHOTO) {
            viewModelScope.launch {
                _events.emit(CameraUiEvent.ShutterFlash)
            }
            cameraController.capturePhoto { media ->
                _uiState.value = _uiState.value.copy(
                    latestMedia = media
                )
            }
        } else {
            cameraController.toggleVideoRecording { media ->
                _uiState.value = _uiState.value.copy(
                    latestMedia = media
                )
            }
        }
    }

    fun toggleCameraFacing() {
        val newFacing = !_uiState.value.isFrontFacing
        _uiState.value = _uiState.value.copy(isFrontFacing = newFacing)
        cameraController.toggleCameraFacing()
    }

    fun toggleFlash() {
        val nextFlash = when (_uiState.value.flashMode) {
            FlashMode.OFF -> FlashMode.AUTO
            FlashMode.AUTO -> FlashMode.ON
            FlashMode.ON -> FlashMode.OFF
            FlashMode.TORCH -> FlashMode.OFF
        }
        _uiState.value = _uiState.value.copy(flashMode = nextFlash)
        cameraController.setFlashMode(nextFlash)
    }

    fun toggle4kResolution() {
        val current4k = _uiState.value.proSettings.resolution4k
        val updated = _uiState.value.proSettings.copy(resolution4k = !current4k)
        updateProSettings(updated)
    }

    fun updateProSettings(settings: ProSettings) {
        _uiState.value = _uiState.value.copy(proSettings = settings)
        cameraController.updateProSettings(settings)
    }

    // ==========================================
    // MODE-SPECIFIC SETTINGS UPDATES
    // ==========================================

    fun updatePhotoSettings(settings: PhotoModeSettings) {
        _uiState.value = _uiState.value.copy(photoSettings = settings)
        cameraController.photoSettings = settings
        cameraController.updatePreview()
    }

    fun updatePortraitSettings(settings: PortraitModeSettings) {
        _uiState.value = _uiState.value.copy(portraitSettings = settings)
        cameraController.portraitSettings = settings
        cameraController.updatePreview()
    }

    fun updateVideoSettings(settings: VideoModeSettings) {
        _uiState.value = _uiState.value.copy(videoSettings = settings)
        cameraController.videoSettings = settings
        cameraController.updatePreview()
    }

    fun updateSlowMotionSettings(settings: SlowMotionModeSettings) {
        _uiState.value = _uiState.value.copy(slowMotionSettings = settings)
        cameraController.slowMotionSettings = settings
        cameraController.updatePreview()
    }

    fun updateCinemaSettings(settings: CinemaModeSettings) {
        _uiState.value = _uiState.value.copy(cinemaSettings = settings)
        cameraController.cinemaSettings = settings
        cameraController.updatePreview()
    }

    fun updateNightSettings(settings: NightModeSettings) {
        _uiState.value = _uiState.value.copy(nightSettings = settings)
        cameraController.nightSettings = settings
        cameraController.updatePreview()
    }

    fun updateGlobalMoreSettings(settings: GlobalMoreSettings) {
        _uiState.value = _uiState.value.copy(globalMoreSettings = settings)
    }

    // ==========================================
    // SHEET & DIALOG NAVIGATION
    // ==========================================

    fun openModeSettings() {
        _uiState.value = _uiState.value.copy(showModeSettingsSheet = true)
    }

    fun closeModeSettings() {
        _uiState.value = _uiState.value.copy(showModeSettingsSheet = false)
    }

    fun openMoreSettingsScreen() {
        _uiState.value = _uiState.value.copy(showMoreSettingsScreen = true)
    }

    fun closeMoreSettingsScreen() {
        _uiState.value = _uiState.value.copy(showMoreSettingsScreen = false)
    }

    fun openProSheet() {
        _uiState.value = _uiState.value.copy(showProSheet = true)
    }

    fun closeProSheet() {
        _uiState.value = _uiState.value.copy(showProSheet = false)
    }

    fun openMediaViewer(media: CapturedMedia) {
        _uiState.value = _uiState.value.copy(selectedMediaForViewer = media)
    }

    fun closeMediaViewer() {
        _uiState.value = _uiState.value.copy(selectedMediaForViewer = null)
    }

    fun deleteMedia(media: CapturedMedia) {
        viewModelScope.launch {
            mediaRepository.deleteMedia(media)
            if (_uiState.value.selectedMediaForViewer?.id == media.id) {
                _uiState.value = _uiState.value.copy(
                    selectedMediaForViewer = _uiState.value.mediaGallery.firstOrNull { it.id != media.id }
                )
            }
        }
    }
}
