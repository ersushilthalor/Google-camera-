package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.camera.LevelState
import com.example.model.CameraModeGroup
import com.example.model.CinemaModeSettings
import com.example.model.FlashMode
import com.example.model.PhotoMode
import com.example.model.PortraitModeSettings
import com.example.model.VideoMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Camera", appName)
  }

  @Test
  fun `test mode group default values`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals("Photo", context.getString(R.string.mode_photo))
    assertEquals("Portrait", context.getString(R.string.mode_portrait))
    assertEquals("Night Sight", context.getString(R.string.mode_night))
    assertEquals("Video", context.getString(R.string.mode_video))
    assertEquals("Slow Motion", context.getString(R.string.mode_slow_motion))
    assertEquals("Cinema", context.getString(R.string.mode_cinema))
  }

  @Test
  fun `test camera modes and configurations`() {
    val cinema = CinemaModeSettings()
    assertEquals(24, cinema.frameRate.fps)

    val portrait = PortraitModeSettings()
    assertEquals("f/2.0", portrait.apertureValue)

    assertEquals(CameraModeGroup.PHOTO, CameraModeGroup.valueOf("PHOTO"))
    assertEquals(PhotoMode.PHOTO, PhotoMode.PHOTO)
    assertEquals(VideoMode.VIDEO, VideoMode.VIDEO)
    assertEquals(FlashMode.OFF, FlashMode.OFF)
  }

  @Test
  fun `test level state`() {
    val level = LevelState(rollAngle = 0f, isLevel = true)
    assertEquals(0f, level.rollAngle)
    assertTrue(level.isLevel)
  }
}
