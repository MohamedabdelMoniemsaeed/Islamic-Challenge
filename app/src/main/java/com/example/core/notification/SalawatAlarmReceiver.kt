package com.example.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Triggered every hour when the Hourly Salawat ("صلِّ على محمد") reminder is enabled,
 * even when the app is closed. Chains the next hourly alarm, posts a Salawat notification,
 * and speaks "صلِّ على محمد".
 */
class SalawatAlarmReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    val notificationManager = AppNotificationManager(context)
    if (!notificationManager.isHourlySalawatEnabled()) {
      notificationManager.cancelHourlySalawatReminder()
      return
    }

    // Chain the next top-of-hour (:00) reminder immediately with a 60s guard against early clock wakeups
    notificationManager.scheduleHourlySalawatReminder(minDelayMillis = 60_000L)

    val isArabic = notificationManager.isSavedArabic()
    notificationManager.sendSalawatNotification(isArabic)

    val pendingResult = goAsync()
    SalawatVoicePlayer.speakSalawat(
      context = context,
      pendingResult = pendingResult,
      isManualTest = false
    )
  }
}
