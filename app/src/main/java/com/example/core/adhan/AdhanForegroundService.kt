package com.example.core.adhan

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R
import com.example.core.notification.AppNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream

/**
 * Foreground Service (`mediaPlayback`) that plays the Adhan audio reliably when a prayer
 * time arrives, even when the app UI is closed or the device screen is off.
 */
class AdhanForegroundService : Service() {

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private var playbackJob: Job? = null
  private var mediaPlayer: MediaPlayer? = null
  private var wakeLock: PowerManager.WakeLock? = null
  private var audioFocusRequest: AudioFocusRequest? = null

  private val audioManager by lazy {
    getSystemService(Context.AUDIO_SERVICE) as? AudioManager
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_STOP_ADHAN -> {
        stopPlaybackAndService(postReminderAfter = false)
        return START_NOT_STICKY
      }
      else -> {
        val prayerNameAr = intent?.getStringExtra(EXTRA_PRAYER_NAME_AR) ?: "الصلاة"
        val prayerNameEn = intent?.getStringExtra(EXTRA_PRAYER_NAME_EN) ?: "Prayer"
        val prayerTimeAr = intent?.getStringExtra(EXTRA_PRAYER_TIME_AR) ?: ""
        val prayerTimeEn = intent?.getStringExtra(EXTRA_PRAYER_TIME_EN) ?: ""
        val prayerOrdinal = intent?.getIntExtra(EXTRA_PRAYER_ID, 0) ?: 0
        val isArabic = intent?.getBooleanExtra(EXTRA_IS_ARABIC, true) ?: true

        val displayName = if (isArabic) prayerNameAr else prayerNameEn
        val displayTime = if (isArabic) prayerTimeAr else prayerTimeEn

        // Ensure notification channels exist before calling startForeground
        val appNotificationManager = AppNotificationManager(this)
        val ongoingNotification = buildOngoingAdhanNotification(
          prayerName = displayName,
          prayerTime = displayTime,
          isArabic = isArabic
        )

        try {
          val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
          } else {
            0
          }
          ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID_ADHAN_SERVICE,
            ongoingNotification,
            fgsType
          )
        } catch (_: Throwable) {
          appNotificationManager.sendPrayerNotification(
            prayerName = displayName,
            prayerTime = displayTime,
            isArabic = isArabic,
            notificationId = AppNotificationManager.NOTIFICATION_ID_ADHAN_BASE + prayerOrdinal
          )
          stopSelf()
          return START_NOT_STICKY
        }

        // Respect full Silent mode: keep visible notification alert, skip loud audio
        if (audioManager?.ringerMode == AudioManager.RINGER_MODE_SILENT) {
          appNotificationManager.sendPrayerNotification(
            prayerName = displayName,
            prayerTime = displayTime,
            isArabic = isArabic,
            notificationId = AppNotificationManager.NOTIFICATION_ID_ADHAN_BASE + prayerOrdinal
          )
          stopPlaybackAndService(postReminderAfter = false)
          return START_NOT_STICKY
        }

        startAdhanAudio(
          prayerName = displayName,
          prayerTime = displayTime,
          prayerOrdinal = prayerOrdinal,
          isArabic = isArabic
        )
        return START_NOT_STICKY
      }
    }
  }

  private fun buildOngoingAdhanNotification(
    prayerName: String,
    prayerTime: String,
    isArabic: Boolean
  ): android.app.Notification {
    val openIntent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val openPendingIntent = PendingIntent.getActivity(
      this,
      NOTIFICATION_ID_ADHAN_SERVICE,
      openIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val stopIntent = Intent(this, AdhanForegroundService::class.java).apply {
      action = ACTION_STOP_ADHAN
    }
    val stopPendingIntent = PendingIntent.getService(
      this,
      NOTIFICATION_ID_ADHAN_SERVICE + 1,
      stopIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val title = if (isArabic) {
      "🕌 حان الآن موعد أذان صلاة $prayerName"
    } else {
      "🕌 Time for $prayerName Prayer"
    }
    val body = if (isArabic) {
      "الله أكبر، الله أكبر ($prayerTime) — حيّ على الصلاة، حيّ على الفلاح"
    } else {
      "Allahu Akbar ($prayerTime) — Come to prayer, come to success"
    }
    val stopLabel = if (isArabic) "إيقاف الأذان" else "Stop Adhan"

    return NotificationCompat.Builder(this, AppNotificationManager.CHANNEL_ID_ADHAN_PLAYING)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(title)
      .setContentText(body)
      .setStyle(NotificationCompat.BigTextStyle().bigText(body))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setOngoing(true)
      .setAutoCancel(false)
      .setContentIntent(openPendingIntent)
      .addAction(android.R.drawable.ic_media_pause, stopLabel, stopPendingIntent)
      .build()
  }

  private fun startAdhanAudio(
    prayerName: String,
    prayerTime: String,
    prayerOrdinal: Int,
    isArabic: Boolean
  ) {
    playbackJob?.cancel()
    releaseMediaPlayer()
    acquireWakeLock()

    val prefs = getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
    val savedMuadhinId = prefs.getString("selected_muadhin_id", null)
    val muadhin = PrayerTimesManager.MUADHINS.find { it.id == savedMuadhinId }
      ?: PrayerTimesManager.MUADHINS.first()

    playbackJob = serviceScope.launch {
      val audioFile = withContext(Dispatchers.IO) {
        AdhanAudioCache.resolveForBackgroundAlarm(this@AdhanForegroundService, muadhin)
      }

      if (audioFile != null && audioFile.exists() && audioFile.length() > 1024L) {
        playFile(audioFile, prayerName, prayerTime, prayerOrdinal, isArabic)
      } else {
        playFallbackNotificationTone(prayerName, prayerTime, prayerOrdinal, isArabic)
      }
    }
  }

  private fun playFile(
    file: java.io.File,
    prayerName: String,
    prayerTime: String,
    prayerOrdinal: Int,
    isArabic: Boolean
  ) {
    try {
      requestAudioFocus()
      val player = MediaPlayer().apply {
        setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        )
        FileInputStream(file).use { fis ->
          setDataSource(fis.fd)
        }
        setOnPreparedListener { mp ->
          mp.start()
        }
        setOnCompletionListener {
          finishWithPrayerReminder(prayerName, prayerTime, prayerOrdinal, isArabic)
        }
        setOnErrorListener { _, _, _ ->
          if (!file.name.endsWith(".wav")) {
            file.delete()
          }
          playFallbackNotificationTone(prayerName, prayerTime, prayerOrdinal, isArabic)
          true
        }
      }
      mediaPlayer = player
      player.prepareAsync()
    } catch (_: Throwable) {
      playFallbackNotificationTone(prayerName, prayerTime, prayerOrdinal, isArabic)
    }
  }

  private fun playFallbackNotificationTone(
    prayerName: String,
    prayerTime: String,
    prayerOrdinal: Int,
    isArabic: Boolean
  ) {
    try {
      val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
      if (uri != null) {
        requestAudioFocus()
        val player = MediaPlayer().apply {
          setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_NOTIFICATION)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          setDataSource(this@AdhanForegroundService, uri)
          setOnPreparedListener { mp -> mp.start() }
          setOnCompletionListener {
            finishWithPrayerReminder(prayerName, prayerTime, prayerOrdinal, isArabic)
          }
          setOnErrorListener { _, _, _ ->
            finishWithPrayerReminder(prayerName, prayerTime, prayerOrdinal, isArabic)
            true
          }
        }
        mediaPlayer = player
        player.prepareAsync()
        return
      }
    } catch (_: Throwable) {}

    finishWithPrayerReminder(prayerName, prayerTime, prayerOrdinal, isArabic)
  }

  private fun finishWithPrayerReminder(
    prayerName: String,
    prayerTime: String,
    prayerOrdinal: Int,
    isArabic: Boolean
  ) {
    AppNotificationManager(this).sendPrayerNotification(
      prayerName = prayerName,
      prayerTime = prayerTime,
      isArabic = isArabic,
      notificationId = AppNotificationManager.NOTIFICATION_ID_ADHAN_BASE + prayerOrdinal
    )
    stopPlaybackAndService(postReminderAfter = false)
  }

  private fun stopPlaybackAndService(postReminderAfter: Boolean) {
    playbackJob?.cancel()
    playbackJob = null
    releaseMediaPlayer()
    abandonAudioFocus()
    releaseWakeLock()
    try {
      ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    } catch (_: Throwable) {}
    if (!postReminderAfter) {
      stopSelf()
    }
  }

  private fun acquireWakeLock() {
    try {
      if (wakeLock?.isHeld == true) return
      val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
      wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "IslamicChallenge::AdhanWakeLock").apply {
        setReferenceCounted(false)
        acquire(5 * 60 * 1000L)
      }
    } catch (_: Throwable) {}
  }

  private fun releaseWakeLock() {
    try {
      if (wakeLock?.isHeld == true) {
        wakeLock?.release()
      }
    } catch (_: Throwable) {}
    wakeLock = null
  }

  private fun releaseMediaPlayer() {
    try {
      mediaPlayer?.let { player ->
        if (player.isPlaying) {
          player.stop()
        }
        player.release()
      }
    } catch (_: Throwable) {}
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
    } catch (_: Throwable) {}
  }

  private fun abandonAudioFocus() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
      } else {
        @Suppress("DEPRECATION")
        audioManager?.abandonAudioFocus(null)
      }
    } catch (_: Throwable) {}
    audioFocusRequest = null
  }

  override fun onDestroy() {
    playbackJob?.cancel()
    releaseMediaPlayer()
    abandonAudioFocus()
    releaseWakeLock()
    serviceScope.cancel()
    super.onDestroy()
  }

  companion object {
    const val ACTION_PLAY_ADHAN = "com.example.action.PLAY_ADHAN"
    const val ACTION_STOP_ADHAN = "com.example.action.STOP_ADHAN"
    const val EXTRA_PRAYER_NAME_AR = "extra_prayer_name_ar"
    const val EXTRA_PRAYER_NAME_EN = "extra_prayer_name_en"
    const val EXTRA_PRAYER_TIME_AR = "extra_prayer_time_ar"
    const val EXTRA_PRAYER_TIME_EN = "extra_prayer_time_en"
    const val EXTRA_PRAYER_ID = "extra_prayer_id"
    const val EXTRA_IS_ARABIC = "extra_is_arabic"
    const val NOTIFICATION_ID_ADHAN_SERVICE = 5100
  }
}
