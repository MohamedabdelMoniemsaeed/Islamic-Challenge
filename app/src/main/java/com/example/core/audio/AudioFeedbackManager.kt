package com.example.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

/**
 * AudioFeedbackManager provides safe, lightweight audio, serene ambient background pad,
 * and haptic feedback across all game interactions without external audio file dependencies.
 */
class AudioFeedbackManager(private val context: Context) {
  private var toneGenerator: ToneGenerator? = null
  private var ambientPlayer: MediaPlayer? = null
  private var ambientEnabled: Boolean = false
  private var currentVolume: Float = 0.8f

  init {
    initToneGenerator(80)
  }

  private fun initToneGenerator(volumePercent: Int) {
    try {
      toneGenerator?.release()
      val clampedVolume = volumePercent.coerceIn(10, 100)
      toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, clampedVolume)
    } catch (_: Throwable) {
      toneGenerator = null
    }
  }

  fun updateVolume(volume: Float) {
    currentVolume = volume.coerceIn(0.1f, 1.0f)
    val volumePercent = (currentVolume * 100).toInt()
    initToneGenerator(volumePercent)
    try {
      val bgVol = currentVolume * 0.22f
      ambientPlayer?.setVolume(bgVol, bgVol)
    } catch (_: Throwable) {}
  }

  fun setAmbientMusicEnabled(enabled: Boolean) {
    ambientEnabled = enabled
    if (enabled) {
      startAmbientMusic()
    } else {
      stopAmbientMusic()
    }
  }

  fun onAppForeground() {
    if (ambientEnabled) {
      startAmbientMusic()
    }
  }

  fun onAppBackground() {
    try {
      if (ambientPlayer?.isPlaying == true) {
        ambientPlayer?.pause()
      }
    } catch (_: Throwable) {}
  }

  private fun startAmbientMusic() {
    try {
      ambientPlayer?.let { player ->
        if (!player.isPlaying) {
          val bgVol = currentVolume * 0.22f
          player.setVolume(bgVol, bgVol)
          player.start()
        }
        return
      }

      val wavFile = ensureAmbientLoopWav() ?: return
      val player = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_GAME)
            .build()
        )
        FileInputStream(wavFile).use { fis ->
          setDataSource(fis.fd)
        }
        isLooping = true
        val bgVol = currentVolume * 0.22f
        setVolume(bgVol, bgVol)
        prepare()
        start()
      }
      ambientPlayer = player
    } catch (_: Throwable) {
      // Safe fallback in Robolectric / headless test runners
    }
  }

  fun stopAmbientMusic() {
    try {
      ambientPlayer?.let { player ->
        if (player.isPlaying) {
          player.stop()
        }
        player.release()
      }
    } catch (_: Throwable) {}
    ambientPlayer = null
  }

  private fun ensureAmbientLoopWav(): File? {
    val file = File(context.cacheDir, "serene_ambient_pad.wav")
    if (file.exists() && file.length() > 20_000L) return file

    return try {
      val sampleRate = 16000
      val durationSec = 6.0
      val totalSamples = (sampleRate * durationSec).toInt()
      val pcmData = ShortArray(totalSamples)

      // Soft, seamless looping warm harmonic drone (D3 + A3 + F#4)
      val baseFreqs = doubleArrayOf(146.83, 220.00, 293.66)
      for (i in 0 until totalSamples) {
        val t = i.toDouble() / sampleRate
        val mod = 0.75 + 0.25 * sin(2.0 * PI * (1.0 / durationSec) * t)
        var sum = 0.0
        for ((idx, f) in baseFreqs.withIndex()) {
          val cycles = kotlin.math.round(f * durationSec)
          val exactF = cycles / durationSec
          sum += sin(2.0 * PI * exactF * t) * (0.28 / (idx + 1))
        }
        val sample = (sum * mod).coerceIn(-0.9, 0.9)
        pcmData[i] = (sample * Short.MAX_VALUE).toInt().toShort()
      }

      val dataSize = totalSamples * 2
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

      val body = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
      for (s in pcmData) {
        body.putShort(s)
      }

      FileOutputStream(file).use { fos ->
        fos.write(header.array())
        fos.write(body.array())
      }
      file
    } catch (_: Throwable) {
      null
    }
  }

  fun playCorrectFeedback(soundEnabled: Boolean, vibrationEnabled: Boolean, volume: Float = 0.8f) {
    if (soundEnabled) {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
      } catch (_: Throwable) {}
    }
    if (vibrationEnabled) {
      vibrate(longArrayOf(0, 45, 55, 60))
    }
  }

  fun playIncorrectFeedback(soundEnabled: Boolean, vibrationEnabled: Boolean, volume: Float = 0.8f) {
    if (soundEnabled) {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 220)
      } catch (_: Throwable) {}
    }
    if (vibrationEnabled) {
      vibrate(longArrayOf(0, 120))
    }
  }

  fun playClickFeedback(soundEnabled: Boolean, vibrationEnabled: Boolean, volume: Float = 0.8f) {
    if (soundEnabled) {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
      } catch (_: Throwable) {}
    }
    if (vibrationEnabled) {
      vibrate(longArrayOf(0, 20))
    }
  }

  fun playLevelUpFeedback(soundEnabled: Boolean, vibrationEnabled: Boolean, volume: Float = 0.8f) {
    if (soundEnabled) {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 350)
      } catch (_: Throwable) {}
    }
    if (vibrationEnabled) {
      vibrate(longArrayOf(0, 70, 80, 70, 80, 120))
    }
  }

  fun playAchievementFeedback(soundEnabled: Boolean, vibrationEnabled: Boolean, volume: Float = 0.8f) {
    if (soundEnabled) {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
      } catch (_: Throwable) {}
    }
    if (vibrationEnabled) {
      vibrate(longArrayOf(0, 60, 60, 90))
    }
  }

  fun playTimerWarningFeedback(soundEnabled: Boolean, vibrationEnabled: Boolean, volume: Float = 0.8f) {
    if (soundEnabled) {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 60)
      } catch (_: Throwable) {}
    }
    if (vibrationEnabled) {
      vibrate(longArrayOf(0, 30))
    }
  }

  fun playDailyChallengeCompleteFeedback(soundEnabled: Boolean, vibrationEnabled: Boolean, volume: Float = 0.8f) {
    if (soundEnabled) {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 300)
      } catch (_: Throwable) {}
    }
    if (vibrationEnabled) {
      vibrate(longArrayOf(0, 80, 80, 80, 80, 140))
    }
  }

  private fun vibrate(pattern: LongArray) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        val vibrator = vibratorManager?.defaultVibrator
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
      } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
          @Suppress("DEPRECATION")
          vibrator?.vibrate(pattern, -1)
        }
      }
    } catch (_: Throwable) {}
  }
}
