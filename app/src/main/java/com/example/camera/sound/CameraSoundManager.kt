package com.example.camera.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.AudioManager
import android.os.Build

class CameraSoundManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var soundPool: SoundPool? = null
    private var shutterSoundId: Int = 0
    private var timerBeepSoundId: Int = 0
    private var burstSoundId: Int = 0
    private var focusLockSoundId: Int = 0
    private var isLoaded = false

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attributes)
            .build()
            .apply {
                setOnLoadCompleteListener { _, _, status ->
                    if (status == 0) isLoaded = true
                }
            }

        // Use Android system audio effects
        shutterSoundId = soundPool?.load(context, android.R.raw.class.fields.firstOrNull { it.name == "camera_click" }?.getInt(null) ?: 0, 1) ?: 0
    }

    fun playShutterSound(enabled: Boolean = true) {
        if (!enabled) return
        try {
            audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.9f)
        } catch (_: Exception) {}
    }

    fun playBurstSound(enabled: Boolean = true) {
        if (!enabled) return
        try {
            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.6f)
        } catch (_: Exception) {}
    }

    fun playTimerBeep(isFinalBeep: Boolean = false) {
        try {
            val effect = if (isFinalBeep) AudioManager.FX_FOCUS_NAVIGATION_UP else AudioManager.FX_KEY_CLICK
            audioManager.playSoundEffect(effect, 1.0f)
        } catch (_: Exception) {}
    }

    fun playFocusLockSound() {
        try {
            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR, 0.7f)
        } catch (_: Exception) {}
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
