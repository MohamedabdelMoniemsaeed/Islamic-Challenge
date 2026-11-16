package com.example.core.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.core.adhan.AsrJuristicMethod
import com.example.core.adhan.CalculationMethod
import com.example.core.adhan.PrayerName
import com.example.core.adhan.PrayerTimesManager
import com.example.core.notification.AppNotificationManager
import com.example.ui.navigation.Routes

class PrayerTimesWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    for (appWidgetId in appWidgetIds) {
      updateSingleWidget(context, appWidgetManager, appWidgetId)
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    if (intent.action == ACTION_REFRESH_WIDGET) {
      updateAllWidgets(context)
    }
  }

  companion object {
    const val ACTION_REFRESH_WIDGET = "com.example.action.REFRESH_PRAYER_WIDGET"

    fun updateAllWidgets(context: Context) {
      try {
        val manager = AppWidgetManager.getInstance(context) ?: return
        val component = ComponentName(context, PrayerTimesWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids != null && ids.isNotEmpty()) {
          for (id in ids) {
            updateSingleWidget(context, manager, id)
          }
        }
      } catch (_: Throwable) {}
    }

    fun buildRemoteViews(context: Context): RemoteViews {
      val appNotif = AppNotificationManager(context)
      val isAr = appNotif.isSavedArabic()
      val prefs = context.getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
      val city = PrayerTimesManager.loadSavedCityLocation(prefs)
      val savedMethodId = prefs.getString("calculation_method_id", null)
      val method = CalculationMethod.entries.find { it.id == savedMethodId } ?: city.defaultMethod
      val savedAsrId = prefs.getString("asr_method_id", null)
      val asr = AsrJuristicMethod.entries.find { it.id == savedAsrId } ?: AsrJuristicMethod.SHAFI

      val nowMillis = System.currentTimeMillis()
      val schedule = PrayerTimesManager.calculateSchedule(
        city = city,
        method = method,
        asrMethod = asr,
        nowMillis = nowMillis
      )
      val hijri = PrayerTimesManager.getHijriDateString(
        isArabic = isAr,
        timeZoneId = city.timeZoneId,
        nowMillis = nowMillis
      )

      val views = RemoteViews(context.packageName, R.layout.widget_prayer_times)
      val cityTitle = if (isAr) city.nameAr else city.nameEn
      views.setTextViewText(R.id.widget_city_hijri, "$cityTitle • $hijri")

      val next = schedule.nextPrayer
      val nextLabel = if (isAr) {
        val tomorrowTag = if (next.isTomorrow) " (غداً)" else ""
        "الصلاة القادمة: ${next.prayer.arName}$tomorrowTag"
      } else {
        val tomorrowTag = if (next.isTomorrow) " (Tomorrow)" else ""
        "Next: ${next.prayer.enName}$tomorrowTag"
      }
      views.setTextViewText(R.id.widget_next_prayer_label, nextLabel)
      views.setTextViewText(R.id.widget_next_prayer_time, next.getTimeString(isAr))

      fun bindSlot(prayer: PrayerName, boxId: Int, nameId: Int, timeId: Int) {
        val item = schedule.todayPrayers.firstOrNull { it.prayer == prayer } ?: return
        views.setTextViewText(nameId, if (isAr) prayer.arName else prayer.enName)
        views.setTextViewText(
          timeId,
          item.getTimeString(isAr).replace(" ص", "").replace(" م", "").replace(" AM", "").replace(" PM", "")
        )
        val isHighlighted = item.isNext || (!next.isTomorrow && next.prayer == prayer)
        views.setInt(
          boxId,
          "setBackgroundResource",
          if (isHighlighted) R.drawable.widget_pill_highlight else R.drawable.widget_tile_bg
        )
      }

      bindSlot(PrayerName.FAJR, R.id.widget_fajr_box, R.id.widget_fajr_name, R.id.widget_fajr_time)
      bindSlot(PrayerName.DHUHR, R.id.widget_dhuhr_box, R.id.widget_dhuhr_name, R.id.widget_dhuhr_time)
      bindSlot(PrayerName.ASR, R.id.widget_asr_box, R.id.widget_asr_name, R.id.widget_asr_time)
      bindSlot(PrayerName.MAGHRIB, R.id.widget_maghrib_box, R.id.widget_maghrib_name, R.id.widget_maghrib_time)
      bindSlot(PrayerName.ISHA, R.id.widget_isha_box, R.id.widget_isha_name, R.id.widget_isha_time)

      val openIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(HomeScreenWidgetHelper.EXTRA_OPEN_ROUTE, Routes.PRAYER_TIMES)
      }
      val openPendingIntent = PendingIntent.getActivity(
        context,
        8101,
        openIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      views.setOnClickPendingIntent(R.id.widget_prayer_root, openPendingIntent)

      val refreshIntent = Intent(context, PrayerTimesWidgetProvider::class.java).apply {
        action = ACTION_REFRESH_WIDGET
      }
      val refreshPendingIntent = PendingIntent.getBroadcast(
        context,
        8102,
        refreshIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      views.setOnClickPendingIntent(R.id.widget_refresh_btn, refreshPendingIntent)

      return views
    }

    private fun updateSingleWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int
    ) {
      val views = buildRemoteViews(context)
      appWidgetManager.updateAppWidget(appWidgetId, views)
    }
  }
}
