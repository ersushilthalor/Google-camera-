package com.example.model

enum class CameraModeGroup {
    PHOTO,
    VIDEO
}

enum class PhotoMode(val title: String) {
    PORTRAIT("Portrait"),
    PHOTO("Photo"),
    NIGHT_SIGHT("Night Sight")
}

enum class VideoMode(val title: String) {
    VIDEO("Video"),
    SLOW_MOTION("Slow Motion"),
    CINEMA("Cinema")
}

enum class FlashMode(val iconLabel: String) {
    OFF("Off"),
    AUTO("Auto"),
    ON("On"),
    TORCH("Torch")
}

enum class PhotoResolution(val title: String, val megapixels: String, val width: Int, val height: Int) {
    RES_12MP("12 MP", "4000 x 3000", 4000, 3000),
    RES_24MP("24 MP", "5664 x 4248", 5664, 4248),
    RES_50MP("50 MP", "8160 x 6120", 8160, 6120),
    RES_108MP("108 MP Ultra", "12000 x 9000", 12000, 9000)
}

enum class PhotoAspectRatio(val label: String, val ratio: Float) {
    RATIO_4_3("4:3", 3f / 4f),
    RATIO_16_9("16:9", 9f / 16f),
    RATIO_1_1("1:1", 1f)
}

enum class HdrMode(val title: String) {
    AUTO("Auto HDR"),
    ON("HDR On"),
    OFF("HDR Off")
}

enum class RawCaptureMode(val title: String) {
    JPEG_ONLY("JPEG Only"),
    RAW_AND_JPEG("RAW (DNG) + JPEG")
}

enum class ImageProcessingTone(val title: String) {
    STANDARD("Standard"),
    NATURAL("Natural"),
    VIBRANT("Vibrant")
}

enum class NoiseReductionMode(val title: String) {
    AUTO("Auto"),
    HIGH("High Quality (Multi-frame)"),
    FAST("Fast (Low Latency)")
}

enum class SharpnessMode(val title: String) {
    STANDARD("Standard"),
    SOFT("Soft Cinematic"),
    SHARP("High Crispness")
}

// 1. Photo Mode Settings
data class PhotoModeSettings(
    val resolution: PhotoResolution = PhotoResolution.RES_50MP,
    val aspectRatio: PhotoAspectRatio = PhotoAspectRatio.RATIO_4_3,
    val hdrMode: HdrMode = HdrMode.AUTO,
    val flashMode: FlashMode = FlashMode.AUTO,
    val timerSeconds: Int = 0, // 0 = off, 3, 10
    val rawCapture: RawCaptureMode = RawCaptureMode.JPEG_ONLY,
    val imageProcessing: ImageProcessingTone = ImageProcessingTone.NATURAL,
    val noiseReduction: NoiseReductionMode = NoiseReductionMode.HIGH,
    val sharpness: SharpnessMode = SharpnessMode.STANDARD,
    val gridEnabled: Boolean = false,
    val levelEnabled: Boolean = true
)

// 2. Portrait Mode Settings
enum class PortraitBlurStyle(val title: String) {
    DISC_BOKEH("Disc Bokeh"),
    STUDIO_LENS("Studio Prime 85mm"),
    GAUSSIAN("Smooth Blur")
}

enum class PortraitLightingMode(val title: String) {
    NATURAL("Natural Light"),
    STUDIO("Studio Light"),
    CONTOUR("Contour Light"),
    STAGE("Stage Light")
}

enum class SkinToneMode(val title: String) {
    NATURAL("Natural Tone"),
    SMOOTH("Subtle Smooth"),
    GLOW("Warm Glow")
}

data class PortraitModeSettings(
    val blurIntensity: Float = 0.65f, // 0.0 to 1.0 (f/1.4 to f/8.0)
    val apertureValue: String = "f/2.0",
    val blurStyle: PortraitBlurStyle = PortraitBlurStyle.DISC_BOKEH,
    val lightingMode: PortraitLightingMode = PortraitLightingMode.NATURAL,
    val resolution: PhotoResolution = PhotoResolution.RES_24MP,
    val aspectRatio: PhotoAspectRatio = PhotoAspectRatio.RATIO_4_3,
    val faceDetectionEnabled: Boolean = true,
    val skinTone: SkinToneMode = SkinToneMode.NATURAL,
    val flashMode: FlashMode = FlashMode.OFF,
    val timerSeconds: Int = 0,
    val gridEnabled: Boolean = false
)

// 3. Video Mode Settings
enum class VideoResolution(val label: String, val width: Int, val height: Int) {
    HD_720P("720p HD", 720, 1280),
    FHD_1080P("1080p FHD", 1080, 1920),
    UHD_4K("4K Ultra HD", 2160, 3840)
}

enum class VideoFps(val label: String, val fps: Int) {
    FPS_24("24 FPS (Cinematic)", 24),
    FPS_30("30 FPS (Standard)", 30),
    FPS_60("60 FPS (Smooth)", 60)
}

enum class VideoStabilization(val label: String) {
    AUTO("Auto Stabilization"),
    OIS("OIS (Hardware Optical)"),
    EIS("EIS (Electronic)"),
    OIS_PLUS_EIS("OIS + EIS Combined")
}

enum class VideoCodec(val label: String) {
    H264("H.264 / AVC (Compatible)"),
    H265("H.265 / HEVC (Efficient 10-bit)")
}

enum class AudioRecordingMode(val label: String) {
    STEREO("Stereo High-Res"),
    WIND_NOISE_REDUCTION("Wind Noise Reduction"),
    MUTE("Mute Audio")
}

data class VideoModeSettings(
    val resolution: VideoResolution = VideoResolution.FHD_1080P,
    val frameRate: VideoFps = VideoFps.FPS_30,
    val stabilization: VideoStabilization = VideoStabilization.OIS_PLUS_EIS,
    val bitrateMbps: Int = 35, // 16, 35, 50
    val codec: VideoCodec = VideoCodec.H264,
    val audioMode: AudioRecordingMode = AudioRecordingMode.STEREO,
    val torchEnabled: Boolean = false,
    val continuousAf: Boolean = true,
    val autoExposureLock: Boolean = false,
    val gridEnabled: Boolean = false,
    val levelEnabled: Boolean = true
)

// 4. Slow Motion Mode Settings
enum class SlowMotionFps(val label: String, val fps: Int) {
    FPS_120("120 FPS", 120),
    FPS_240("240 FPS", 240),
    FPS_480("480 FPS Super Slow", 480)
}

enum class SlowMotionPlaybackSpeed(val label: String, val multiplier: Float) {
    SPEED_QUARTER("1/4x Speed (Smooth)", 0.25f),
    SPEED_EIGHTH("1/8x Speed (Dramatic)", 0.125f),
    SPEED_SIXTEENTH("1/16x Speed (Ultra Slow)", 0.0625f)
}

data class SlowMotionModeSettings(
    val frameRate: SlowMotionFps = SlowMotionFps.FPS_120,
    val resolution: VideoResolution = VideoResolution.FHD_1080P,
    val playbackSpeed: SlowMotionPlaybackSpeed = SlowMotionPlaybackSpeed.SPEED_QUARTER,
    val bitrateMbps: Int = 40,
    val stabilization: VideoStabilization = VideoStabilization.EIS,
    val highSpeedExposureBias: Boolean = true,
    val focusLock: Boolean = false,
    val muteAudio: Boolean = true
)

// 5. Cinema Mode Settings
enum class CinemaFps(val label: String, val fps: Int) {
    FPS_24("24.00 FPS (Standard Film)", 24),
    FPS_25("25.00 FPS (PAL Cinema)", 25),
    FPS_48("48.00 FPS (HFR Cinema)", 48)
}

enum class CinemaAspectRatio(val label: String, val maskFactor: Float) {
    ANAMORPHIC_239("2.39:1 Anamorphic", 0.14f),
    FLAT_185("1.85:1 Academy Flat", 0.08f),
    FULL_916("9:16 Vertical Cinema", 0.0f)
}

enum class CinemaFocusControl(val label: String) {
    CONTINUOUS_AF("Cinema Autofocus"),
    MANUAL_PULL("Manual Focus Pull"),
    RACK_FOCUS("Rack Focus Preset")
}

enum class CinemaLogProfile(val displayName: String, val description: String) {
    OFF("Standard (Rec.709)", "Normal dynamic range, ready to view"),
    C_LOG("Canon C-Log", "Cinema curve preserving 14+ stops highlight dynamic range"),
    S_LOG3("Sony S-Log3", "Wide gamut curve designed for grading flexibility"),
    D_LOG("DJI D-Log", "Optimized shadow detail and soft highlight roll-off"),
    FILM_FLAT("Cine Flat Log", "Neutral flat profile for custom grading")
}

enum class CinemaColorSpace(val label: String) {
    REC_709("Rec.709 (SDR Broadcast)"),
    BT_2020("BT.2020 (HDR Wide Gamut)")
}

enum class CinemaLut(val displayName: String, val description: String) {
    NATURAL("Natural Passthrough", "True-to-life balanced tones"),
    WARM_GOLDEN("Golden Hour Sunset", "Warm amber highlights and rich copper shadows"),
    TEAL_ORANGE("Blockbuster Teal & Orange", "Hollywood contrast with vibrant skin tones"),
    MOODY_NOIR("Moody Noir Monochrome", "High-contrast dramatic black and white"),
    VINTAGE_FILM("Vintage 70s Kodachrome", "Faded cyan shadows with warm nostalgic saturation"),
    MATRIX_EMERALD("Emerald Cyberpunk", "Stylized deep greens and cool tones"),
    CUSTOM_CUBE("Custom .cube 3D LUT", "Imported 33x33x33 Cube LUT table")
}

data class CinemaModeSettings(
    // A. Recording
    val resolution: VideoResolution = VideoResolution.UHD_4K,
    val frameRate: CinemaFps = CinemaFps.FPS_24,
    val bitrateMbps: Int = 100, // 50, 100
    val codec: VideoCodec = VideoCodec.H265,
    val aspectRatio: CinemaAspectRatio = CinemaAspectRatio.ANAMORPHIC_239,
    val stabilization: VideoStabilization = VideoStabilization.OIS_PLUS_EIS,
    val focusControl: CinemaFocusControl = CinemaFocusControl.CONTINUOUS_AF,
    val shutterAngle: String = "180° (1/48s)",
    val iso: Int = 400,

    // B. Log Color Profiles
    val logRecordingEnabled: Boolean = true,
    val logProfile: CinemaLogProfile = CinemaLogProfile.C_LOG,
    val colorSpace: CinemaColorSpace = CinemaColorSpace.BT_2020,
    val bitDepth10Bit: Boolean = true,

    // C. LUT Settings
    val activeLut: CinemaLut = CinemaLut.WARM_GOLDEN,
    val lutEnabled: Boolean = true,
    val lutIntensity: Float = 0.85f, // 0.0 to 1.0
    val applyLutToExportedVideo: Boolean = true,
    val customLutLoaded: Boolean = false,
    val customLutName: String = "FilmGrade3D.cube",

    // D. Cinema Color Settings
    val whiteBalanceKelvin: Int = 5600, // 3200K to 6500K
    val tint: Int = 0, // -10 to +10
    val contrast: Float = 1.0f, // 0.5 to 1.5
    val saturation: Float = 1.05f, // 0.0 to 2.0
    val highlights: Float = 0.0f, // -10 to +10
    val shadows: Float = 0.0f, // -10 to +10
    val sharpness: Float = 1.0f,

    // E. Cinema Monitoring
    val histogramEnabled: Boolean = true,
    val waveformEnabled: Boolean = false,
    val zebraWarningEnabled: Boolean = false,
    val focusPeakingEnabled: Boolean = false,
    val horizonLevelEnabled: Boolean = true,
    val framingGuidesEnabled: Boolean = true
)

// 6. Night Mode Settings
enum class NightExposureDuration(val label: String, val seconds: Int) {
    AUTO("Auto (1 - 2 sec)", 2),
    HANDHELD_3S("Handheld (3 sec)", 3),
    TRIPOD_5S("Tripod (5 sec)", 5),
    ASTRO_10S("Astrophotography (10 sec)", 10)
}

enum class NightProcessingMode(val label: String) {
    DEEP_FUSION("Deep Fusion (Multi-frame Stacking)"),
    ULTRA_CLEAR("Ultra Clear Night"),
    BALANCED("Balanced Low Light")
}

data class NightModeSettings(
    val autoNight: Boolean = true,
    val exposureDuration: NightExposureDuration = NightExposureDuration.AUTO,
    val flashMode: FlashMode = FlashMode.OFF,
    val timerSeconds: Int = 0,
    val nightProcessing: NightProcessingMode = NightProcessingMode.DEEP_FUSION,
    val strongNoiseReduction: Boolean = true,
    val resolution: PhotoResolution = PhotoResolution.RES_50MP,
    val aspectRatio: PhotoAspectRatio = PhotoAspectRatio.RATIO_4_3,
    val gridEnabled: Boolean = false,
    val levelEnabled: Boolean = true
)

// 7. Global More Settings (Shared across all modes)
data class GlobalMoreSettings(
    // General
    val defaultLens: String = "Main Lens (1x)",
    val defaultCameraMode: String = "Photo",
    val globalGrid: Boolean = false,
    val globalLevel: Boolean = true,
    val shutterSound: Boolean = true,
    val saveLocation: String = "Internal / DCIM / Camera",

    // Camera Hardware Info
    val hardwareLevel: String = "LEVEL_3 (Full Camera2 Pro Capabilities)",
    val sensorMegapixels: String = "50.0 MP (8160 x 6120)",
    val supportedFpsList: String = "24, 25, 30, 60, 120, 240 FPS",
    val availableLenses: String = "Wide 24mm f/1.8, Telephoto 50mm f/2.4, Front 20mm f/2.0",
    val ultraWideLensReport: String = "Not exposed by device hardware (Single main camera detected)",
    val oisSupported: Boolean = true,
    val rawDngSupported: Boolean = true,

    // Photo Defaults
    val defaultPhotoResolution: PhotoResolution = PhotoResolution.RES_50MP,
    val defaultPhotoFormat: String = "JPEG (Ultra Quality)",
    val defaultColorTone: ImageProcessingTone = ImageProcessingTone.NATURAL,

    // Video Defaults
    val defaultVideoResolution: VideoResolution = VideoResolution.FHD_1080P,
    val defaultVideoFps: VideoFps = VideoFps.FPS_30,
    val defaultVideoCodec: VideoCodec = VideoCodec.H264,
    val defaultVideoStabilization: VideoStabilization = VideoStabilization.OIS_PLUS_EIS,

    // Cinema Defaults
    val defaultCinemaProfile: CinemaLogProfile = CinemaLogProfile.C_LOG,
    val defaultCinemaLut: CinemaLut = CinemaLut.WARM_GOLDEN,
    val defaultCinemaResolution: VideoResolution = VideoResolution.UHD_4K,
    val defaultCinemaFps: CinemaFps = CinemaFps.FPS_24,

    // Storage
    val internalFreeSpace: String = "84.5 GB Free of 128 GB",
    val fileNamingScheme: String = "IMG_YYYYMMDD_HHMMSS / VID_YYYYMMDD_HHMMSS",

    // Advanced
    val highPerformanceMode: Boolean = true,
    val neuralDenoise: Boolean = true,
    val debugOverlay: Boolean = false
)

data class CapturedMedia(
    val id: String,
    val filePath: String,
    val uriString: String,
    val isVideo: Boolean,
    val timestamp: Long,
    val modeName: String,
    val resolutionLabel: String = "1080p",
    val appliedLut: String? = null
)

data class ProSettings(
    val exposureCompensation: Int = 0,
    val iso: Int = 0,
    val whiteBalance: String = "Auto",
    val cinemaLut: CinemaLut = CinemaLut.WARM_GOLDEN,
    val gridEnabled: Boolean = false,
    val levelEnabled: Boolean = true,
    val resolution4k: Boolean = true,
    val timerSeconds: Int = 0
)
