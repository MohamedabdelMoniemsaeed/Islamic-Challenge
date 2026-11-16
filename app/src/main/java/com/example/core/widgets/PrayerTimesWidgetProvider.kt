package com.example.core.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.SizeF
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.core.adhan.AsrJuristicMethod
import com.example.core.adhan.CalculationMethod
import com.example.core.adhan.PrayerName
import com.example.core.adhan.PrayerTimesManager
import com.example.core.adhan.QiblahCalculator
import com.example.core.notification.AppNotificationManager
import com.example.ui.navigation.Routes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

enum class PrayerWidgetLayoutTier(val layoutRes: Int) {
  MINI(R.layout.widget_prayer_mini),
  COMPACT(R.layout.widget_prayer_compact),
  MEDIUM(R.layout.widget_prayer_times),
  EXPANDED(R.layout.widget_prayer_expanded)
}

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

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle
  ) {
    super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    updateSingleWidget(context, appWidgetManager, appWidgetId, newOptions)
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

    fun selectTierForSize(widthDp: Int, heightDp: Int): PrayerWidgetLayoutTier {
      return when {
        widthDp < 120 -> PrayerWidgetLayoutTier.MINI
        widthDp < 220 -> PrayerWidgetLayoutTier.COMPACT
        heightDp >= 190 -> PrayerWidgetLayoutTier.EXPANDED
        else -> PrayerWidgetLayoutTier.MEDIUM
      }
    }

    fun buildRemoteViews(context: Context): RemoteViews {
      return buildRemoteViewsForTier(context, PrayerWidgetLayoutTier.MEDIUM)
    }

    fun buildRemoteViews(context: Context, widthDp: Int, heightDp: Int): RemoteViews {
      return buildRemoteViewsForTier(context, selectTierForSize(widthDp, heightDp))
    }

    fun buildRemoteViewsForTier(
      context: Context,
      tier: PrayerWidgetLayoutTier
    ): RemoteViews {
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
      val locale = if (isAr) Locale("ar") else Locale.US
      val gregorian = SimpleDateFormat("EEE d MMM", locale).apply {
        timeZone = TimeZone.getTimeZone(city.timeZoneId)
      }.format(Date(nowMillis))

      val bearing = QiblahCalculator.calculateQiblahBearing(city.latitude, city.longitude)
      val bearingInt = bearing.roundToInt()
      val dirLabel = if (isAr) {
        QiblahCalculator.getDirectionLabelAr(bearing)
      } else {
        QiblahCalculator.getDirectionLabelEn(bearing)
      }

      val next = schedule.nextPrayer
      val remainingMs = (next.timestampMillis - nowMillis).coerceAtLeast(0L)
      val chronoBase = SystemClock.elapsedRealtime() + remainingMs

      val views = RemoteViews(context.packageName, tier.layoutRes)
      val cityTitle = if (isAr) city.nameAr else city.nameEn

      val openPrayerIntent = createRoutePendingIntent(context, 8101, Routes.PRAYER_TIMES)
      val refreshIntent = PendingIntent.getBroadcast(
        context,
        8102,
        Intent(context, PrayerTimesWidgetProvider::class.java).apply { action = ACTION_REFRESH_WIDGET },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      views.setOnClickPendingIntent(R.id.widget_prayer_root, openPrayerIntent)

      // Live countdown timer (system Chronometer in countdown mode, API 24+)
      views.setChronometerCountDown(R.id.widget_countdown_chrono, true)
      views.setChronometer(R.id.widget_countdown_chrono, chronoBase, null, true)

      if (tier == PrayerWidgetLayoutTier.MINI) {
        val nextName = if (isAr) next.prayer.arName else next.prayer.enName
        views.setTextViewText(R.id.widget_mini_prayer_name, nextName)
        views.setTextViewText(R.id.widget_mini_prayer_time, next.getTimeString(isAr))
        return views
      }

      // Common fields across COMPACT, MEDIUM, EXPANDED
      views.setTextViewText(R.id.widget_city_hijri, "$cityTitle • $hijri")
      views.setOnClickPendingIntent(R.id.widget_refresh_btn, refreshIntent)

      val nextLabel = if (isAr) {
        val tomorrowTag = if (next.isTomorrow) " (غداً)" else ""
        if (tier == PrayerWidgetLayoutTier.COMPACT) "القادمة: ${next.prayer.arName}$tomorrowTag"
        else "الصلاة القادمة: ${next.prayer.arName}$tomorrowTag"
      } else {
        val tomorrowTag = if (next.isTomorrow) " (Tmrw)" else ""
        "Next: ${next.prayer.enName}$tomorrowTag"
      }
      views.setTextViewText(R.id.widget_next_prayer_label, nextLabel)
      views.setTextViewText(R.id.widget_next_prayer_time, next.getTimeString(isAr))

      val qiblahText = if (isAr) "القبلة $bearingInt° ($dirLabel)" else "Qiblah $bearingInt° ($dirLabel)"
      views.setTextViewText(R.id.widget_qiblah_badge, qiblahText)

      if (tier == PrayerWidgetLayoutTier.COMPACT) {
        views.setTextViewText(R.id.widget_countdown_prefix, if (isAr) "متبقي: " else "Left: ")
        return views
      }

      // Bind all 6 prayers (including Sunrise) for MEDIUM and EXPANDED
      fun bindSlot(prayer: PrayerName, boxId: Int, nameId: Int, timeId: Int, includeAmPm: Boolean) {
        val item = schedule.todayPrayers.firstOrNull { it.prayer == prayer } ?: return
        val baseName = if (isAr) prayer.arName else prayer.enName
        val alertEnabled = prayer != PrayerName.SUNRISE && prefs.getBoolean("alert_${prayer.name}", true)
        val displayName = if (tier == PrayerWidgetLayoutTier.EXPANDED && alertEnabled) {
          "$baseName 🔔"
        } else {
          baseName
        }
        views.setTextViewText(nameId, displayName)
        val rawTime = item.getTimeString(isAr)
        val formattedTime = if (includeAmPm) {
          rawTime
        } else {
          rawTime.replace(" ص", "").replace(" م", "").replace(" AM", "").replace(" PM", "")
        }
        views.setTextViewText(timeId, formattedTime)

        val isHighlighted = item.isNext || (!next.isTomorrow && next.prayer == prayer)
        views.setInt(
          boxId,
          "setBackgroundResource",
          if (isHighlighted) R.drawable.widget_pill_highlight else R.drawable.widget_tile_bg
        )
      }

      val showAmPm = tier == PrayerWidgetLayoutTier.EXPANDED
      bindSlot(PrayerName.FAJR, R.id.widget_fajr_box, R.id.widget_fajr_name, R.id.widget_fajr_time, showAmPm)
      bindSlot(PrayerName.SUNRISE, R.id.widget_sunrise_box, R.id.widget_sunrise_name, R.id.widget_sunrise_time, showAmPm)
      bindSlot(PrayerName.DHUHR, R.id.widget_dhuhr_box, R.id.widget_dhuhr_name, R.id.widget_dhuhr_time, showAmPm)
      bindSlot(PrayerName.ASR, R.id.widget_asr_box, R.id.widget_asr_name, R.id.widget_asr_time, showAmPm)
      bindSlot(PrayerName.MAGHRIB, R.id.widget_maghrib_box, R.id.widget_maghrib_name, R.id.widget_maghrib_time, showAmPm)
      bindSlot(PrayerName.ISHA, R.id.widget_isha_box, R.id.widget_isha_name, R.id.widget_isha_time, showAmPm)

      if (tier == PrayerWidgetLayoutTier.EXPANDED) {
        val methodLabel = if (isAr) method.nameAr else method.nameEn
        views.setTextViewText(R.id.widget_method_gregorian, "$gregorian • $methodLabel")
        views.setTextViewText(R.id.widget_countdown_prefix, if (isAr) "متبقي على الأذان: " else "Time left: ")
        views.setTextViewText(R.id.widget_adhkar_btn_label, if (isAr) "الأذكار" else "Adhkar")
        views.setTextViewText(R.id.widget_quiz_btn_label, if (isAr) "التحدي" else "Quiz")

        views.setOnClickPendingIntent(R.id.widget_action_qiblah, openPrayerIntent)
        views.setOnClickPendingIntent(
          R.id.widget_action_adhkar,
          createRoutePendingIntent(context, 8103, Routes.ADHKAR_TASBIH)
        )
        views.setOnClickPendingIntent(
          R.id.widget_action_quiz,
          createRoutePendingIntent(context, 8104, Routes.CATEGORIES)
        )
      }

      return views
    }

    private fun createRoutePendingIntent(
      context: Context,
      requestCode: Int,
      route: String
    ): PendingIntent {
      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(HomeScreenWidgetHelper.EXTRA_OPEN_ROUTE, route)
      }
      return PendingIntent.getActivity(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
    }

    private fun updateSingleWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int,
      explicitOptions: Bundle? = null
    ) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val sizeMapping: Map<SizeF, RemoteViews> = mapOf(
          SizeF(56f, 50f) to buildRemoteViewsForTier(context, PrayerWidgetLayoutTier.MINI),
          SizeF(130f, 50f) to buildRemoteViewsForTier(context, PrayerWidgetLayoutTier.COMPACT),
          SizeF(230f, 100f) to buildRemoteViewsForTier(context, PrayerWidgetLayoutTier.MEDIUM),
          SizeF(230f, 200f) to buildRemoteViewsForTier(context, PrayerWidgetLayoutTier.EXPANDED)
        )
        appWidgetManager.updateAppWidget(appWidgetId, RemoteViews(sizeMapping))
      } else {
        val opts = explicitOptions ?: appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minW = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
        val minH = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)
        val views = buildRemoteViews(context, minW, minH)
        appWidgetManager.updateAppWidget(appWidgetId, views)
      }
    }
  }
}
