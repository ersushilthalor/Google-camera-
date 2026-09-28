package com.example.camera.motionphoto

import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.view.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MotionPhotoVideoEncoder {

    suspend fun encodeBitmapsToMp4(
        frames: List<Bitmap>,
        outputFile: File,
        width: Int = 1080,
        height: Int = 1440,
        fps: Int = 30
    ): Boolean = withContext(Dispatchers.IO) {
        if (frames.isEmpty()) return@withContext false

        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, 6_000_000)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = codec.createInputSurface()
            codec.start()

            val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var trackIndex = -1
            var muxerStarted = false
            val bufferInfo = MediaCodec.BufferInfo()

            val frameDurationUs = 1_000_000L / fps

            for (i in frames.indices) {
                val bmp = frames[i]
                val canvas = inputSurface.lockCanvas(null)
                canvas.drawBitmap(bmp, null, android.graphics.Rect(0, 0, width, height), null)
                inputSurface.unlockCanvasAndPost(canvas)

                // Drain output
                while (true) {
                    val status = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                    if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        trackIndex = muxer.addTrack(codec.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    } else if (status >= 0) {
                        val encodedData = codec.getOutputBuffer(status)
                        if (encodedData != null && muxerStarted && bufferInfo.size > 0) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            bufferInfo.presentationTimeUs = i * frameDurationUs
                            muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                        }
                        codec.releaseOutputBuffer(status, false)
                        break
                    } else {
                        break
                    }
                }
            }

            codec.signalEndOfInputStream()
            codec.stop()
            codec.release()
            inputSurface.release()

            if (muxerStarted) {
                muxer.stop()
            }
            muxer.release()
            true
        } catch (_: Exception) {
            false
        }
    }
}
