package com.example.core.adhan

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
  val errorMessage: String? = null
)

class AdhanPlayerManager(private val context: Context) {

  private var mediaPlayer: MediaPlayer? = null
  private val handler = Handler(Looper.getMainLooper())

  private val _uiState = MutableStateFlow(AdhanPlayerUiState())
  val uiState: StateFlow<AdhanPlayerUiState> = _uiState.asStateFlow()

  private val progressRunnable = object : Runnable {
    override fun run() {
      mediaPlayer?.let { player ->
        if (player.isPlaying) {
          _uiState.value = _uiState.value.copy(
            currentPositionMs = player.currentPosition,
            durationMs = player.duration
          )
          handler.postDelayed(this, 500)
        }
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
        errorMessage = null
      )
    }
  }

  fun playCurrent() {
    val current = _uiState.value.currentMuadhin
    playUrl(current.audioUrl)
  }

  fun playUrl(url: String) {
    try {
      if (mediaPlayer == null) {
        initPlayer(url)
      } else {
        mediaPlayer?.let { player ->
          if (_uiState.value.state == AdhanPlaybackState.PAUSED) {
            player.start()
            _uiState.value = _uiState.value.copy(state = AdhanPlaybackState.PLAYING)
            handler.post(progressRunnable)
          } else {
            stop()
            initPlayer(url)
          }
        }
      }
    } catch (e: Exception) {
      _uiState.value = _uiState.value.copy(
        state = AdhanPlaybackState.ERROR,
        errorMessage = "تعذر تشغيل الأذان، تأكد من الاتصال بالإنترنت"
      )
    }
  }

  private fun initPlayer(url: String) {
    releasePlayer()
    _uiState.value = _uiState.value.copy(
      state = AdhanPlaybackState.BUFFERING,
      errorMessage = null
    )

    try {
      mediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        )
        setDataSource(url)
        setOnPreparedListener { player ->
          player.start()
          _uiState.value = _uiState.value.copy(
            state = AdhanPlaybackState.PLAYING,
            durationMs = player.duration
          )
          handler.post(progressRunnable)
        }
        setOnCompletionListener {
          _uiState.value = _uiState.value.copy(
            state = AdhanPlaybackState.IDLE,
            currentPositionMs = 0
          )
          handler.removeCallbacks(progressRunnable)
        }
        setOnErrorListener { _, _, _ ->
          _uiState.value = _uiState.value.copy(
            state = AdhanPlaybackState.ERROR,
            errorMessage = "تعذر تحميل ملف الأذان الصوتي"
          )
          handler.removeCallbacks(progressRunnable)
          true
        }
        prepareAsync()
      }
    } catch (e: Exception) {
      _uiState.value = _uiState.value.copy(
        state = AdhanPlaybackState.ERROR,
        errorMessage = "خطأ في تهيئة مشغل الصوت"
      )
    }
  }

  fun pause() {
    mediaPlayer?.let { player ->
      if (player.isPlaying) {
        player.pause()
        _uiState.value = _uiState.value.copy(state = AdhanPlaybackState.PAUSED)
        handler.removeCallbacks(progressRunnable)
      }
    }
  }

  fun stop() {
    handler.removeCallbacks(progressRunnable)
    try {
      mediaPlayer?.let { player ->
        if (player.isPlaying) {
          player.stop()
        }
      }
    } catch (_: Exception) {}
    _uiState.value = _uiState.value.copy(
      state = AdhanPlaybackState.IDLE,
      currentPositionMs = 0
    )
  }

  fun release() {
    handler.removeCallbacks(progressRunnable)
    releasePlayer()
  }

  private fun releasePlayer() {
    try {
      mediaPlayer?.release()
    } catch (_: Exception) {}
    mediaPlayer = null
  }
}
