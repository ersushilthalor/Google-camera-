package com.example.camera.model

data class VideoAdjustments(
    val exposureCompensation: Int = 0,
    val iso: Int = 0,
    val shutterSpeedNumerator: Int = 1,
    val shutterSpeedDenominator: Int = 60,
    val whiteBalanceKelvin: Int = 5500,
    val manualFocusDistance: Float = 0.0f, // 0 = Auto, >0 = diopters
    val isAutoExposureLock: Boolean = false,
    val isAutoFocusLock: Boolean = false,
    val focusPeakingEnabled: Boolean = false,
    val zebraStripesEnabled: Boolean = false,
    val audioMeterEnabled: Boolean = true,
    val saturationBoost: Float = 1.0f,
    val contrastBoost: Float = 1.0f
)
