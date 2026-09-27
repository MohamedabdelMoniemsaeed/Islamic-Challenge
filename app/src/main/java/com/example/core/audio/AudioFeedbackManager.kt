package com.example.core.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * AudioFeedbackManager provides safe, lightweight audio and haptic feedback
 * across all game interactions without external audio file dependencies.
 */
class AudioFeedbackManager(private val context: Context) {
  private var toneGenerator: ToneGenerator? = null

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
    val volumePercent = (volume * 100).toInt()
    initToneGenerator(volumePercent)
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
