package com.example.core.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.core.adhan.AsrJuristicMethod
import com.example.core.adhan.CalculationMethod
import com.example.core.adhan.PrayerName
import com.example.core.adhan.PrayerTimeItem
import com.example.core.adhan.PrayerTimesManager
import java.util.Calendar

class AppNotificationManager(private val context: Context) {

  companion object {
    const val CHANNEL_ID = "islamic_challenge_daily_reminders"
    const val CHANNEL_ID_ADHAN = "islamic_challenge_adhan_alerts"
    const val CHANNEL_ID_ADHAN_PLAYING = "islamic_challenge_adhan_playing"
    const val CHANNEL_ID_SALAWAT = "islamic_challenge_hourly_salawat"

    const val NOTIFICATION_ID_DAILY = 2001
    const val NOTIFICATION_ID_TEST = 2002
    const val NOTIFICATION_ID_REWARD = 2003
    const val NOTIFICATION_ID_CHALLENGE = 2004
    const val NOTIFICATION_ID_SALAWAT = 2005
    const val NOTIFICATION_ID_ADHAN_BASE = 5000

    const val ALARM_REQUEST_CODE = 3001
    const val PRAYER_ALARM_REQUEST_BASE = 6000
    const val SALAWAT_ALARM_REQUEST_CODE = 7001
    const val HOURLY_SALAWAT_INTERVAL_MS = 60L * 60L * 1000L

    private const val NOTIF_PREFS_NAME = "app_notification_prefs"
    private const val PRAYER_PREFS_NAME = "prayer_times_prefs"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_HOURLY_SALAWAT_ENABLED = "hourly_salawat_enabled"
    private const val KEY_IS_ARABIC = "is_arabic"
    private const val KEY_NEXT_SALAWAT_TRIGGER_MS = "next_salawat_trigger_ms"
  }

  private val notificationManager =
    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  private val notifPrefs by lazy {
    context.getSharedPreferences(NOTIF_PREFS_NAME, Context.MODE_PRIVATE)
  }

  init {
    createNotificationChannel()
  }

  fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "تحدي المعرفة الإسلامية - التذكيرات",
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "إشعارات التذكير اليومي والتحديات والمكافآت خارج التطبيق"
        enableVibration(true)
      }
      notificationManager.createNotificationChannel(channel)

      val adhanChannel = NotificationChannel(
        CHANNEL_ID_ADHAN,
        "مواقيت الصلاة وتنبيهات الأذان",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "تنبيهات دخول أوقات الصلوات الخمس"
        enableVibration(true)
        setSound(
          RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
      }
      notificationManager.createNotificationChannel(adhanChannel)

      // Silent notification channel for AdhanForegroundService so system ding never overlaps Adhan audio
      val adhanPlayingChannel = NotificationChannel(
        CHANNEL_ID_ADHAN_PLAYING,
        "تشغيل صوت الأذان",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "تشغيل صوت الأذان الكامل في الخلفية عند دخول وقت الصلاة"
        enableVibration(false)
        setSound(null, null)
      }
      notificationManager.createNotificationChannel(adhanPlayingChannel)

      // Silent notification channel for Hourly Salawat so voice TTS ("صلِّ على محمد") speaks cleanly
      val salawatChannel = NotificationChannel(
        CHANNEL_ID_SALAWAT,
        "تذكير الصلاة على النبي ﷺ كل ساعة",
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "تذكير صوتي وإشعار كل ساعة بالصلاة على النبي محمد ﷺ"
        enableVibration(true)
        setSound(null, null)
      }
      notificationManager.createNotificationChannel(salawatChannel)
    }
  }

  fun saveNotificationPreferences(
    notificationsEnabled: Boolean,
    hourlySalawatEnabled: Boolean,
    isArabic: Boolean
  ) {
    notifPrefs.edit()
      .putBoolean(KEY_NOTIFICATIONS_ENABLED, notificationsEnabled)
      .putBoolean(KEY_HOURLY_SALAWAT_ENABLED, hourlySalawatEnabled)
      .putBoolean(KEY_IS_ARABIC, isArabic)
      .apply()
  }

  fun isNotificationsEnabled(): Boolean = notifPrefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

  fun isHourlySalawatEnabled(): Boolean = notifPrefs.getBoolean(KEY_HOURLY_SALAWAT_ENABLED, true)

  fun isSavedArabic(): Boolean = notifPrefs.getBoolean(KEY_IS_ARABIC, true)

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

  fun canScheduleExactAlarms(): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      alarmManager.canScheduleExactAlarms()
    } else {
      true
    }
  }

  private fun scheduleAlarmWithBestPrecision(
    alarmManager: AlarmManager,
    triggerAtMillis: Long,
    pendingIntent: PendingIntent
  ) {
    try {
      if (canScheduleExactAlarms()) {
        alarmManager.setExactAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerAtMillis,
          pendingIntent
        )
      } else {
        alarmManager.setAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerAtMillis,
          pendingIntent
        )
      }
    } catch (_: SecurityException) {
      try {
        alarmManager.setAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerAtMillis,
          pendingIntent
        )
      } catch (_: Exception) {}
    } catch (_: Exception) {}
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

  fun sendPrayerNotification(
    prayerName: String,
    prayerTime: String,
    isArabic: Boolean = true,
    notificationId: Int = NOTIFICATION_ID_ADHAN_BASE
  ) {
    if (!hasNotificationPermission()) return

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      notificationId,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val title = if (isArabic) {
      "🕌 حان الآن موعد أذان صلاة $prayerName"
    } else {
      "🕌 Time for $prayerName Prayer"
    }
    val message = if (isArabic) {
      "الله أكبر، الله أكبر.. حان الآن موعد أذان $prayerName ($prayerTime). حيّ على الصلاة، حيّ على الفلاح."
    } else {
      "Allahu Akbar.. It is now time for $prayerName ($prayerTime). Come to prayer, come to success."
    }

    val builder = NotificationCompat.Builder(context, CHANNEL_ID_ADHAN)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(title)
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)

    try {
      NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    } catch (_: SecurityException) {}
  }

  fun sendSalawatNotification(isArabic: Boolean = true) {
    if (!hasNotificationPermission()) return

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      NOTIFICATION_ID_SALAWAT,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val phrasesAr = listOf(
      "اللهم صلِّ وسلم وبارك على سيدنا محمد وعلى آله وصحبه أجمعين",
      "من صلّى عليّ صلاةً صلّى الله عليه بها عشراً — اللهم صلِّ على محمد",
      "اللهم صلِّ على محمد وعلى آل محمد كما صليت على إبراهيم وعلى آل إبراهيم"
    )
    val phrasesEn = listOf(
      "Allahumma salli wa sallim 'ala Nabiyyina Muhammad ﷺ",
      "Whoever sends blessings upon me once, Allah sends blessings upon him tenfold ﷺ"
    )

    val title = if (isArabic) "ﷺ صلِّ على محمد" else "ﷺ Send Blessings Upon Muhammad"
    val message = if (isArabic) phrasesAr.random() else phrasesEn.random()

    val builder = NotificationCompat.Builder(context, CHANNEL_ID_SALAWAT)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(title)
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)

    try {
      NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SALAWAT, builder.build())
    } catch (_: SecurityException) {}
  }

  /**
   * Schedules alarms for all enabled obligatory prayers. If a prayer time has already passed
   * today, it automatically schedules tomorrow's occurrence of that prayer so Adhan works
   * continuously even when the app stays closed across midnight or multiple days.
   */
  fun schedulePrayerAlerts(
    prayers: List<PrayerTimeItem>,
    alertsEnabled: Map<PrayerName, Boolean>,
    isArabic: Boolean
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val now = System.currentTimeMillis()
    val tomorrowPrayers by lazy { computeTomorrowPrayersFromPrefs(now) }

    prayers.forEach { todayItem ->
      if (todayItem.prayer == PrayerName.SUNRISE) return@forEach
      val enabled = alertsEnabled[todayItem.prayer] ?: true
      val requestCode = PRAYER_ALARM_REQUEST_BASE + todayItem.prayer.ordinal

      val targetItem = if (todayItem.timestampMillis > now + 1_000L) {
        todayItem
      } else {
        tomorrowPrayers.firstOrNull { it.prayer == todayItem.prayer }
      }

      if (enabled && targetItem != null && targetItem.timestampMillis > now + 1_000L) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
          putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, if (isArabic) targetItem.prayer.arName else targetItem.prayer.enName)
          putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME, targetItem.getTimeString(isArabic))
          putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME_AR, targetItem.prayer.arName)
          putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME_EN, targetItem.prayer.enName)
          putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME_AR, targetItem.timeStringAr)
          putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME_EN, targetItem.timeStringEn)
          putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_ID, targetItem.prayer.ordinal)
          putExtra(PrayerAlarmReceiver.EXTRA_IS_ARABIC, isArabic)
        }
        val pendingIntent = PendingIntent.getBroadcast(
          context,
          requestCode,
          intent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        scheduleAlarmWithBestPrecision(alarmManager, targetItem.timestampMillis, pendingIntent)
      } else {
        val cancelIntent = Intent(context, PrayerAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
          context,
          requestCode,
          cancelIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
      }
    }
  }

  /**
   * Recomputes today's and tomorrow's prayer schedule directly from saved SharedPreferences
   * and ensures all enabled prayer alarms are scheduled. Safe to call from BroadcastReceivers.
   */
  fun scheduleAllPrayerAlertsFromPrefs() {
    val prefs = context.getSharedPreferences(PRAYER_PREFS_NAME, Context.MODE_PRIVATE)
    val city = PrayerTimesManager.loadSavedCityLocation(prefs)

    val savedMethodId = prefs.getString("calculation_method_id", null)
    val method = CalculationMethod.entries.find { it.id == savedMethodId } ?: city.defaultMethod

    val savedAsrId = prefs.getString("asr_method_id", null)
    val asr = AsrJuristicMethod.entries.find { it.id == savedAsrId } ?: AsrJuristicMethod.SHAFI

    val savedAlerts = PrayerName.entries.associateWith { prayer ->
      prefs.getBoolean("alert_${prayer.name}", prayer != PrayerName.SUNRISE)
    }

    val schedule = PrayerTimesManager.calculateSchedule(
      city = city,
      method = method,
      asrMethod = asr,
      nowMillis = System.currentTimeMillis()
    )
    schedulePrayerAlerts(schedule.todayPrayers, savedAlerts, isSavedArabic())
  }

  private fun computeTomorrowPrayersFromPrefs(nowMillis: Long): List<PrayerTimeItem> {
    val prefs = context.getSharedPreferences(PRAYER_PREFS_NAME, Context.MODE_PRIVATE)
    val city = PrayerTimesManager.loadSavedCityLocation(prefs)

    val savedMethodId = prefs.getString("calculation_method_id", null)
    val method = CalculationMethod.entries.find { it.id == savedMethodId } ?: city.defaultMethod

    val savedAsrId = prefs.getString("asr_method_id", null)
    val asr = AsrJuristicMethod.entries.find { it.id == savedAsrId } ?: AsrJuristicMethod.SHAFI

    val tomorrowCal = Calendar.getInstance().apply {
      timeInMillis = nowMillis
      add(Calendar.DAY_OF_YEAR, 1)
    }
    return PrayerTimesManager.calculatePrayerTimes(
      city = city,
      calendar = tomorrowCal,
      method = method,
      asrMethod = asr
    )
  }

  /**
   * Computes the exact top-of-the-hour timestamp (minute = 00, second = 00, ms = 000)
   * strictly after [nowMillis] by at least [minDelayMillis] to avoid double-triggers
   * if the OS wakes the alarm a few seconds early.
   */
  fun computeNextTopOfHourMillis(
    nowMillis: Long = System.currentTimeMillis(),
    minDelayMillis: Long = 5_000L
  ): Long {
    val cal = Calendar.getInstance().apply {
      timeInMillis = nowMillis
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      add(Calendar.HOUR_OF_DAY, 1)
    }
    while (cal.timeInMillis - nowMillis < minDelayMillis) {
      cal.add(Calendar.HOUR_OF_DAY, 1)
    }
    return cal.timeInMillis
  }

  fun scheduleHourlySalawatReminder(minDelayMillis: Long = 5_000L) {
    val triggerAt = computeNextTopOfHourMillis(
      nowMillis = System.currentTimeMillis(),
      minDelayMillis = minDelayMillis
    )
    notifPrefs.edit()
      .putBoolean(KEY_HOURLY_SALAWAT_ENABLED, true)
      .putLong(KEY_NEXT_SALAWAT_TRIGGER_MS, triggerAt)
      .apply()
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, SalawatAlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      SALAWAT_ALARM_REQUEST_CODE,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    scheduleAlarmWithBestPrecision(alarmManager, triggerAt, pendingIntent)
  }

  fun ensureHourlySalawatScheduled() {
    if (!isHourlySalawatEnabled()) return
    val intent = Intent(context, SalawatAlarmReceiver::class.java)
    val existing = PendingIntent.getBroadcast(
      context,
      SALAWAT_ALARM_REQUEST_CODE,
      intent,
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    val savedTrigger = notifPrefs.getLong(KEY_NEXT_SALAWAT_TRIGGER_MS, 0L)
    val isAlignedTopOfHour = if (savedTrigger > System.currentTimeMillis()) {
      val cal = Calendar.getInstance().apply { timeInMillis = savedTrigger }
      cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.SECOND) == 0
    } else {
      false
    }
    if (existing == null || !isAlignedTopOfHour) {
      scheduleHourlySalawatReminder()
    }
  }

  fun cancelHourlySalawatReminder() {
    notifPrefs.edit()
      .putBoolean(KEY_HOURLY_SALAWAT_ENABLED, false)
      .remove(KEY_NEXT_SALAWAT_TRIGGER_MS)
      .apply()
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, SalawatAlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      SALAWAT_ALARM_REQUEST_CODE,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
    pendingIntent.cancel()
  }

  fun rescheduleAllFromPrefs() {
    scheduleAllPrayerAlertsFromPrefs()
    if (isNotificationsEnabled()) {
      scheduleDailyReminder()
    }
    if (isHourlySalawatEnabled()) {
      scheduleHourlySalawatReminder()
    }
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
