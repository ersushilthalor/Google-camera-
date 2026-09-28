package com.example.camera.model

enum class CinemaFps(val label: String, val fps: Int) {
    FPS_24("24 FPS (Hollywood Master)", 24),
    FPS_25("25 FPS (PAL Standard)", 25),
    FPS_30("30 FPS (NTSC Standard)", 30),
    FPS_48("48 FPS (High Frame Rate)", 48),
    FPS_60("60 FPS (Ultra Smooth)", 60)
}

enum class CinemaLogProfile(val displayName: String, val dynamicRangeStops: Float) {
    OFF("Standard Rec.709", 10.0f),
    C_LOG("Canon Log (C-Log)", 13.5f),
    S_LOG3("Sony S-Log3", 14.0f),
    D_LOG("DJI D-Log", 12.8f),
    APPLE_LOG("Apple Log HDR", 14.5f)
}

enum class CinemaAspectRatio(val label: String, val ratioValue: Float, val maskFactor: Float) {
    ANAMORPHIC_239("2.39:1 Anamorphic", 2.39f, 0.16f),
    FLAT_185("1.85:1 Academy Flat", 1.85f, 0.08f),
    WIDESCREEN_169("16:9 Standard", 1.777f, 0.0f),
    FULL_916("9:16 Vertical Story", 0.5625f, 0.0f)
}

data class CinemaEngineConfig(
    val frameRate: CinemaFps = CinemaFps.FPS_24,
    val logProfile: CinemaLogProfile = CinemaLogProfile.C_LOG,
    val activeLut: CinematicLut = CinematicLut.WARM_GOLDEN,
    val lutIntensity: Float = 0.85f,
    val aspectRatio: CinemaAspectRatio = CinemaAspectRatio.ANAMORPHIC_239,
    val shutterAngleDegrees: Int = 180, // 180 degree rule (1/48s at 24fps)
    val applyLutToExportedVideo: Boolean = true,
    val framingGuidesEnabled: Boolean = true,
    val histogramEnabled: Boolean = true,
    val audioMeterEnabled: Boolean = true,
    val bitrateMbps: Int = 50
)
