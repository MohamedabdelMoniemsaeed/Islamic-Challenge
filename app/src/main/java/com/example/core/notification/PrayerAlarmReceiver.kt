package com.example.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.core.adhan.AdhanForegroundService

class PrayerAlarmReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    val notificationManager = AppNotificationManager(context)

    // Immediately chain tomorrow's prayer alarm so Adhan keeps firing daily when closed
    notificationManager.scheduleAllPrayerAlertsFromPrefs()

    val prayerNameFallback = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: return
    val prayerTimeFallback = intent.getStringExtra(EXTRA_PRAYER_TIME) ?: ""
    val prayerNameAr = intent.getStringExtra(EXTRA_PRAYER_NAME_AR) ?: prayerNameFallback
    val prayerNameEn = intent.getStringExtra(EXTRA_PRAYER_NAME_EN) ?: prayerNameFallback
    val prayerTimeAr = intent.getStringExtra(EXTRA_PRAYER_TIME_AR) ?: prayerTimeFallback
    val prayerTimeEn = intent.getStringExtra(EXTRA_PRAYER_TIME_EN) ?: prayerTimeFallback
    val prayerOrdinal = intent.getIntExtra(EXTRA_PRAYER_ID, 0)
    val isArabic = intent.getBooleanExtra(EXTRA_IS_ARABIC, notificationManager.isSavedArabic())

    val prayerPrefs = context.getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
    val autoPlayAdhan = prayerPrefs.getBoolean("auto_play_adhan", true)

    val displayName = if (isArabic) prayerNameAr else prayerNameEn
    val displayTime = if (isArabic) prayerTimeAr else prayerTimeEn

    if (autoPlayAdhan) {
      val serviceIntent = Intent(context, AdhanForegroundService::class.java).apply {
        action = AdhanForegroundService.ACTION_PLAY_ADHAN
        putExtra(AdhanForegroundService.EXTRA_PRAYER_NAME_AR, prayerNameAr)
        putExtra(AdhanForegroundService.EXTRA_PRAYER_NAME_EN, prayerNameEn)
        putExtra(AdhanForegroundService.EXTRA_PRAYER_TIME_AR, prayerTimeAr)
        putExtra(AdhanForegroundService.EXTRA_PRAYER_TIME_EN, prayerTimeEn)
        putExtra(AdhanForegroundService.EXTRA_PRAYER_ID, prayerOrdinal)
        putExtra(AdhanForegroundService.EXTRA_IS_ARABIC, isArabic)
      }
      try {
        ContextCompat.startForegroundService(context, serviceIntent)
        return
      } catch (_: Throwable) {
        // Fallback to high-priority notification alert if background FGS launch is blocked
      }
    }

    notificationManager.sendPrayerNotification(
      prayerName = displayName,
      prayerTime = displayTime,
      isArabic = isArabic,
      notificationId = AppNotificationManager.NOTIFICATION_ID_ADHAN_BASE + prayerOrdinal
    )
  }

  companion object {
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_PRAYER_TIME = "extra_prayer_time"
    const val EXTRA_PRAYER_NAME_AR = "extra_prayer_name_ar"
    const val EXTRA_PRAYER_NAME_EN = "extra_prayer_name_en"
    const val EXTRA_PRAYER_TIME_AR = "extra_prayer_time_ar"
    const val EXTRA_PRAYER_TIME_EN = "extra_prayer_time_en"
    const val EXTRA_PRAYER_ID = "extra_prayer_id"
    const val EXTRA_IS_ARABIC = "extra_is_arabic"
  }
}
