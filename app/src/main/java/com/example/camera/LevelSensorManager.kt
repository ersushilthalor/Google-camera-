package com.example.camera

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class LevelState(
    val rollAngle: Float = 0f,
    val isLevel: Boolean = true
)

class LevelSensorManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _levelState = MutableStateFlow(LevelState(0f, true))
    val levelState: StateFlow<LevelState> = _levelState.asStateFlow()

    private var smoothedAngle = 0f
    private val smoothingFactor = 0.2f

    fun startListening() {
        if (rotationSensor != null) {
            sensorManager?.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (accelSensor != null) {
            sensorManager?.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        var rawAngle = 0f
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            // roll is orientation[2] in radians
            rawAngle = Math.toDegrees(orientation[2].toDouble()).toFloat()
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val ax = event.values[0]
            val ay = event.values[1]
            rawAngle = Math.toDegrees(atan2(-ax.toDouble(), ay.toDouble())).toFloat()
        }

        // Normalize angle around 0 degrees for portrait orientation
        while (rawAngle > 180f) rawAngle -= 360f
        while (rawAngle < -180f) rawAngle += 360f

        // Clamp to -45..45 for camera level indicator
        val clampedAngle = rawAngle.coerceIn(-45f, 45f)
        smoothedAngle += (clampedAngle - smoothedAngle) * smoothingFactor

        val displayAngle = if (kotlin.math.abs(smoothedAngle) <= 1.5f) {
            0f
        } else {
            (smoothedAngle * 10).roundToInt() / 10f
        }

        _levelState.value = LevelState(
            rollAngle = displayAngle,
            isLevel = displayAngle == 0f
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
