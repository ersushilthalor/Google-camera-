package com.example.camera.model

data class CameraUiCustomization(
    val visibleModes: List<String> = listOf("PHOTO", "PORTRAIT", "NIGHT", "VIDEO", "CINEMA", "PRO"),
    val accentColor: Long = 0xFFF6E8DC,
    val showQuickSettingsInTopBar: Boolean = true,
    val proControlsExpanded: Boolean = false,
    val zoomRulerStyle: String = "CIRCULAR_PILL", // "CIRCULAR_PILL" or "HORIZONTAL_RULER"
    val showHistogramInProMode: Boolean = true,
    val showLevelIndicator: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true
)
