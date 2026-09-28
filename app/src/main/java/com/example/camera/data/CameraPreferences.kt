package com.example.camera.data

import android.content.Context
import android.content.SharedPreferences

class CameraPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("oppocam_prefs", Context.MODE_PRIVATE)

    var defaultMode: String
        get() = prefs.getString("default_mode", "PHOTO") ?: "PHOTO"
        set(value) = prefs.edit().putString("default_mode", value).apply()

    var highPerformanceMode: Boolean
        get() = prefs.getBoolean("high_perf_mode", true)
        set(value) = prefs.edit().putBoolean("high_perf_mode", value).apply()

    var gyroStabilizationEnabled: Boolean
        get() = prefs.getBoolean("gyro_stab", true)
        set(value) = prefs.edit().putBoolean("gyro_stab", value).apply()

    var naturalLogExposureEnabled: Boolean
        get() = prefs.getBoolean("natural_log_exp", true)
        set(value) = prefs.edit().putBoolean("natural_log_exp", value).apply()

    var rec2020ColorSpaceEnabled: Boolean
        get() = prefs.getBoolean("rec2020_color", false)
        set(value) = prefs.edit().putBoolean("rec2020_color", value).apply()

    var aiSuperResolutionModel: String
        get() = prefs.getString("ai_sr_model", "HAT_BALANCED") ?: "HAT_BALANCED"
        set(value) = prefs.edit().putString("ai_sr_model", value).apply()

    var motionPhotoEnabled: Boolean
        get() = prefs.getBoolean("motion_photo", false)
        set(value) = prefs.edit().putBoolean("motion_photo", value).apply()

    var shutterSoundEnabled: Boolean
        get() = prefs.getBoolean("shutter_sound", true)
        set(value) = prefs.edit().putBoolean("shutter_sound", value).apply()

    var gridEnabled: Boolean
        get() = prefs.getBoolean("grid_enabled", false)
        set(value) = prefs.edit().putBoolean("grid_enabled", value).apply()

    var horizonLevelEnabled: Boolean
        get() = prefs.getBoolean("level_enabled", true)
        set(value) = prefs.edit().putBoolean("level_enabled", value).apply()

    var zebraStripesThreshold: Int
        get() = prefs.getInt("zebra_threshold", 90)
        set(value) = prefs.edit().putInt("zebra_threshold", value).apply()

    var focusPeakingColor: String
        get() = prefs.getString("focus_peaking_color", "GREEN") ?: "GREEN"
        set(value) = prefs.edit().putString("focus_peaking_color", value).apply()
}
