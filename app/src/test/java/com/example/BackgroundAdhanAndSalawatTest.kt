package com.example

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.speech.tts.Voice
import androidx.test.core.app.ApplicationProvider
import com.example.core.adhan.AdhanAudioCache
import com.example.core.adhan.AdhanForegroundService
import com.example.core.adhan.PrayerName
import com.example.core.adhan.PrayerTimesManager
import com.example.core.notification.AppNotificationManager
import com.example.core.notification.DailyReminderReceiver
import com.example.core.notification.PrayerAlarmReceiver
import com.example.core.notification.SalawatAlarmReceiver
import com.example.core.notification.SalawatVoicePlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.io.File
import java.util.Calendar
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BackgroundAdhanAndSalawatTest {

  private lateinit var context: Application
  private lateinit var alarmManager: AlarmManager
  private lateinit var notificationManager: AppNotificationManager

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    notificationManager = AppNotificationManager(context)
    context.getSharedPreferences("app_notification_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    context.getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE).edit().clear().commit()
  }

  @Test
  fun `schedulePrayerAlerts schedules future alarms even when today prayers have already passed`() {
    val cairo = PrayerTimesManager.CITIES.first { it.id == "cairo" }
    val pastPrayers = PrayerTimesManager.calculatePrayerTimes(cairo).map { item ->
      item.copy(timestampMillis = System.currentTimeMillis() - 3_600_000L)
    }
    val alerts = PrayerName.entries.associateWith { it != PrayerName.SUNRISE }

    notificationManager.schedulePrayerAlerts(pastPrayers, alerts, isArabic = true)

    val shadowAlarm = Shadows.shadowOf(alarmManager)
    val scheduled = shadowAlarm.scheduledAlarms
    val now = System.currentTimeMillis()

    // Should have scheduled 5 alarms (Fajr, Dhuhr, Asr, Maghrib, Isha) for tomorrow (future > now)
    assertEquals(5, scheduled.size)
    assertTrue(scheduled.all { it.triggerAtTime > now })
  }

  @Test
  fun `PrayerAlarmReceiver starts AdhanForegroundService and reschedules next prayer alarms`() {
    Shadows.shadowOf(context).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
    context.getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
      .edit()
      .putBoolean("auto_play_adhan", true)
      .commit()

    val receiver = PrayerAlarmReceiver()
    val alarmIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
      putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, "الفجر")
      putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME, "04:50 ص")
      putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME_AR, "الفجر")
      putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME_EN, "Fajr")
      putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME_AR, "04:50 ص")
      putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME_EN, "04:50 AM")
      putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_ID, PrayerName.FAJR.ordinal)
      putExtra(PrayerAlarmReceiver.EXTRA_IS_ARABIC, true)
    }

    receiver.onReceive(context, alarmIntent)

    val nextStartedService = Shadows.shadowOf(context).nextStartedService
    assertNotNull("PrayerAlarmReceiver must start AdhanForegroundService", nextStartedService)
    assertEquals(AdhanForegroundService::class.java.name, nextStartedService!!.component?.className)
    assertEquals(AdhanForegroundService.ACTION_PLAY_ADHAN, nextStartedService.action)
    assertEquals("الفجر", nextStartedService.getStringExtra(AdhanForegroundService.EXTRA_PRAYER_NAME_AR))

    // Also verifies upcoming prayer alarms were chained
    val scheduledAlarms = Shadows.shadowOf(alarmManager).scheduledAlarms
    assertEquals(5, scheduledAlarms.size)
  }

  @Test
  fun `hourly salawat reminder schedules strictly at minute 00 and toggles cleanly`() {
    Shadows.shadowOf(context).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)

    // Enable and schedule
    notificationManager.scheduleHourlySalawatReminder()
    assertTrue(notificationManager.isHourlySalawatEnabled())
    val shadowAlarm = Shadows.shadowOf(alarmManager)
    assertEquals(1, shadowAlarm.scheduledAlarms.size)
    @Suppress("DEPRECATION")
    val triggerTime = shadowAlarm.scheduledAlarms.first().triggerAtTime
    val triggerCal = Calendar.getInstance().apply { timeInMillis = triggerTime }
    assertEquals("Must trigger strictly at minute 00", 0, triggerCal.get(Calendar.MINUTE))
    assertEquals("Must trigger strictly at second 00", 0, triggerCal.get(Calendar.SECOND))
    assertEquals("Must trigger strictly at millisecond 0", 0, triggerCal.get(Calendar.MILLISECOND))
    assertTrue("Must be in the future", triggerTime > System.currentTimeMillis())

    // Simulate firing
    val salawatReceiver = SalawatAlarmReceiver()
    salawatReceiver.onReceive(context, Intent(context, SalawatAlarmReceiver::class.java))

    val sysNotifMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val salawatNotif = Shadows.shadowOf(sysNotifMgr)
      .getNotification(AppNotificationManager.NOTIFICATION_ID_SALAWAT)
    assertNotNull("Salawat notification must be posted", salawatNotif)
    // Next hour remains scheduled at minute 00
    assertEquals(1, shadowAlarm.scheduledAlarms.size)
    @Suppress("DEPRECATION")
    val nextCal = Calendar.getInstance().apply { timeInMillis = shadowAlarm.scheduledAlarms.first().triggerAtTime }
    assertEquals(0, nextCal.get(Calendar.MINUTE))
    assertEquals(0, nextCal.get(Calendar.SECOND))

    // Disable and cancel
    notificationManager.cancelHourlySalawatReminder()
    assertFalse(notificationManager.isHourlySalawatEnabled())
    assertEquals(0, shadowAlarm.scheduledAlarms.size)
  }

  @Test
  fun `computeNextTopOfHourMillis aligns to minute 00 and guards against early wakeups`() {
    val calMid = Calendar.getInstance().apply {
      set(2026, Calendar.NOVEMBER, 15, 14, 23, 17)
      set(Calendar.MILLISECOND, 450)
    }
    val nextFromMid = notificationManager.computeNextTopOfHourMillis(calMid.timeInMillis)
    val resMid = Calendar.getInstance().apply { timeInMillis = nextFromMid }
    assertEquals(15, resMid.get(Calendar.HOUR_OF_DAY))
    assertEquals(0, resMid.get(Calendar.MINUTE))
    assertEquals(0, resMid.get(Calendar.SECOND))
    assertEquals(0, resMid.get(Calendar.MILLISECOND))

    // If receiver fires 5 seconds early (13:59:55) with 60s guard, skip 14:00 and schedule 15:00
    val calEarly = Calendar.getInstance().apply {
      set(2026, Calendar.NOVEMBER, 15, 13, 59, 55)
      set(Calendar.MILLISECOND, 0)
    }
    val nextFromEarly = notificationManager.computeNextTopOfHourMillis(calEarly.timeInMillis, minDelayMillis = 60_000L)
    val resEarly = Calendar.getInstance().apply { timeInMillis = nextFromEarly }
    assertEquals(15, resEarly.get(Calendar.HOUR_OF_DAY))
    assertEquals(0, resEarly.get(Calendar.MINUTE))
    assertEquals(0, resEarly.get(Calendar.SECOND))
  }

  @Test
  fun `selectPreferredMaleArabicVoice chooses male voice over female voice`() {
    val arLocale = Locale("ar", "SA")
    val femaleArc = Voice("ar-xa-x-arc-local", arLocale, Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, emptySet())
    val femaleArz = Voice("ar-xa-x-arz-local", arLocale, Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, emptySet())
    val maleArdLocal = Voice("ar-xa-x-ard-local", arLocale, Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, emptySet())
    val maleAreNetwork = Voice("ar-xa-x-are-network", arLocale, Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, emptySet())
    val englishMale = Voice("en-us-x-sfg-male-local", Locale.US, Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, emptySet())

    // 1. Offline male voice wins over female voices and English male
    val pickOfflineMale = SalawatVoicePlayer.selectPreferredMaleArabicVoice(
      voices = listOf(englishMale, femaleArc, femaleArz, maleAreNetwork, maleArdLocal),
      isNetworkAvailable = true
    )
    assertNotNull(pickOfflineMale.voice)
    assertEquals("ar-xa-x-ard-local", pickOfflineMale.voice!!.name)
    assertTrue(pickOfflineMale.isConfirmedMale)

    // 2. Network male voice wins over offline female voice when online
    val pickNetworkMale = SalawatVoicePlayer.selectPreferredMaleArabicVoice(
      voices = listOf(femaleArc, femaleArz, maleAreNetwork),
      isNetworkAvailable = true
    )
    assertEquals("ar-xa-x-are-network", pickNetworkMale.voice!!.name)
    assertTrue(pickNetworkMale.isConfirmedMale)

    // 3. When offline and only female local exists, selects offline fallback with isConfirmedMale = false
    val pickOfflineFallback = SalawatVoicePlayer.selectPreferredMaleArabicVoice(
      voices = listOf(femaleArc, maleAreNetwork),
      isNetworkAvailable = false
    )
    assertEquals("ar-xa-x-arc-local", pickOfflineFallback.voice!!.name)
    assertFalse(pickOfflineFallback.isConfirmedMale)
  }

  @Test
  fun `boot and timezone broadcasts reschedule prayer alarms and enabled salawat reminder`() {
    notificationManager.saveNotificationPreferences(
      notificationsEnabled = true,
      hourlySalawatEnabled = true,
      isArabic = true
    )

    val bootReceiver = DailyReminderReceiver()
    bootReceiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))

    val scheduled = Shadows.shadowOf(alarmManager).scheduledAlarms
    // 5 prayers + 1 daily reminder + 1 hourly salawat = 7 alarms
    assertEquals(7, scheduled.size)
  }

  @Test
  fun `offline adhan and salawat synthesized WAV chimes are valid RIFF WAVE files`() {
    val adhanWav: File? = AdhanAudioCache.ensureOfflineChimeWav(context)
    assertNotNull(adhanWav)
    assertTrue(adhanWav!!.exists() && adhanWav.length() > 40_000L)
    val adhanBytes = adhanWav.readBytes()
    assertEquals("RIFF", String(adhanBytes.copyOfRange(0, 4)))
    assertEquals("WAVE", String(adhanBytes.copyOfRange(8, 12)))

    val salawatWav: File? = AdhanAudioCache.ensureSalawatChimeWav(context)
    assertNotNull(salawatWav)
    assertTrue(salawatWav!!.exists() && salawatWav.length() > 10_000L)
    val salawatBytes = salawatWav.readBytes()
    assertEquals("RIFF", String(salawatBytes.copyOfRange(0, 4)))
    assertEquals("WAVE", String(salawatBytes.copyOfRange(8, 12)))
  }

  @Test
  fun `daily reminder fires at most once per day and never immediately on schedule`() {
    Shadows.shadowOf(context).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
    notificationManager.saveNotificationPreferences(
      notificationsEnabled = true,
      hourlySalawatEnabled = false,
      isArabic = true
    )

    val sysNotifMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val shadowNotifMgr = Shadows.shadowOf(sysNotifMgr)

    // Scheduling daily reminder must NEVER fire a notification immediately
    notificationManager.ensureDailyReminderScheduled()
    assertEquals(0, shadowNotifMgr.allNotifications.size)

    // First alarm delivery sends 1 daily notification and marks today as sent
    val receiver = DailyReminderReceiver()
    receiver.onReceive(context, Intent(context, DailyReminderReceiver::class.java))
    assertEquals(1, shadowNotifMgr.allNotifications.size)
    assertTrue(notificationManager.hasSentDailyReminderToday())

    // Subsequent trigger on the same day must be skipped (still only 1 notification)
    sysNotifMgr.cancelAll()
    receiver.onReceive(context, Intent(context, DailyReminderReceiver::class.java))
    assertEquals(0, shadowNotifMgr.allNotifications.size)

    // Next scheduled daily alarm must be strictly tomorrow (> now + 60_000ms and tomorrow's day)
    val scheduled = Shadows.shadowOf(alarmManager).scheduledAlarms.lastOrNull()
    assertNotNull(scheduled)
    @Suppress("DEPRECATION")
    assertTrue(scheduled!!.triggerAtTime > System.currentTimeMillis() + 60_000L)
  }
}
