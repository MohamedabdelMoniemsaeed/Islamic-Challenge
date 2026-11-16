package com.example.core.notification

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailyReminderReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    val notificationManager = AppNotificationManager(context)

    when (intent?.action) {
      Intent.ACTION_BOOT_COMPLETED,
      Intent.ACTION_MY_PACKAGE_REPLACED,
      Intent.ACTION_TIME_CHANGED,
      Intent.ACTION_TIMEZONE_CHANGED,
      AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> {
        // Re-schedule prayer Adhan alarms, daily reminder, and hourly Salawat reminder
        notificationManager.rescheduleAllFromPrefs()
      }
      else -> {
        if (!notificationManager.isNotificationsEnabled()) return

        val reminders = listOf(
          Pair(
            "🌙 تحدي المعرفة الإسلامية",
            "حان وقت اختبار معلوماتك الدينية اليوم! أجب عن الأسئلة واكسب النقاط والعملات."
          ),
          Pair(
            "⭐ لا تفوت التحدي اليومي!",
            "حافظ على سلسلة انتصاراتك (Streak) اليومية وأجب عن أسئلة اليوم."
          ),
          Pair(
            "🎁 مكافأتك اليومية بانتظارك!",
            "سجل دخولك اليوم واستلم مكافأة اليوم من العملات ونقاط الخبرة."
          ),
          Pair(
            "📖 قال رسول الله ﷺ: «من سلك طريقاً يلتمس فيه علماً سهّل الله له به طريقاً إلى الجنة»",
            "تعلّم وتفقّه في دينك اليوم من خلال أسئلة التحدي الإسلامي."
          )
        )

        val selected = reminders.random()
        notificationManager.sendNotification(
          title = selected.first,
          message = selected.second,
          notificationId = AppNotificationManager.NOTIFICATION_ID_DAILY
        )

        // Ensure next reminder is scheduled
        notificationManager.scheduleDailyReminder()
      }
    }
  }
}
