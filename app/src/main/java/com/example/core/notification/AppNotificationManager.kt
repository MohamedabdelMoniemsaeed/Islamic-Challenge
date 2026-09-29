package com.example.core.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

class AppNotificationManager(private val context: Context) {

  companion object {
    const val CHANNEL_ID = "islamic_challenge_daily_reminders"
    const val NOTIFICATION_ID_DAILY = 2001
    const val NOTIFICATION_ID_TEST = 2002
    const val NOTIFICATION_ID_REWARD = 2003
    const val NOTIFICATION_ID_CHALLENGE = 2004
    const val ALARM_REQUEST_CODE = 3001
  }

  private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  init {
    createNotificationChannel()
  }

  fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = "تحدي المعرفة الإسلامية - التذكيرات"
      val descriptionText = "إشعارات التذكير اليومي والتحديات والمكافآت خارج التطبيق"
      val importance = NotificationManager.IMPORTANCE_DEFAULT
      val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
        enableVibration(true)
      }
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun hasNotificationPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
    } else {
      NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
  }

  fun sendNotification(
    title: String,
    message: String,
    notificationId: Int = NOTIFICATION_ID_DAILY
  ) {
    if (!hasNotificationPermission()) {
      return
    }

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val pendingIntent = PendingIntent.getActivity(
      context,
      notificationId,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(title)
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)

    try {
      NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    } catch (_: SecurityException) {
      // Gracefully handle if permission was revoked
    }
  }

  fun sendTestNotification(isArabic: Boolean) {
    val title = if (isArabic) "🌙 تحدي المعرفة الإسلامية" else "🌙 Islamic Challenge"
    val message = if (isArabic) {
      "تم تفعيل الإشعارات بنجاح! ستصلك التذكيرات اليومية خارج التطبيق في شريط الإشعارات هنا."
    } else {
      "Notifications activated! Daily reminders will now appear outside the app in your notification bar."
    }
    sendNotification(title, message, NOTIFICATION_ID_TEST)
  }

  fun sendDailyRewardNotification(isArabic: Boolean) {
    val title = if (isArabic) "🎁 مكافأتك اليومية جاهزة!" else "🎁 Daily Reward Ready!"
    val message = if (isArabic) {
      "مكافأتك اليومية بانتظارك، افتح التطبيق لاستلام العملات ونقاط الخبرة والوسائل المساعدة!"
    } else {
      "Your daily reward is waiting! Open the app to claim coins, XP, and lifelines."
    }
    sendNotification(title, message, NOTIFICATION_ID_REWARD)
  }

  fun sendDailyChallengeNotification(isArabic: Boolean) {
    val title = if (isArabic) "⭐ تحدي اليوم بانتظارك!" else "⭐ Daily Challenge is Live!"
    val message = if (isArabic) {
      "أسئلة اليوم الجديدة متاحة الآن! أجب وحافظ على سلسلة أيامك ونقاطك."
    } else {
      "New questions are available! Answer today's challenge to maintain your streak."
    }
    sendNotification(title, message, NOTIFICATION_ID_CHALLENGE)
  }

  fun scheduleDailyReminder(hour: Int = 12, minute: Int = 0) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, DailyReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      ALARM_REQUEST_CODE,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      if (before(Calendar.getInstance())) {
        add(Calendar.DAY_OF_YEAR, 1)
      }
    }

    try {
      alarmManager.setInexactRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
      )
    } catch (_: Exception) {
      // Inexact repeating is standard and safe on all API levels
    }
  }

  fun cancelDailyReminder() {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, DailyReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      ALARM_REQUEST_CODE,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
  }
}
