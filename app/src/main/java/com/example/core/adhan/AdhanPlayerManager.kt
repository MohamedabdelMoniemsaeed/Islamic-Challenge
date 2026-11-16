package com.example.core.adhan

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

enum class AdhanPlaybackState {
  IDLE,
  BUFFERING,
  PLAYING,
  PAUSED,
  ERROR
}

data class AdhanPlayerUiState(
  val state: AdhanPlaybackState = AdhanPlaybackState.IDLE,
  val currentMuadhin: MuadhinAudio = PrayerTimesManager.MUADHINS.first(),
  val currentPositionMs: Int = 0,
  val durationMs: Int = 0,
  val errorMessage: String? = null,
  val isPlayingOfflineFallback: Boolean = false
)

class AdhanPlayerManager(private val context: Context) {

  private var mediaPlayer: MediaPlayer? = null
  private val handler = Handler(Looper.getMainLooper())
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private var loadJob: Job? = null
  private var precacheJob: Job? = null

  private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
  private var audioFocusRequest: AudioFocusRequest? = null

  private val _uiState = MutableStateFlow(AdhanPlayerUiState())
  val uiState: StateFlow<AdhanPlayerUiState> = _uiState.asStateFlow()

  private val progressRunnable = object : Runnable {
    override fun run() {
      mediaPlayer?.let { player ->
        try {
          if (player.isPlaying) {
            _uiState.value = _uiState.value.copy(
              currentPositionMs = player.currentPosition,
              durationMs = player.duration.coerceAtLeast(1)
            )
            handler.postDelayed(this, 400)
          }
        } catch (_: Exception) {}
      }
    }
  }

  fun selectMuadhin(muadhin: MuadhinAudio) {
    if (_uiState.value.currentMuadhin.id != muadhin.id) {
      stop()
      _uiState.value = _uiState.value.copy(
        currentMuadhin = muadhin,
        currentPositionMs = 0,
        durationMs = 0,
        errorMessage = null,
        isPlayingOfflineFallback = muadhin.isOfflineBuiltIn
      )
    }
    precacheMuadhin(muadhin)
  }

  /**
   * Downloads and caches the selected Muadhin audio in the background while the app is open,
   * so background prayer alarms can play the real Adhan immediately even without internet.
   */
  fun precacheMuadhin(muadhin: MuadhinAudio) {
    precacheJob?.cancel()
    precacheJob = scope.launch(Dispatchers.IO) {
      try {
        if (muadhin.isOfflineBuiltIn) {
          AdhanAudioCache.ensureOfflineChimeWav(context)
        } else {
          AdhanAudioCache.resolveOrDownloadAudio(context, muadhin)
        }
      } catch (_: Throwable) {}
    }
  }

  fun playCurrent(isArabic: Boolean = true) {
    stopBackgroundAdhanService()
    val current = _uiState.value.currentMuadhin
    if (_uiState.value.state == AdhanPlaybackState.PAUSED && mediaPlayer != null) {
      try {
        requestAudioFocus()
        mediaPlayer?.start()
        _uiState.value = _uiState.value.copy(
          state = AdhanPlaybackState.PLAYING,
          errorMessage = null
        )
        handler.post(progressRunnable)
        return
      } catch (_: Exception) {
        releasePlayer()
      }
    }

    playMuadhin(current, isArabic)
  }

  fun playOfflineChimeFallback() {
    stopBackgroundAdhanService()
    val chime = PrayerTimesManager.MUADHINS.firstOrNull { it.isOfflineBuiltIn } ?: return
    stop()
    _uiState.value = _uiState.value.copy(
      currentMuadhin = chime,
      isPlayingOfflineFallback = true,
      errorMessage = null
    )
    playMuadhin(chime, isArabic = true)
  }

  private fun stopBackgroundAdhanService() {
    try {
      val stopIntent = Intent(context, AdhanForegroundService::class.java).apply {
        action = AdhanForegroundService.ACTION_STOP_ADHAN
      }
      context.startService(stopIntent)
    } catch (_: Throwable) {}
  }

  private fun playMuadhin(muadhin: MuadhinAudio, isArabic: Boolean) {
    loadJob?.cancel()
    releasePlayer()

    _uiState.value = _uiState.value.copy(
      state = AdhanPlaybackState.BUFFERING,
      currentPositionMs = 0,
      durationMs = 0,
      errorMessage = null,
      isPlayingOfflineFallback = muadhin.isOfflineBuiltIn
    )

    loadJob = scope.launch {
      val localFile = withContext(Dispatchers.IO) {
        if (muadhin.isOfflineBuiltIn) {
          AdhanAudioCache.ensureOfflineChimeWav(context)
        } else {
          AdhanAudioCache.resolveOrDownloadAudio(context, muadhin)
        }
      }

      if (localFile != null && localFile.exists() && localFile.length() > 1024L) {
        startMediaPlayerFromFile(localFile, isArabic)
      } else {
        _uiState.value = _uiState.value.copy(
          state = AdhanPlaybackState.ERROR,
          errorMessage = if (isArabic) {
            "تعذر تحميل صوت الأذان، تحقق من اتصال الإنترنت أو جرّب نغمة المسجد المحلية."
          } else {
            "Could not load Adhan audio. Check your internet connection or use the Offline Mosque Chime."
          }
        )
      }
    }
  }

  private fun startMediaPlayerFromFile(file: File, isArabic: Boolean) {
    try {
      releasePlayer()
      requestAudioFocus()

      val player = MediaPlayer()
      player.setAudioAttributes(
        AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .build()
      )

      FileInputStream(file).use { fis ->
        player.setDataSource(fis.fd)
      }

      player.setOnPreparedListener { mp ->
        mp.start()
        _uiState.value = _uiState.value.copy(
          state = AdhanPlaybackState.PLAYING,
          durationMs = mp.duration.coerceAtLeast(1),
          errorMessage = null
        )
        handler.post(progressRunnable)
      }

      player.setOnCompletionListener {
        handler.removeCallbacks(progressRunnable)
        abandonAudioFocus()
        _uiState.value = _uiState.value.copy(
          state = AdhanPlaybackState.IDLE,
          currentPositionMs = 0
        )
      }

      player.setOnErrorListener { _, _, _ ->
        handler.removeCallbacks(progressRunnable)
        abandonAudioFocus()
        if (!file.name.endsWith(".wav")) {
          file.delete()
        }
        _uiState.value = _uiState.value.copy(
          state = AdhanPlaybackState.ERROR,
          errorMessage = if (isArabic) "تعذر تشغيل الملف الصوتي، أعد المحاولة." else "Playback error, please retry."
        )
        true
      }

      mediaPlayer = player
      player.prepareAsync()
    } catch (_: Exception) {
      abandonAudioFocus()
      _uiState.value = _uiState.value.copy(
        state = AdhanPlaybackState.ERROR,
        errorMessage = if (isArabic) "خطأ في تهيئة مشغل الصوت" else "Audio player initialization error"
      )
    }
  }

  fun seekTo(positionMs: Int) {
    try {
      mediaPlayer?.let { player ->
        if (player.duration > 0) {
          val target = positionMs.coerceIn(0, player.duration)
          player.seekTo(target)
          _uiState.value = _uiState.value.copy(currentPositionMs = target)
        }
      }
    } catch (_: Exception) {}
  }

  fun pause() {
    mediaPlayer?.let { player ->
      try {
        if (player.isPlaying) {
          player.pause()
          _uiState.value = _uiState.value.copy(state = AdhanPlaybackState.PAUSED)
          handler.removeCallbacks(progressRunnable)
          abandonAudioFocus()
        }
      } catch (_: Exception) {}
    }
  }

  fun stop() {
    loadJob?.cancel()
    loadJob = null
    handler.removeCallbacks(progressRunnable)
    releasePlayer()
    abandonAudioFocus()
    _uiState.value = _uiState.value.copy(
      state = AdhanPlaybackState.IDLE,
      currentPositionMs = 0,
      durationMs = 0,
      errorMessage = null
    )
  }

  fun release() {
    loadJob?.cancel()
    precacheJob?.cancel()
    handler.removeCallbacks(progressRunnable)
    releasePlayer()
    abandonAudioFocus()
    scope.cancel()
  }

  private fun releasePlayer() {
    try {
      mediaPlayer?.let { player ->
        if (player.isPlaying) {
          player.stop()
        }
        player.release()
      }
    } catch (_: Exception) {}
    mediaPlayer = null
  }

  private fun requestAudioFocus() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_MEDIA)
              .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
              .build()
          )
          .build()
        audioFocusRequest = req
        audioManager?.requestAudioFocus(req)
      } else {
        @Suppress("DEPRECATION")
        audioManager?.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
      }
    } catch (_: Exception) {}
  }

  private fun abandonAudioFocus() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
      } else {
        @Suppress("DEPRECATION")
        audioManager?.abandonAudioFocus(null)
      }
    } catch (_: Exception) {}
  }
}
