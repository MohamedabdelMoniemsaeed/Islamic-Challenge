package com.example

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.core.adhan.PrayerName
import com.example.core.adhan.PrayerTimeItem
import com.example.core.adhan.PrayerTimesManager
import com.example.core.notification.AppNotificationManager
import com.example.core.notification.PrayerAlarmReceiver
import com.example.core.widgets.HomeScreenWidgetHelper
import com.example.core.widgets.HomeShortcutTarget
import com.example.core.widgets.IslamicShortcutsWidgetProvider
import com.example.core.widgets.PrayerTimesWidgetProvider
import com.example.ui.navigation.Routes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HomeWidgetsAndExactAdhanTest {

  private lateinit var context: Application
  private lateinit var alarmManager: AlarmManager
  private lateinit var notificationManager: AppNotificationManager

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    ShadowAlarmManager.setCanScheduleExactAlarms(true)
    notificationManager = AppNotificationManager(context)
    context.getSharedPreferences("app_notification_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    context.getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE).edit().clear().commit()
  }

  @Test
  fun `schedulePrayerAlerts uses setAlarmClock for exact zero-delay Adhan timing`() {
    val shadowAlarm = Shadows.shadowOf(alarmManager)
    val now = System.currentTimeMillis()
    val futurePrayers = listOf(
      PrayerTimeItem(PrayerName.FAJR, "05:00 ص", "05:00 AM", now + 3_600_000L, isNext = true),
      PrayerTimeItem(PrayerName.SUNRISE, "06:20 ص", "06:20 AM", now + 7_200_000L),
      PrayerTimeItem(PrayerName.DHUHR, "12:00 م", "12:00 PM", now + 10_800_000L),
      PrayerTimeItem(PrayerName.ASR, "03:15 م", "03:15 PM", now + 14_400_000L),
      PrayerTimeItem(PrayerName.MAGHRIB, "05:30 م", "05:30 PM", now + 18_000_000L),
      PrayerTimeItem(PrayerName.ISHA, "06:50 م", "06:50 PM", now + 21_600_000L)
    )
    val alerts = PrayerName.entries.associateWith { it != PrayerName.SUNRISE }

    notificationManager.schedulePrayerAlerts(futurePrayers, alerts, isArabic = true)

    val nextAlarmClock = alarmManager.nextAlarmClock
    assertNotNull("Expected AlarmClockInfo to be registered via setAlarmClock()", nextAlarmClock)
    assertEquals(now + 3_600_000L, nextAlarmClock!!.triggerTime)

    val showIntent = Shadows.shadowOf(nextAlarmClock.showIntent).savedIntent
    assertEquals(MainActivity::class.java.name, showIntent.component?.className)
    assertEquals(Routes.PRAYER_TIMES, showIntent.getStringExtra(HomeScreenWidgetHelper.EXTRA_OPEN_ROUTE))
    assertEquals(5, shadowAlarm.scheduledAlarms.size)
  }

  @Test
  fun `skipJustTriggeredWithinMs skips prayer triggering within 60s window and chains tomorrow`() {
    val now = System.currentTimeMillis()
    val almostNowFajr = PrayerTimeItem(
      prayer = PrayerName.FAJR,
      timeStringAr = "05:00 ص",
      timeStringEn = "05:00 AM",
      timestampMillis = now + 20_000L, // e.g. OS woke receiver 20s early
      isNext = true
    )
    val alerts = mapOf(PrayerName.FAJR to true)

    notificationManager.schedulePrayerAlerts(
      prayers = listOf(almostNowFajr),
      alertsEnabled = alerts,
      isArabic = true,
      skipJustTriggeredWithinMs = 60_000L
    )

    val scheduledClock = alarmManager.nextAlarmClock
    assertNotNull(scheduledClock)
    assertTrue(
      "Re-chained Fajr alarm must skip today's imminent slot and schedule tomorrow (> now + 12h)",
      scheduledClock!!.triggerTime > now + 12L * 3_600_000L
    )
  }

  @Test
  fun `PrayerTimesWidgetProvider builds non-null RemoteViews in Arabic and English`() {
    notificationManager.saveNotificationPreferences(
      notificationsEnabled = true,
      hourlySalawatEnabled = true,
      isArabic = true
    )
    val arViews = PrayerTimesWidgetProvider.buildRemoteViews(context)
    assertNotNull(arViews)
    assertEquals(R.layout.widget_prayer_times, arViews.layoutId)

    notificationManager.saveNotificationPreferences(
      notificationsEnabled = true,
      hourlySalawatEnabled = true,
      isArabic = false
    )
    val enViews = PrayerTimesWidgetProvider.buildRemoteViews(context)
    assertNotNull(enViews)
    assertEquals(R.layout.widget_prayer_times, enViews.layoutId)
  }

  @Test
  fun `IslamicShortcutsWidgetProvider builds non-null RemoteViews and targets valid routes`() {
    notificationManager.saveNotificationPreferences(
      notificationsEnabled = true,
      hourlySalawatEnabled = true,
      isArabic = true
    )
    val arViews = IslamicShortcutsWidgetProvider.buildRemoteViews(context)
    assertNotNull(arViews)
    assertEquals(R.layout.widget_islamic_shortcuts, arViews.layoutId)

    val targets = HomeShortcutTarget.entries
    assertEquals(4, targets.size)
    assertTrue(targets.any { it.route == Routes.PRAYER_TIMES })
    assertTrue(targets.any { it.route == Routes.ADHKAR_TASBIH })
    assertTrue(targets.any { it.route == Routes.CATEGORIES })
  }

  @Test
  fun `PrayerAlarmReceiver handles early wakeup and chains tomorrow cleanly`() {
    Shadows.shadowOf(context).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
    val receiver = PrayerAlarmReceiver()
    val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
      putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME_AR, "العصر")
      putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME_EN, "Asr")
      putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME_AR, "03:15 م")
      putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME_EN, "03:15 PM")
      putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_ID, PrayerName.ASR.ordinal)
      putExtra(PrayerAlarmReceiver.EXTRA_IS_ARABIC, true)
    }

    receiver.onReceive(context, intent)

    val nextAlarm = alarmManager.nextAlarmClock
    assertNotNull("Next exact AlarmClock must remain scheduled after PrayerAlarmReceiver fires", nextAlarm)
    assertTrue(nextAlarm!!.triggerTime > System.currentTimeMillis() + 55_000L)
  }
}
