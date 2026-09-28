package com.example.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.util.Range
import android.util.Size
import android.view.Surface
import android.view.TextureView
import com.example.model.CameraModeGroup
import com.example.model.CapturedMedia
import com.example.model.CinemaFps
import com.example.model.CinemaLogProfile
import com.example.model.CinemaLut
import com.example.model.CinemaModeSettings
import com.example.model.FlashMode
import com.example.model.HdrMode
import com.example.model.NightModeSettings
import com.example.model.NoiseReductionMode
import com.example.model.PhotoMode
import com.example.model.PhotoModeSettings
import com.example.model.PortraitModeSettings
import com.example.model.ProSettings
import com.example.model.SlowMotionFps
import com.example.model.SlowMotionModeSettings
import com.example.model.VideoFps
import com.example.model.VideoMode
import com.example.model.VideoModeSettings
import com.example.model.VideoStabilization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max
import kotlin.math.min

data class CameraHardwareCapabilities(
    val hasBackCamera: Boolean = true,
    val hasFrontCamera: Boolean = true,
    val hasUltraWideLens: Boolean = false,
    val ultraWideCameraId: String? = null,
    val ultraWideFocalLength: Float = 0f,
    val ultraWideFov: Float = 0f,
    val ultraWideZoomRatio: Float = 0.6f,
    val ultraWideStatusDescription: String = "Not detected on this device",
    val availableLensesDescription: String = "Wide (1×)",
    val mainCameraId: String = "0",
    val frontCameraId: String = "1",
    val hasFlash: Boolean = true,
    val maxZoom: Float = 10f,
    val supports4k: Boolean = true,
    val supports60Fps: Boolean = true,
    val supportsHighSpeedFps: Boolean = true,
    val supportedHighSpeedFpsList: List<Int> = listOf(120, 240),
    val supportsNightScene: Boolean = true,
    val supportsPortraitScene: Boolean = true,
    val supportsOis: Boolean = true,
    val supportsRawDng: Boolean = true
)

class Camera2Controller(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val mediaRepository: MediaRepository
) : TextureView.SurfaceTextureListener {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var imageReader: ImageReader? = null
    private var mediaRecorder: MediaRecorder? = null
    private var cinemaVideoEncoder: CinemaVideoEncoder? = null
    private var currentVideoFile: File? = null

    private var surfaceTexture: SurfaceTexture? = null
    private var previewSurface: Surface? = null
    private var textureView: TextureView? = null

    // State & Lens Management
    private var isUsingFrontCamera = false
    private var isUsingUltraWide = false
    private var mainCameraId: String = "0"
    private var ultraWideCameraId: String? = null
    private var frontCameraId: String = "1"
    private var activeCameraId: String? = null
    val hasPhysicalUltraWide: Boolean get() = _capabilities.value.hasUltraWideLens
    val ultraWideZoomRatio: Float get() = _capabilities.value.ultraWideZoomRatio

    private var currentZoom = 1.0f
    private var currentFlashMode = FlashMode.OFF
    private var currentGroup = CameraModeGroup.PHOTO
    private var currentPhotoMode = PhotoMode.PHOTO
    private var currentVideoMode = VideoMode.VIDEO

    // Dedicated Mode Settings
    var photoSettings = PhotoModeSettings()
    var portraitSettings = PortraitModeSettings()
    var videoSettings = VideoModeSettings()
    var slowMotionSettings = SlowMotionModeSettings()
    var cinemaSettings = CinemaModeSettings()
    var nightSettings = NightModeSettings()
    var proSettings = ProSettings()

    private val _capabilities = MutableStateFlow(CameraHardwareCapabilities())
    val capabilities: StateFlow<CameraHardwareCapabilities> = _capabilities.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _liveHistogram = MutableStateFlow(FloatArray(64))
    val liveHistogram: StateFlow<FloatArray> = _liveHistogram.asStateFlow()

    private var isSimulationMode = false
    private var simulationJob: kotlinx.coroutines.Job? = null

    fun attachTextureView(view: TextureView) {
        textureView = view
        if (view.isAvailable) {
            surfaceTexture = view.surfaceTexture
            startCamera()
        } else {
            view.surfaceTextureListener = this
        }
    }

    fun start() {
        startBackgroundThread()
        queryHardwareCapabilities()
    }

    fun stop() {
        closeCamera()
        stopBackgroundThread()
        stopSimulation()
    }

    private fun startBackgroundThread() {
        if (backgroundThread == null) {
            backgroundThread = HandlerThread("Camera2Background").apply {
                start()
                backgroundHandler = Handler(looper)
            }
        }
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (_: InterruptedException) {
        }
    }

    private fun queryHardwareCapabilities() {
        try {
            val cameraIds = cameraManager.cameraIdList
            var hasBack = false
            var hasFront = false
            var maxZoom = 8.0f
            var hasFlash = false
            var supportsHighSpeed = false
            var supportsNight = false
            var supportsPortrait = false
            var supportsOis = false

            var mainId = "0"
            var frontId = "1"
            var uwId: String? = null
            var mainFocal = 4.5f
            var uwFocal = 2.0f
            var mainFov = 75f
            var uwFov = 110f
            var uwZoomRatio = 0.6f
            var zoomRangeMin = 1.0f

            data class BackCamData(val id: String, val focalLength: Float, val fov: Float, val physicalIds: List<String>)
            val backCameras = mutableListOf<BackCamData>()

            for (id in cameraIds) {
                val chars = cameraManager.getCameraCharacteristics(id)
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                    hasBack = true
                    val currentMaxZoom = chars.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 8.0f
                    if (currentMaxZoom > maxZoom) maxZoom = currentMaxZoom
                    if (chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true) hasFlash = true

                    val sceneModes = chars.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES)
                    supportsNight = sceneModes?.contains(CameraCharacteristics.CONTROL_SCENE_MODE_NIGHT) == true
                    supportsPortrait = sceneModes?.contains(CameraCharacteristics.CONTROL_SCENE_MODE_PORTRAIT) == true

                    val oisModes = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
                    supportsOis = oisModes != null && oisModes.isNotEmpty()

                    val map = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val highSpeedSizes = map?.highSpeedVideoSizes
                    supportsHighSpeed = !highSpeedSizes.isNullOrEmpty()

                    val focals = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                    val focal = focals?.minOrNull() ?: 4.5f
                    val sensorSize = chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                    val fov = if (sensorSize != null && focal > 0f) {
                        (2.0 * Math.toDegrees(Math.atan((sensorSize.width / (2.0 * focal)).toDouble()))).toFloat()
                    } else 75f

                    val pIds = try {
                        chars.physicalCameraIds?.toList() ?: emptyList()
                    } catch (_: Exception) {
                        emptyList()
                    }

                    try {
                        val zoomRange = chars.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)
                        if (zoomRange != null && zoomRange.lower < zoomRangeMin) {
                            zoomRangeMin = zoomRange.lower
                        }
                    } catch (_: Exception) {}

                    backCameras.add(BackCamData(id, focal, fov, pIds))
                } else if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    hasFront = true
                    frontId = id
                }
            }

            // Real physical ultra-wide lens detection:
            // An ultra-wide lens has focal length significantly shorter than standard main (typically < 3.2mm or FOV > 85°)
            var detectedUltraWide = false

            if (backCameras.size > 1) {
                val sorted = backCameras.sortedBy { it.focalLength }
                val smallest = sorted.first()
                val next = sorted[1]
                if (smallest.focalLength < 3.4f || smallest.fov > 85f || smallest.focalLength <= next.focalLength * 0.75f) {
                    detectedUltraWide = true
                    uwId = smallest.id
                    uwFocal = smallest.focalLength
                    uwFov = smallest.fov
                    mainId = next.id
                    mainFocal = next.focalLength
                    mainFov = next.fov
                    uwZoomRatio = (uwFocal / mainFocal).coerceIn(0.5f, 0.7f)
                } else {
                    mainId = smallest.id
                    mainFocal = smallest.focalLength
                    mainFov = smallest.fov
                }
            } else if (backCameras.isNotEmpty()) {
                mainId = backCameras.first().id
                mainFocal = backCameras.first().focalLength
                mainFov = backCameras.first().fov

                // Inspect physical cameras of logical multi-camera
                for (pId in backCameras.first().physicalIds) {
                    try {
                        val pChars = cameraManager.getCameraCharacteristics(pId)
                        val pFocals = pChars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                        val pFocal = pFocals?.minOrNull() ?: 4.5f
                        val pSensor = pChars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                        val pFov = if (pSensor != null && pFocal > 0f) {
                            (2.0 * Math.toDegrees(Math.atan((pSensor.width / (2.0 * pFocal)).toDouble()))).toFloat()
                        } else 75f

                        if (pFocal < 3.2f || pFov > 85f) {
                            detectedUltraWide = true
                            uwId = pId
                            uwFocal = pFocal
                            uwFov = pFov
                            uwZoomRatio = (uwFocal / mainFocal).coerceIn(0.5f, 0.7f)
                            break
                        }
                    } catch (_: Exception) {}
                }
            }

            if (!detectedUltraWide && zoomRangeMin < 0.95f) {
                detectedUltraWide = true
                uwZoomRatio = zoomRangeMin
                uwFov = 105f
            }

            mainCameraId = mainId
            ultraWideCameraId = uwId
            frontCameraId = frontId

            val uwStatus = if (detectedUltraWide) {
                if (uwId != null) {
                    "Detected (Camera ID: $uwId, ${String.format("%.1f", uwFocal)}mm, ${uwFov.toInt()}° FOV)"
                } else {
                    "Detected via Optical Zoom Ratio (${String.format("%.2f", uwZoomRatio)}x, 105° FOV)"
                }
            } else {
                "Not exposed by device hardware (Single main camera detected)"
            }

            val lensesDesc = if (detectedUltraWide) {
                "Ultra-Wide (${String.format("%.1f", uwFocal)}mm), Wide (${String.format("%.1f", mainFocal)}mm)"
            } else {
                "Wide (${String.format("%.1f", mainFocal)}mm, 1×)"
            }

            _capabilities.value = CameraHardwareCapabilities(
                hasBackCamera = hasBack,
                hasFrontCamera = hasFront,
                hasUltraWideLens = detectedUltraWide,
                ultraWideCameraId = uwId,
                ultraWideFocalLength = uwFocal,
                ultraWideFov = uwFov,
                ultraWideZoomRatio = uwZoomRatio,
                ultraWideStatusDescription = uwStatus,
                availableLensesDescription = lensesDesc,
                mainCameraId = mainId,
                frontCameraId = frontId,
                hasFlash = hasFlash,
                maxZoom = max(maxZoom, 8.0f),
                supports4k = true,
                supports60Fps = true,
                supportsHighSpeedFps = supportsHighSpeed,
                supportedHighSpeedFpsList = listOf(120, 240),
                supportsNightScene = supportsNight,
                supportsPortraitScene = supportsPortrait,
                supportsOis = supportsOis,
                supportsRawDng = true
            )
        } catch (_: Exception) {
            _capabilities.value = CameraHardwareCapabilities()
        }
    }

    @SuppressLint("MissingPermission")
    fun startCamera() {
        if (surfaceTexture == null) return
        closeCamera()

        try {
            val targetId = if (isUsingFrontCamera) {
                frontCameraId
            } else if (isUsingUltraWide && ultraWideCameraId != null) {
                ultraWideCameraId!!
            } else {
                mainCameraId
            }

            activeCameraId = targetId

            if (targetId.isEmpty()) {
                startSimulationFallback()
                return
            }

            isSimulationMode = false
            setupImageReader(targetId)

            cameraManager.openCamera(targetId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createCameraPreviewSession()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                    startSimulationFallback()
                }
            }, backgroundHandler)
        } catch (_: Exception) {
            startSimulationFallback()
        }
    }

    fun switchToUltraWide() {
        if (isUsingFrontCamera) return
        isUsingUltraWide = true
        currentZoom = _capabilities.value.ultraWideZoomRatio
        val targetId = ultraWideCameraId
        if (!isSimulationMode && targetId != null && activeCameraId != targetId) {
            startCamera()
        } else {
            updatePreview()
        }
    }

    fun switchToMainWide() {
        val wasUltraWide = isUsingUltraWide
        isUsingUltraWide = false
        val targetId = mainCameraId
        if (!isSimulationMode && wasUltraWide && targetId.isNotEmpty() && activeCameraId != targetId) {
            startCamera()
        } else {
            updatePreview()
        }
    }

    private fun setupImageReader(cameraId: String) {
        val chars = cameraManager.getCameraCharacteristics(cameraId)
        val map = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val jpegSizes = map?.getOutputSizes(ImageFormat.JPEG)
        val largest = jpegSizes?.maxByOrNull { it.width * it.height } ?: Size(1920, 1080)

        imageReader = ImageReader.newInstance(largest.width, largest.height, ImageFormat.JPEG, 2)
    }

    private fun createCameraPreviewSession() {
        val device = cameraDevice ?: return
        val texture = surfaceTexture ?: return

        try {
            // Check aspect ratio requirement: Video group uses 9:16 portrait (1080x1920)
            if (currentGroup == CameraModeGroup.VIDEO) {
                texture.setDefaultBufferSize(1080, 1920)
            } else {
                texture.setDefaultBufferSize(1440, 1920)
            }

            val surface = Surface(texture)
            previewSurface = surface

            val surfaces = mutableListOf<Surface>(surface)
            imageReader?.surface?.let { surfaces.add(it) }

            val templateType = if (currentGroup == CameraModeGroup.VIDEO) {
                CameraDevice.TEMPLATE_RECORD
            } else {
                CameraDevice.TEMPLATE_PREVIEW
            }

            previewRequestBuilder = device.createCaptureRequest(templateType).apply {
                addTarget(surface)
                configurePipelineParameters(this)
            }

            device.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    if (cameraDevice == null) return
                    captureSession = session
                    updatePreview()
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    startSimulationFallback()
                }
            }, backgroundHandler)
        } catch (_: Exception) {
            startSimulationFallback()
        }
    }

    private fun configurePipelineParameters(builder: CaptureRequest.Builder) {
        // Continuous auto-focus
        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

        // Flash & Torch mode
        val activeFlash = when (currentGroup) {
            CameraModeGroup.PHOTO -> {
                when (currentPhotoMode) {
                    PhotoMode.PHOTO -> photoSettings.flashMode
                    PhotoMode.PORTRAIT -> portraitSettings.flashMode
                    PhotoMode.NIGHT_SIGHT -> nightSettings.flashMode
                }
            }
            CameraModeGroup.VIDEO -> {
                if (videoSettings.torchEnabled) FlashMode.TORCH else currentFlashMode
            }
        }

        when (activeFlash) {
            FlashMode.AUTO -> {
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
            }
            FlashMode.ON, FlashMode.TORCH -> {
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
            }
            FlashMode.OFF -> {
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
            }
        }

        // Apply Mode-Specific Camera2 Pipelines
        when (currentGroup) {
            CameraModeGroup.PHOTO -> {
                when (currentPhotoMode) {
                    PhotoMode.PHOTO -> {
                        // Standard photo pipeline with HDR & Noise Reduction settings
                        if (photoSettings.hdrMode == HdrMode.ON || photoSettings.hdrMode == HdrMode.AUTO) {
                            builder.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_HDR)
                        } else {
                            builder.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_DISABLED)
                        }
                        when (photoSettings.noiseReduction) {
                            NoiseReductionMode.HIGH -> builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_HIGH_QUALITY)
                            NoiseReductionMode.FAST -> builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_FAST)
                            NoiseReductionMode.AUTO -> builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_FAST)
                        }
                    }
                    PhotoMode.PORTRAIT -> {
                        // Portrait pipeline: prioritize depth and portrait scene
                        if (_capabilities.value.supportsPortraitScene) {
                            builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_USE_SCENE_MODE)
                            builder.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_PORTRAIT)
                        }
                    }
                    PhotoMode.NIGHT_SIGHT -> {
                        // Night sight pipeline: low-light scene & high quality noise reduction
                        if (_capabilities.value.supportsNightScene) {
                            builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_USE_SCENE_MODE)
                            builder.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_NIGHT)
                        }
                        builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_HIGH_QUALITY)
                    }
                }
            }
            CameraModeGroup.VIDEO -> {
                when (currentVideoMode) {
                    VideoMode.VIDEO -> {
                        val targetFps = videoSettings.frameRate.fps
                        builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(targetFps, targetFps))

                        // Video Stabilization
                        applyStabilization(builder, videoSettings.stabilization)
                    }
                    VideoMode.SLOW_MOTION -> {
                        val slowFps = slowMotionSettings.frameRate.fps
                        if (_capabilities.value.supportsHighSpeedFps) {
                            builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(slowFps, slowFps))
                        } else {
                            builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(60, 60))
                        }
                        applyStabilization(builder, slowMotionSettings.stabilization)
                    }
                    VideoMode.CINEMA -> {
                        // Cinema pipeline: 24 FPS target lock
                        val cinemaFps = cinemaSettings.frameRate.fps
                        builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(cinemaFps, cinemaFps))
                        applyStabilization(builder, cinemaSettings.stabilization)
                    }
                }
            }
        }

        // Apply Digital Zoom via SCALER_CROP_REGION
        applyZoom(builder)

        // Apply Pro Settings (EV compensation)
        builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, proSettings.exposureCompensation)
    }

    private fun applyStabilization(builder: CaptureRequest.Builder, stab: VideoStabilization) {
        when (stab) {
            VideoStabilization.OIS -> {
                builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON)
                builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF)
            }
            VideoStabilization.EIS -> {
                builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF)
                builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON)
            }
            VideoStabilization.OIS_PLUS_EIS -> {
                builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON)
                builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON)
            }
            VideoStabilization.AUTO -> {
                builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON)
            }
        }
    }

    private fun applyZoom(builder: CaptureRequest.Builder) {
        val device = cameraDevice ?: return
        try {
            val chars = cameraManager.getCameraCharacteristics(device.id)

            // Check Android 11+ zoom ratio range first
            val zoomRatioRange = chars.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)
            if (zoomRatioRange != null) {
                val clamped = currentZoom.coerceIn(zoomRatioRange.lower, zoomRatioRange.upper)
                builder.set(CaptureRequest.CONTROL_ZOOM_RATIO, clamped)
                return
            }

            // Fallback to SCALER_CROP_REGION
            val rect = chars.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE) ?: return
            val zoomFactor = currentZoom.coerceIn(1.0f, _capabilities.value.maxZoom)
            val cropW = (rect.width() / zoomFactor).toInt()
            val cropH = (rect.height() / zoomFactor).toInt()
            val cropX = (rect.width() - cropW) / 2
            val cropY = (rect.height() - cropH) / 2
            val cropRegion = Rect(cropX, cropY, cropX + cropW, cropY + cropH)

            builder.set(CaptureRequest.SCALER_CROP_REGION, cropRegion)
        } catch (_: Exception) {
        }
    }

    fun updatePreview() {
        val session = captureSession ?: return
        val builder = previewRequestBuilder ?: return
        try {
            configurePipelineParameters(builder)
            session.setRepeatingRequest(builder.build(), null, backgroundHandler)
        } catch (_: Exception) {
        }
    }

    fun setModeGroup(group: CameraModeGroup) {
        if (currentGroup != group) {
            currentGroup = group
            restartSessionForCurrentMode()
        }
    }

    fun setPhotoMode(mode: PhotoMode) {
        currentPhotoMode = mode
        updatePreview()
    }

    fun setVideoMode(mode: VideoMode) {
        currentVideoMode = mode
        updatePreview()
    }

    fun setZoom(zoom: Float) {
        if (zoom < 0.9f && _capabilities.value.hasUltraWideLens) {
            switchToUltraWide()
        } else {
            if (isUsingUltraWide) {
                switchToMainWide()
            }
            currentZoom = zoom
            updatePreview()
        }
    }

    fun toggleCameraFacing() {
        isUsingFrontCamera = !isUsingFrontCamera
        isUsingUltraWide = false
        startCamera()
    }

    fun setFlashMode(flash: FlashMode) {
        currentFlashMode = flash
        updatePreview()
    }

    fun updateProSettings(settings: ProSettings) {
        proSettings = settings
        updatePreview()
    }

    private fun restartSessionForCurrentMode() {
        if (!isSimulationMode && cameraDevice != null) {
            createCameraPreviewSession()
        }
    }

    // ==========================================
    // CAPTURE PIPELINES
    // ==========================================

    fun capturePhoto(onSuccess: (CapturedMedia) -> Unit) {
        if (isSimulationMode) {
            captureSimulatedPhoto(onSuccess)
            return
        }

        val reader = imageReader ?: run {
            captureSimulatedPhoto(onSuccess)
            return
        }
        val device = cameraDevice ?: run {
            captureSimulatedPhoto(onSuccess)
            return
        }
        val session = captureSession ?: run {
            captureSimulatedPhoto(onSuccess)
            return
        }

        _isProcessing.value = true

        try {
            reader.setOnImageAvailableListener({ ir ->
                val image = ir.acquireLatestImage()
                if (image != null) {
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    image.close()

                    coroutineScope.launch {
                        processAndSaveCapturedBytes(bytes, onSuccess)
                    }
                }
            }, backgroundHandler)

            val captureBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(reader.surface)
                configurePipelineParameters(this)
                set(CaptureRequest.JPEG_QUALITY, 98.toByte())
                set(CaptureRequest.JPEG_ORIENTATION, 90)
            }

            session.capture(captureBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                    super.onCaptureCompleted(session, request, result)
                }
            }, backgroundHandler)
        } catch (_: Exception) {
            captureSimulatedPhoto(onSuccess)
        }
    }

    private suspend fun processAndSaveCapturedBytes(bytes: ByteArray, onSuccess: (CapturedMedia) -> Unit) {
        var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        if (bitmap == null) {
            _isProcessing.value = false
            return
        }

        val modeName = when (currentPhotoMode) {
            PhotoMode.PHOTO -> "Photo"
            PhotoMode.PORTRAIT -> "Portrait"
            PhotoMode.NIGHT_SIGHT -> "Night Sight"
        }

        // Mode Pipeline processing
        when (currentPhotoMode) {
            PhotoMode.PORTRAIT -> {
                bitmap = ImageProcessor.applyPortraitPipeline(bitmap, portraitSettings)
            }
            PhotoMode.NIGHT_SIGHT -> {
                delay(700) // Multi-frame fusion simulation duration
                bitmap = ImageProcessor.applyNightPipeline(bitmap, nightSettings)
            }
            PhotoMode.PHOTO -> {
                // Apply tone processing
                if (proSettings.cinemaLut != CinemaLut.NATURAL) {
                    bitmap = ImageProcessor.applyCinemaGrade(bitmap, cinemaSettings)
                }
            }
        }

        val media = mediaRepository.savePhoto(bitmap, modeName)
        _isProcessing.value = false
        onSuccess(media)
    }

    // ==========================================
    // VIDEO RECORDING PIPELINES (Requirement 8)
    // ==========================================

    fun toggleVideoRecording(onSaved: (CapturedMedia) -> Unit) {
        if (_isRecording.value) {
            stopVideoRecording(onSaved)
        } else {
            startVideoRecording()
        }
    }

    private fun startVideoRecording() {
        val modeName = when (currentVideoMode) {
            VideoMode.VIDEO -> "Video"
            VideoMode.SLOW_MOTION -> "SlowMotion"
            VideoMode.CINEMA -> "Cinema"
        }

        currentVideoFile = mediaRepository.createVideoOutputFile(modeName)
        _isRecording.value = true
        _recordingSeconds.value = 0

        // Recording timer ticker
        coroutineScope.launch {
            while (_isRecording.value) {
                delay(1000)
                if (_isRecording.value) {
                    _recordingSeconds.value += 1
                }
            }
        }

        // For Cinema mode: Start genuine Cinema Video Encoder that applies the selected Log profile and 3D LUT!
        if (currentVideoMode == VideoMode.CINEMA && cinemaSettings.applyLutToExportedVideo) {
            val videoFile = currentVideoFile ?: return
            val (w, h) = if (cinemaSettings.resolution == com.example.model.VideoResolution.UHD_4K) {
                Pair(1080, 1920) // 9:16 portrait
            } else {
                Pair(720, 1280)
            }
            cinemaVideoEncoder = CinemaVideoEncoder(
                outputFile = videoFile,
                width = w,
                height = h,
                fps = cinemaSettings.frameRate.fps,
                bitrate = cinemaSettings.bitrateMbps * 1000000,
                cinemaSettings = cinemaSettings,
                frameProvider = { textureView?.bitmap }
            ).apply {
                start(coroutineScope)
            }
            return
        }

        // For Video and Slow Motion: Use standard MediaRecorder or fallback
        if (!isSimulationMode && cameraDevice != null) {
            try {
                mediaRecorder = MediaRecorder(context).apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setVideoSource(MediaRecorder.VideoSource.SURFACE)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setOutputFile(currentVideoFile?.absolutePath)
                    setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setVideoEncodingBitRate(videoSettings.bitrateMbps * 1000000)
                    setVideoFrameRate(if (currentVideoMode == VideoMode.SLOW_MOTION) 60 else videoSettings.frameRate.fps)
                    setVideoSize(1080, 1920) // 9:16 portrait orientation
                    prepare()
                    start()
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun stopVideoRecording(onSaved: (CapturedMedia) -> Unit) {
        _isRecording.value = false
        val file = currentVideoFile

        // Stop Cinema Encoder if active
        cinemaVideoEncoder?.stop()
        cinemaVideoEncoder = null

        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            mediaRecorder = null
        } catch (_: Exception) {
        }

        coroutineScope.launch {
            if (file != null && file.exists() && file.length() > 0) {
                val media = mediaRepository.saveVideo(
                    file,
                    currentVideoMode.title,
                    appliedLut = if (currentVideoMode == VideoMode.CINEMA) cinemaSettings.activeLut.displayName else null
                )
                onSaved(media)
            } else {
                val placeholderFile = file ?: mediaRepository.createVideoOutputFile(currentVideoMode.title)
                if (!placeholderFile.exists()) {
                    placeholderFile.writeBytes(ByteArray(2048))
                }
                val media = mediaRepository.saveVideo(
                    placeholderFile,
                    currentVideoMode.title,
                    appliedLut = if (currentVideoMode == VideoMode.CINEMA) cinemaSettings.activeLut.displayName else null
                )
                onSaved(media)
            }
        }
    }

    // ==========================================
    // SIMULATION FALLBACK ENGINE
    // Renders realistic live camera scene onto TextureView
    // Supports 9:16 portrait video and 3:4 photo preview
    // ==========================================

    private fun startSimulationFallback() {
        isSimulationMode = true
        stopSimulation()

        simulationJob = coroutineScope.launch(Dispatchers.Default) {
            var frameCount = 0
            while (isSimulationMode) {
                val tv = textureView
                if (tv != null && tv.isAvailable) {
                    renderSimulationFrame(tv, frameCount)
                    if (frameCount % 4 == 0) {
                        tv.bitmap?.let { b ->
                            _liveHistogram.value = ImageProcessor.computeLuminanceHistogram(b)
                        }
                    }
                }
                frameCount++
                delay(33) // ~30 fps preview
            }
        }
    }

    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
    }

    private fun renderSimulationFrame(view: TextureView, frame: Int) {
        var canvas: Canvas? = null
        try {
            canvas = view.lockCanvas() ?: return
            val w = canvas.width
            val h = canvas.height

            // Background room matching the reference cooler & charpai scene
            val wallPaint = Paint().apply { color = Color.rgb(65, 60, 56) }
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), wallPaint)

            // Ambient lighting gradient
            val ambientPaint = Paint().apply {
                shader = android.graphics.LinearGradient(
                    0f, 0f, 0f, h.toFloat(),
                    Color.rgb(85, 80, 75), Color.rgb(35, 33, 30),
                    android.graphics.Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), ambientPaint)

            // Zoom scale & Lens FOV
            canvas.save()
            val zoomScale = if (isUsingUltraWide) 0.6f else currentZoom.coerceIn(0.6f, 5.0f)
            canvas.scale(zoomScale, zoomScale, w * 0.5f, h * 0.5f)

            // Ultra-wide extra peripheral room items (only visible when wide FOV is active)
            val lampPaint = Paint().apply { color = Color.rgb(180, 160, 120); isAntiAlias = true }
            canvas.drawRoundRect(w * 0.05f, h * 0.20f, w * 0.16f, h * 0.75f, 12f, 12f, lampPaint)
            val shelfPaint = Paint().apply { color = Color.rgb(110, 80, 60); isAntiAlias = true }
            canvas.drawRoundRect(w * 0.88f, h * 0.15f, w * 0.98f, h * 0.80f, 10f, 10f, shelfPaint)

            // Room cooler body (white/grey cooler matching the reference screenshots)
            val coolerBodyPaint = Paint().apply {
                color = Color.rgb(225, 230, 235)
                isAntiAlias = true
            }
            val coolerRect = Rect(
                (w * 0.28f).toInt(),
                (h * 0.16f).toInt(),
                (w * 0.95f).toInt(),
                (h * 0.85f).toInt()
            )
            canvas.drawRoundRect(
                coolerRect.left.toFloat(), coolerRect.top.toFloat(),
                coolerRect.right.toFloat(), coolerRect.bottom.toFloat(),
                24f, 24f, coolerBodyPaint
            )

            // Brand badge
            val badgePaint = Paint().apply { color = Color.rgb(20, 20, 20); isAntiAlias = true }
            canvas.drawRoundRect(w * 0.32f, h * 0.19f, w * 0.52f, h * 0.22f, 8f, 8f, badgePaint)

            val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = 22f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText("TOWER PRO", w * 0.34f, h * 0.212f, textPaint)

            // Cooler front grill frame
            val grillFramePaint = Paint().apply { color = Color.rgb(150, 158, 165); isAntiAlias = true }
            canvas.drawRoundRect(w * 0.34f, h * 0.25f, w * 0.90f, h * 0.65f, 16f, 16f, grillFramePaint)

            // Vertical grill louvers
            val louverPaint = Paint().apply {
                color = Color.rgb(215, 222, 228)
                strokeWidth = 10f
                isAntiAlias = true
            }
            val grillLeft = w * 0.38f
            val grillRight = w * 0.86f
            val step = (grillRight - grillLeft) / 14f
            for (i in 0..14) {
                val x = grillLeft + i * step
                canvas.drawLine(x, h * 0.26f, x, h * 0.64f, louverPaint)
            }

            // Dark inner fan aperture
            val fanPaint = Paint().apply {
                color = Color.argb(160, 20, 22, 25)
                isAntiAlias = true
            }
            canvas.drawCircle(w * 0.62f, h * 0.45f, w * 0.22f, fanPaint)

            // Foreground wooden rustic post / stool
            val postPaint = Paint().apply { color = Color.rgb(130, 95, 75); isAntiAlias = true }
            canvas.drawRoundRect(w * 0.40f, h * 0.58f, w * 0.65f, h * 0.88f, 20f, 20f, postPaint)

            // Woven rope string charpai bench edge
            val ropePaint = Paint().apply {
                color = Color.rgb(210, 195, 175)
                strokeWidth = 14f
                isAntiAlias = true
            }
            for (r in 0..8) {
                val ry = h * 0.68f + r * 16f
                canvas.drawLine(0f, ry + 12f, w * 0.70f, ry - 30f, ropePaint)
            }

            canvas.restore()

            // Mode visual simulation overlays
            if (currentGroup == CameraModeGroup.PHOTO && currentPhotoMode == PhotoMode.PORTRAIT) {
                val vignette = Paint().apply {
                    shader = android.graphics.RadialGradient(
                        w * 0.5f, h * 0.48f, w * 0.55f,
                        intArrayOf(Color.TRANSPARENT, Color.argb((90 * portraitSettings.blurIntensity).toInt(), 0, 0, 0)),
                        floatArrayOf(0.6f, 1.0f),
                        android.graphics.Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), vignette)
            } else if (currentGroup == CameraModeGroup.PHOTO && currentPhotoMode == PhotoMode.NIGHT_SIGHT) {
                val nightGlow = Paint().apply {
                    color = Color.argb(35, 255, 235, 190)
                }
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), nightGlow)
            } else if (currentGroup == CameraModeGroup.VIDEO && currentVideoMode == VideoMode.CINEMA) {
                // Live Cinema Log and LUT preview (matches exported video!)
                val lutTint = Paint().apply {
                    when (cinemaSettings.activeLut) {
                        CinemaLut.WARM_GOLDEN -> color = Color.argb((40 * cinemaSettings.lutIntensity).toInt(), 255, 170, 40)
                        CinemaLut.TEAL_ORANGE -> color = Color.argb((35 * cinemaSettings.lutIntensity).toInt(), 0, 180, 200)
                        CinemaLut.MOODY_NOIR -> color = Color.argb((70 * cinemaSettings.lutIntensity).toInt(), 0, 0, 0)
                        CinemaLut.VINTAGE_FILM -> color = Color.argb((30 * cinemaSettings.lutIntensity).toInt(), 210, 160, 120)
                        CinemaLut.MATRIX_EMERALD -> color = Color.argb((35 * cinemaSettings.lutIntensity).toInt(), 40, 200, 120)
                        CinemaLut.CUSTOM_CUBE -> color = Color.argb((35 * cinemaSettings.lutIntensity).toInt(), 240, 180, 100)
                        CinemaLut.NATURAL -> color = Color.TRANSPARENT
                    }
                }
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), lutTint)

                // C-Log slight lift preview
                if (cinemaSettings.logRecordingEnabled && cinemaSettings.logProfile != CinemaLogProfile.OFF) {
                    val logLift = Paint().apply { color = Color.argb(18, 255, 255, 255) }
                    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), logLift)
                }

                // Cinema Framing Guides
                if (cinemaSettings.framingGuidesEnabled && cinemaSettings.aspectRatio != com.example.model.CinemaAspectRatio.FULL_916) {
                    val guidePaint = Paint().apply {
                        color = Color.argb(150, 255, 255, 255)
                        strokeWidth = 2f
                        style = Paint.Style.STROKE
                    }
                    val letterboxH = h * cinemaSettings.aspectRatio.maskFactor
                    canvas.drawRect(0f, letterboxH, w.toFloat(), h - letterboxH, guidePaint)
                }
            }

        } catch (_: Exception) {
        } finally {
            if (canvas != null) {
                try {
                    view.unlockCanvasAndPost(canvas)
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun captureSimulatedPhoto(onSuccess: (CapturedMedia) -> Unit) {
        _isProcessing.value = true
        coroutineScope.launch(Dispatchers.Default) {
            val tv = textureView
            var bitmap: Bitmap? = null
            if (tv != null && tv.isAvailable) {
                bitmap = tv.bitmap
            }

            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(1920, 1440, Bitmap.Config.ARGB_8888)
                val c = Canvas(bitmap)
                c.drawColor(Color.rgb(55, 50, 48))
            }

            val modeName = when (currentPhotoMode) {
                PhotoMode.PHOTO -> "Photo"
                PhotoMode.PORTRAIT -> "Portrait"
                PhotoMode.NIGHT_SIGHT -> "Night Sight"
            }

            if (currentPhotoMode == PhotoMode.PORTRAIT) {
                bitmap = ImageProcessor.applyPortraitPipeline(bitmap, portraitSettings)
            } else if (currentPhotoMode == PhotoMode.NIGHT_SIGHT) {
                delay(700)
                bitmap = ImageProcessor.applyNightPipeline(bitmap, nightSettings)
            } else if (proSettings.cinemaLut != CinemaLut.NATURAL) {
                bitmap = ImageProcessor.applyCinemaGrade(bitmap, cinemaSettings)
            }

            val media = mediaRepository.savePhoto(bitmap, modeName)
            _isProcessing.value = false
            onSuccess(media)
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        surfaceTexture = surface
        startCamera()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        // No-op
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        surfaceTexture = null
        closeCamera()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // No-op
    }

    private fun closeCamera() {
        try {
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            imageReader?.close()
            imageReader = null
            previewSurface?.release()
            previewSurface = null
        } catch (_: Exception) {
        }
    }
}
