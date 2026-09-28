package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.camera.model.BokehStyle
import com.example.camera.model.CameraMode
import com.example.camera.model.CinemaConfig
import com.example.camera.model.CinematicLut
import com.example.camera.model.HardwareCapabilities
import com.example.camera.model.PhotoMegapixelMode
import com.example.camera.model.PortraitConfig
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
  fun `test oppo camera modes and configurations`() {
    val cinema = CinemaConfig()
    assertEquals(24, cinema.videoFps)
    assertEquals(CinematicLut.REC_709, cinema.selectedLut)
    assertTrue(cinema.isLutPreviewEnabled)

    val portrait = PortraitConfig()
    assertEquals(BokehStyle.NATURAL_ROUND, portrait.bokehStyle)
    assertEquals("f/1.4", portrait.simulatedAperture)
    assertTrue(portrait.opticalBlurGuided)

    val photoMegapixelMode = PhotoMegapixelMode.M50
    assertEquals("50M", photoMegapixelMode.label)
    assertEquals(50, photoMegapixelMode.megapixels)

    val photo12M = PhotoMegapixelMode.M12
    assertEquals("12M", photo12M.label)
  }

  @Test
  fun `test zoom presets and lens capabilities`() {
    val caps = HardwareCapabilities(
      minZoom = 1.0f,
      maxZoom = 10f
    )
    assertEquals(1.0f, caps.minZoom)
    assertEquals(10f, caps.maxZoom)
  }
}
