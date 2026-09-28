package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
  fun `test mode specific settings and cinema profiles`() {
    val cinema = com.example.model.CinemaModeSettings()
    assertEquals(com.example.model.VideoResolution.UHD_4K, cinema.resolution)
    assertEquals(com.example.model.CinemaFps.FPS_24, cinema.frameRate)
    assertEquals(com.example.model.CinemaLogProfile.C_LOG, cinema.logProfile)
    assertEquals(true, cinema.applyLutToExportedVideo)

    val photo = com.example.model.PhotoModeSettings()
    assertEquals(com.example.model.PhotoResolution.RES_50MP, photo.resolution)
    assertEquals(com.example.model.PhotoAspectRatio.RATIO_4_3, photo.aspectRatio)

    val portrait = com.example.model.PortraitModeSettings()
    assertEquals(com.example.model.PortraitBlurStyle.DISC_BOKEH, portrait.blurStyle)

    val video = com.example.model.VideoModeSettings()
    assertEquals(com.example.model.VideoFps.FPS_30, video.frameRate)

    val slowMo = com.example.model.SlowMotionModeSettings()
    assertEquals(com.example.model.SlowMotionFps.FPS_120, slowMo.frameRate)
  }

  @Test
  fun `test zoom presets and lens capabilities`() {
    val caps = com.example.camera.CameraHardwareCapabilities(
      hasBackCamera = true,
      hasUltraWideLens = false,
      ultraWideStatusDescription = "Not exposed by device hardware (Single main camera detected)",
      availableLensesDescription = "Wide (4.5mm, 1×)"
    )
    assertEquals(false, caps.hasUltraWideLens)
    assertEquals("Not exposed by device hardware (Single main camera detected)", caps.ultraWideStatusDescription)

    val capsWithUw = com.example.camera.CameraHardwareCapabilities(
      hasBackCamera = true,
      hasUltraWideLens = true,
      ultraWideCameraId = "2",
      ultraWideFocalLength = 1.8f,
      ultraWideFov = 115f,
      ultraWideZoomRatio = 0.6f,
      ultraWideStatusDescription = "Detected (Camera ID: 2, 1.8mm, 115° FOV)"
    )
    assertEquals(true, capsWithUw.hasUltraWideLens)
    assertEquals("2", capsWithUw.ultraWideCameraId)
    assertEquals(0.6f, capsWithUw.ultraWideZoomRatio)
  }
}
