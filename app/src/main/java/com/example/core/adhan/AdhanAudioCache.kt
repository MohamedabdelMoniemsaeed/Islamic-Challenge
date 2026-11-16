package com.example.core.adhan

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Shared offline/online audio cache and built-in synthesized chime generator
 * used by both the in-app [AdhanPlayerManager] and background [AdhanForegroundService].
 */
object AdhanAudioCache {

  private fun cacheDir(context: Context): File {
    return File(context.cacheDir, "adhan_cache").apply {
      if (!exists()) mkdirs()
    }
  }

  private fun createHttpClient(connectTimeoutSec: Long, readTimeoutSec: Long): OkHttpClient {
    return OkHttpClient.Builder()
      .connectTimeout(connectTimeoutSec, TimeUnit.SECONDS)
      .readTimeout(readTimeoutSec, TimeUnit.SECONDS)
      .followRedirects(true)
      .followSslRedirects(true)
      .build()
  }

  fun getCachedAudioFile(context: Context, muadhin: MuadhinAudio): File? {
    if (muadhin.isOfflineBuiltIn) {
      return ensureOfflineChimeWav(context)
    }
    val cachedFile = File(cacheDir(context), "adhan_${muadhin.id}.mp3")
    return if (cachedFile.exists() && cachedFile.length() > 15_000L) cachedFile else null
  }

  fun resolveOrDownloadAudio(
    context: Context,
    muadhin: MuadhinAudio,
    connectTimeoutSec: Long = 10,
    readTimeoutSec: Long = 15
  ): File? {
    if (muadhin.isOfflineBuiltIn) {
      return ensureOfflineChimeWav(context)
    }

    val dir = cacheDir(context)
    val cachedFile = File(dir, "adhan_${muadhin.id}.mp3")
    if (cachedFile.exists() && cachedFile.length() > 15_000L) {
      return cachedFile
    }

    val client = createHttpClient(connectTimeoutSec, readTimeoutSec)
    for (url in muadhin.audioUrls) {
      try {
        val request = Request.Builder()
          .url(url)
          .header("User-Agent", "IslamicChallengeApp/7.0 (Android)")
          .build()

        client.newCall(request).execute().use { response ->
          if (!response.isSuccessful) return@use
          val body = response.body ?: return@use
          val contentType = response.header("Content-Type")?.lowercase() ?: ""
          if (contentType.contains("text/html")) return@use

          val tempFile = File(dir, "adhan_${muadhin.id}.tmp")
          body.byteStream().use { input ->
            FileOutputStream(tempFile).use { output ->
              input.copyTo(output)
            }
          }

          if (tempFile.exists() && tempFile.length() > 15_000L) {
            if (cachedFile.exists()) cachedFile.delete()
            if (tempFile.renameTo(cachedFile)) {
              return cachedFile
            }
            return tempFile
          } else {
            tempFile.delete()
          }
        }
      } catch (_: Exception) {
        // Try next mirror URL
      }
    }
    return null
  }

  /**
   * Resolves the selected Muadhin audio or immediately falls back to the built-in
   * offline mosque chime so background prayer alarms always produce audible sound.
   */
  fun resolveForBackgroundAlarm(context: Context, muadhin: MuadhinAudio): File? {
    getCachedAudioFile(context, muadhin)?.let { return it }
    resolveOrDownloadAudio(context, muadhin, connectTimeoutSec = 5, readTimeoutSec = 6)?.let { return it }
    return ensureOfflineChimeWav(context)
  }

  /**
   * Generates a serene, warm, harmonic Islamic mosque chime cadence (PCM 16-bit WAV)
   * completely offline so audio playback is always available even without internet.
   */
  fun ensureOfflineChimeWav(context: Context): File? {
    val wavFile = File(cacheDir(context), "adhan_offline_chime.wav")
    if (wavFile.exists() && wavFile.length() > 40_000L) {
      return wavFile
    }

    return try {
      val sampleRate = 22050
      val durationSec = 10.0
      val totalSamples = (sampleRate * durationSec).toInt()
      val pcmData = ShortArray(totalSamples)

      data class Note(val freq: Double, val startSec: Double, val durSec: Double, val amp: Double)
      val notes = listOf(
        Note(293.66, 0.10, 1.3, 0.35),
        Note(349.23, 0.55, 1.4, 0.35),
        Note(392.00, 1.05, 1.8, 0.40),
        Note(440.00, 1.75, 2.2, 0.42),
        Note(466.16, 3.20, 1.4, 0.34),
        Note(392.00, 3.90, 1.4, 0.36),
        Note(369.99, 4.55, 1.6, 0.36),
        Note(293.66, 5.25, 2.6, 0.44),
        Note(349.23, 6.80, 1.2, 0.32),
        Note(369.99, 7.35, 1.4, 0.34),
        Note(293.66, 8.00, 1.9, 0.42)
      )

      for (n in notes) {
        val startIdx = (n.startSec * sampleRate).toInt().coerceIn(0, totalSamples - 1)
        val endIdx = ((n.startSec + n.durSec) * sampleRate).toInt().coerceIn(startIdx, totalSamples)
        for (i in startIdx until endIdx) {
          val t = (i - startIdx).toDouble() / sampleRate
          val attack = (t / 0.06).coerceAtMost(1.0)
          val decay = exp(-2.4 * t / n.durSec)
          val fundamental = sin(2.0 * PI * n.freq * t)
          val harmonic2 = 0.28 * sin(2.0 * PI * n.freq * 2.0 * t) * exp(-4.0 * t)
          val harmonic3 = 0.10 * sin(2.0 * PI * n.freq * 3.0 * t) * exp(-6.0 * t)
          val sample = (fundamental + harmonic2 + harmonic3) * attack * decay * n.amp
          val mixed = pcmData[i] + (sample * Short.MAX_VALUE).toInt()
          pcmData[i] = mixed.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
      }

      writeWavFile(wavFile, sampleRate, pcmData)
    } catch (_: Exception) {
      null
    }
  }

  /**
   * Generates a brief, gentle 1.6-second two-tone chime WAV for the hourly Salawat reminder
   * as an audio fallback when Arabic Text-To-Speech is not installed on the device.
   */
  fun ensureSalawatChimeWav(context: Context): File? {
    val wavFile = File(cacheDir(context), "salawat_chime.wav")
    if (wavFile.exists() && wavFile.length() > 10_000L) {
      return wavFile
    }

    return try {
      val sampleRate = 22050
      val durationSec = 1.8
      val totalSamples = (sampleRate * durationSec).toInt()
      val pcmData = ShortArray(totalSamples)

      data class Tone(val freq: Double, val startSec: Double, val durSec: Double, val amp: Double)
      val tones = listOf(
        Tone(523.25, 0.05, 0.70, 0.34), // C5
        Tone(659.25, 0.35, 0.85, 0.36), // E5
        Tone(783.99, 0.70, 1.05, 0.38)  // G5
      )

      for (n in tones) {
        val startIdx = (n.startSec * sampleRate).toInt().coerceIn(0, totalSamples - 1)
        val endIdx = ((n.startSec + n.durSec) * sampleRate).toInt().coerceIn(startIdx, totalSamples)
        for (i in startIdx until endIdx) {
          val t = (i - startIdx).toDouble() / sampleRate
          val attack = (t / 0.04).coerceAtMost(1.0)
          val decay = exp(-3.2 * t / n.durSec)
          val sample = (sin(2.0 * PI * n.freq * t) + 0.25 * sin(2.0 * PI * n.freq * 2.0 * t)) * attack * decay * n.amp
          val mixed = pcmData[i] + (sample * Short.MAX_VALUE).toInt()
          pcmData[i] = mixed.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
      }

      writeWavFile(wavFile, sampleRate, pcmData)
    } catch (_: Exception) {
      null
    }
  }

  private fun writeWavFile(wavFile: File, sampleRate: Int, pcmData: ShortArray): File {
    val dataSize = pcmData.size * 2
    val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
      put("RIFF".toByteArray(Charsets.US_ASCII))
      putInt(36 + dataSize)
      put("WAVE".toByteArray(Charsets.US_ASCII))
      put("fmt ".toByteArray(Charsets.US_ASCII))
      putInt(16)
      putShort(1)
      putShort(1)
      putInt(sampleRate)
      putInt(sampleRate * 2)
      putShort(2)
      putShort(16)
      put("data".toByteArray(Charsets.US_ASCII))
      putInt(dataSize)
    }

    val bodyBuffer = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
    for (s in pcmData) {
      bodyBuffer.putShort(s)
    }

    FileOutputStream(wavFile).use { fos ->
      fos.write(header.array())
      fos.write(bodyBuffer.array())
    }
    return wavFile
  }
}
