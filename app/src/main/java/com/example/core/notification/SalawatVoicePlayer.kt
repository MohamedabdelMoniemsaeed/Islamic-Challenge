package com.example.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.example.core.adhan.AdhanAudioCache
import java.io.FileInputStream
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Speaks "صلِّ على محمد" in a male Arabic voice via Android TextToSpeech
 * (preferring local or network male Arabic TTS voices and falling back cleanly
 * to a serene built-in chime WAV if Arabic TTS is unavailable), safely managing
 * WakeLock, AudioFocus, and BroadcastReceiver async lifecycle.
 */
object SalawatVoicePlayer {

  private const val UTTERANCE_ID = "salawat_hourly_utterance"
  private const val UTTERANCE_RETRY_ID = "salawat_hourly_utterance_retry"
  private const val MAX_SESSION_TIMEOUT_MS = 9500L
  private const val TTS_INIT_TIMEOUT_MS = 2500L

  private val MALE_TOKEN_REGEX = Regex("(^|[-_])(ard|are|arm|male|man|m|m0|m1)([-_]|$)")
  private val FEMALE_TOKEN_REGEX = Regex("(^|[-_])(arc|arz|female|woman|girl|f|f0|f1)([-_]|$)")
  private val KNOWN_MALE_NAMES = listOf("tariq", "majed", "maged", "naayf", "hamed", "hamid", "zaid", "omar", "hassan", "karim")
  private val KNOWN_FEMALE_NAMES = listOf("laila", "leila", "salma", "mariam", "fatima", "zeina", "hala", "rana", "amira", "noor", "hana", "sara")

  data class MaleVoiceCandidate(
    val voice: Voice?,
    val isConfirmedMale: Boolean
  )

  fun isConfirmedMaleArabicVoice(voice: Voice): Boolean {
    val name = voice.name.lowercase(Locale.US)
    if (name.contains("female") || FEMALE_TOKEN_REGEX.containsMatchIn(name)) return false
    if (KNOWN_FEMALE_NAMES.any { name.contains(it) }) return false

    if (MALE_TOKEN_REGEX.containsMatchIn(name)) return true
    if (KNOWN_MALE_NAMES.any { name.contains(it) }) return true

    val features = voice.features ?: emptySet()
    return features.any { feat ->
      val f = feat.lowercase(Locale.US)
      (f.contains("male") && !f.contains("female"))
    }
  }

  fun isKnownFemaleArabicVoice(voice: Voice): Boolean {
    val name = voice.name.lowercase(Locale.US)
    if (FEMALE_TOKEN_REGEX.containsMatchIn(name)) return true
    if (KNOWN_FEMALE_NAMES.any { name.contains(it) }) return true
    val features = voice.features ?: emptySet()
    return features.any { it.lowercase(Locale.US).contains("female") }
  }

  /**
   * Picks the best Arabic male voice available from [voices], strongly preferring
   * offline confirmed male voices (e.g. Google `ar-xa-x-ard-local` / `ar-xa-x-are-local`),
   * then online confirmed male voices when [isNetworkAvailable] is true, then non-female voices.
   */
  fun selectPreferredMaleArabicVoice(
    voices: Collection<Voice>?,
    isNetworkAvailable: Boolean
  ): MaleVoiceCandidate {
    val arabicVoices = voices
      ?.filter { it.locale?.language?.equals("ar", ignoreCase = true) == true }
      .orEmpty()

    if (arabicVoices.isEmpty()) {
      return MaleVoiceCandidate(voice = null, isConfirmedMale = false)
    }

    val usableVoices = arabicVoices.filter { !it.isNetworkConnectionRequired || isNetworkAvailable }
      .ifEmpty { arabicVoices }

    // 1. Offline confirmed male Arabic voice (instant, reliable)
    usableVoices.firstOrNull { !it.isNetworkConnectionRequired && isConfirmedMaleArabicVoice(it) }
      ?.let { return MaleVoiceCandidate(voice = it, isConfirmedMale = true) }

    // 2. Network confirmed male Arabic voice (when online)
    if (isNetworkAvailable) {
      usableVoices.firstOrNull { it.isNetworkConnectionRequired && isConfirmedMaleArabicVoice(it) }
        ?.let { return MaleVoiceCandidate(voice = it, isConfirmedMale = true) }
    }

    // 3. Offline non-female Arabic voice
    usableVoices.firstOrNull { !it.isNetworkConnectionRequired && !isKnownFemaleArabicVoice(it) }
      ?.let { return MaleVoiceCandidate(voice = it, isConfirmedMale = false) }

    // 4. Network non-female Arabic voice
    if (isNetworkAvailable) {
      usableVoices.firstOrNull { it.isNetworkConnectionRequired && !isKnownFemaleArabicVoice(it) }
        ?.let { return MaleVoiceCandidate(voice = it, isConfirmedMale = false) }
    }

    // 5. Any remaining offline Arabic voice (will apply subtle lower pitch so it doesn't sound shrill)
    val fallback = usableVoices.firstOrNull { !it.isNetworkConnectionRequired } ?: usableVoices.first()
    return MaleVoiceCandidate(voice = fallback, isConfirmedMale = false)
  }

  private fun isNetworkConnected(context: Context): Boolean {
    return try {
      val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
      val network = cm.activeNetwork ?: return false
      val caps = cm.getNetworkCapabilities(network) ?: return false
      caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } catch (_: Throwable) {
      false
    }
  }

  fun speakSalawat(
    context: Context,
    pendingResult: BroadcastReceiver.PendingResult? = null,
    isManualTest: Boolean = false
  ) {
    val appContext = context.applicationContext
    val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    // Respect Silent/Vibrate mode for automatic background reminders
    if (!isManualTest && audioManager != null && audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
      safeFinish(pendingResult)
      return
    }
    if (isManualTest && audioManager != null && audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT) {
      safeFinish(pendingResult)
      return
    }

    val finished = AtomicBoolean(false)
    val mainHandler = Handler(Looper.getMainLooper())
    var ttsInstance: TextToSpeech? = null
    var mediaPlayer: MediaPlayer? = null
    var wakeLock: PowerManager.WakeLock? = null
    var focusRequest: AudioFocusRequest? = null

    fun cleanup() {
      if (!finished.compareAndSet(false, true)) return
      mainHandler.removeCallbacksAndMessages(null)
      try {
        ttsInstance?.stop()
        ttsInstance?.shutdown()
      } catch (_: Throwable) {}
      ttsInstance = null

      try {
        mediaPlayer?.let { mp ->
          if (mp.isPlaying) mp.stop()
          mp.release()
        }
      } catch (_: Throwable) {}
      mediaPlayer = null

      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          focusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
          @Suppress("DEPRECATION")
          audioManager?.abandonAudioFocus(null)
        }
      } catch (_: Throwable) {}

      try {
        if (wakeLock?.isHeld == true) wakeLock?.release()
      } catch (_: Throwable) {}
      wakeLock = null

      safeFinish(pendingResult)
    }

    fun requestFocus() {
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(
              AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            )
            .build()
          focusRequest = req
          audioManager?.requestAudioFocus(req)
        } else {
          @Suppress("DEPRECATION")
          audioManager?.requestAudioFocus(
            null,
            AudioManager.STREAM_NOTIFICATION,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
          )
        }
      } catch (_: Throwable) {}
    }

    fun playChimeFallback() {
      if (finished.get()) return
      try {
        val chimeFile = AdhanAudioCache.ensureSalawatChimeWav(appContext)
        if (chimeFile == null || !chimeFile.exists()) {
          cleanup()
          return
        }
        requestFocus()
        val player = MediaPlayer().apply {
          setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_NOTIFICATION)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          FileInputStream(chimeFile).use { fis ->
            setDataSource(fis.fd)
          }
          setOnPreparedListener { mp -> mp.start() }
          setOnCompletionListener { cleanup() }
          setOnErrorListener { _, _, _ ->
            cleanup()
            true
          }
        }
        mediaPlayer = player
        player.prepareAsync()
      } catch (_: Throwable) {
        cleanup()
      }
    }

    if (pendingResult != null) {
      try {
        val pm = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "IslamicChallenge::SalawatWakeLock")?.apply {
          setReferenceCounted(false)
          acquire(MAX_SESSION_TIMEOUT_MS + 1000L)
        }
      } catch (_: Throwable) {}
    }

    mainHandler.postDelayed({ cleanup() }, MAX_SESSION_TIMEOUT_MS)

    val ttsStartedSpeaking = AtomicBoolean(false)
    val fallbackRunnable = Runnable {
      if (!ttsStartedSpeaking.get() && !finished.get()) {
        try {
          ttsInstance?.shutdown()
        } catch (_: Throwable) {}
        ttsInstance = null
        playChimeFallback()
      }
    }
    mainHandler.postDelayed(fallbackRunnable, TTS_INIT_TIMEOUT_MS)

    try {
      ttsInstance = TextToSpeech(appContext) { status ->
        if (finished.get()) return@TextToSpeech
        if (status != TextToSpeech.SUCCESS) {
          mainHandler.removeCallbacks(fallbackRunnable)
          playChimeFallback()
          return@TextToSpeech
        }

        val engine = ttsInstance ?: run {
          playChimeFallback()
          return@TextToSpeech
        }

        val arAvailability = engine.isLanguageAvailable(Locale("ar", "SA")).let { res ->
          if (res >= TextToSpeech.LANG_AVAILABLE) res else engine.isLanguageAvailable(Locale("ar"))
        }

        if (arAvailability < TextToSpeech.LANG_AVAILABLE) {
          mainHandler.removeCallbacks(fallbackRunnable)
          playChimeFallback()
          return@TextToSpeech
        }

        try {
          val selectedLocale = if (engine.isLanguageAvailable(Locale("ar", "SA")) >= TextToSpeech.LANG_AVAILABLE) {
            Locale("ar", "SA")
          } else {
            Locale("ar")
          }
          // Set baseline language FIRST, then override voice so setLanguage doesn't reset voice to female default
          engine.language = selectedLocale

          val availableVoices = try {
            engine.voices
          } catch (_: Throwable) {
            null
          }
          val candidate = selectPreferredMaleArabicVoice(
            voices = availableVoices,
            isNetworkAvailable = isNetworkConnected(appContext)
          )
          val selectedVoice = candidate.voice
          if (selectedVoice != null) {
            engine.voice = selectedVoice
          }

          // Natural pitch (1.0) for a confirmed male voice; deeper pitch (0.80) only if only female/unknown fallback exists
          engine.setPitch(if (candidate.isConfirmedMale) 1.0f else 0.80f)
          engine.setSpeechRate(0.95f)
          engine.setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_NOTIFICATION)
              .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
              .build()
          )

          var retriedOffline = false
          engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
              ttsStartedSpeaking.set(true)
              mainHandler.removeCallbacks(fallbackRunnable)
            }

            override fun onDone(utteranceId: String?) {
              cleanup()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
              handleTtsError()
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
              handleTtsError()
            }

            private fun handleTtsError() {
              if (!retriedOffline && selectedVoice?.isNetworkConnectionRequired == true) {
                retriedOffline = true
                val offlineCandidate = selectPreferredMaleArabicVoice(availableVoices, isNetworkAvailable = false)
                if (offlineCandidate.voice != null) {
                  try {
                    engine.voice = offlineCandidate.voice
                    engine.setPitch(if (offlineCandidate.isConfirmedMale) 1.0f else 0.80f)
                    val retryParams = Bundle().apply {
                      putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_NOTIFICATION)
                    }
                    if (engine.speak("صَلِّ عَلَى مُحَمَّد", TextToSpeech.QUEUE_FLUSH, retryParams, UTTERANCE_RETRY_ID) == TextToSpeech.SUCCESS) {
                      return
                    }
                  } catch (_: Throwable) {}
                }
              }
              playChimeFallback()
            }
          })

          requestFocus()
          ttsStartedSpeaking.set(true)
          mainHandler.removeCallbacks(fallbackRunnable)
          val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_NOTIFICATION)
          }
          val speakRes = engine.speak(
            "صَلِّ عَلَى مُحَمَّد",
            TextToSpeech.QUEUE_FLUSH,
            params,
            UTTERANCE_ID
          )
          if (speakRes != TextToSpeech.SUCCESS) {
            playChimeFallback()
          }
        } catch (_: Throwable) {
          playChimeFallback()
        }
      }
    } catch (_: Throwable) {
      mainHandler.removeCallbacks(fallbackRunnable)
      playChimeFallback()
    }
  }

  private fun safeFinish(pendingResult: BroadcastReceiver.PendingResult?) {
    try {
      pendingResult?.finish()
    } catch (_: Throwable) {}
  }
}
