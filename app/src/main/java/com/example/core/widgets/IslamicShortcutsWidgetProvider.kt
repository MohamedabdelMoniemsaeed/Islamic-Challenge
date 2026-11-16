package com.example.core.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.core.adhan.AsrJuristicMethod
import com.example.core.adhan.CalculationMethod
import com.example.core.adhan.PrayerTimesManager
import com.example.core.notification.AppNotificationManager
import com.example.ui.navigation.Routes

enum class ShortcutsWidgetLayoutTier(val layoutRes: Int) {
  MINI(R.layout.widget_shortcuts_mini),
  GRID_2X2(R.layout.widget_shortcuts_grid),
  BAR_4X1(R.layout.widget_islamic_shortcuts)
}

class IslamicShortcutsWidgetProvider : AppWidgetProvider() {

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

  companion object {
    fun updateAllWidgets(context: Context) {
      try {
        val manager = AppWidgetManager.getInstance(context) ?: return
        val component = ComponentName(context, IslamicShortcutsWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids != null && ids.isNotEmpty()) {
          for (id in ids) {
            updateSingleWidget(context, manager, id)
          }
        }
      } catch (_: Throwable) {}
    }

    fun selectTierForSize(widthDp: Int, heightDp: Int): ShortcutsWidgetLayoutTier {
      return when {
        widthDp < 120 -> ShortcutsWidgetLayoutTier.MINI
        widthDp < 220 || heightDp >= 120 -> ShortcutsWidgetLayoutTier.GRID_2X2
        else -> ShortcutsWidgetLayoutTier.BAR_4X1
      }
    }

    fun buildRemoteViews(context: Context): RemoteViews {
      return buildRemoteViewsForTier(context, ShortcutsWidgetLayoutTier.BAR_4X1)
    }

    fun buildRemoteViews(context: Context, widthDp: Int, heightDp: Int): RemoteViews {
      return buildRemoteViewsForTier(context, selectTierForSize(widthDp, heightDp))
    }

    fun buildRemoteViewsForTier(
      context: Context,
      tier: ShortcutsWidgetLayoutTier
    ): RemoteViews {
      val isAr = AppNotificationManager(context).isSavedArabic()
      val views = RemoteViews(context.packageName, tier.layoutRes)

      if (tier == ShortcutsWidgetLayoutTier.MINI) {
        val prefs = context.getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
        val city = PrayerTimesManager.loadSavedCityLocation(prefs)
        val savedMethodId = prefs.getString("calculation_method_id", null)
        val method = CalculationMethod.entries.find { it.id == savedMethodId } ?: city.defaultMethod
        val savedAsrId = prefs.getString("asr_method_id", null)
        val asr = AsrJuristicMethod.entries.find { it.id == savedAsrId } ?: AsrJuristicMethod.SHAFI
        val schedule = PrayerTimesManager.calculateSchedule(city, method, asr, System.currentTimeMillis())
        val nextName = if (isAr) schedule.nextPrayer.prayer.arName else schedule.nextPrayer.prayer.enName
        views.setTextViewText(R.id.shortcut_label_prayer, nextName)
        views.setOnClickPendingIntent(
          R.id.shortcut_tile_prayer,
          createRoutePendingIntent(context, 8201, Routes.PRAYER_TIMES)
        )
        return views
      }

      views.setTextViewText(R.id.shortcut_label_prayer, if (isAr) "المواقيت" else "Prayers")
      views.setTextViewText(R.id.shortcut_label_qibla, if (isAr) "القبلة" else "Qiblah")
      views.setTextViewText(R.id.shortcut_label_adhkar, if (isAr) "الأذكار" else "Adhkar")
      views.setTextViewText(R.id.shortcut_label_quiz, if (isAr) "التحدي" else "Quiz")

      views.setOnClickPendingIntent(
        R.id.shortcut_tile_prayer,
        createRoutePendingIntent(context, 8201, Routes.PRAYER_TIMES)
      )
      views.setOnClickPendingIntent(
        R.id.shortcut_tile_qibla,
        createRoutePendingIntent(context, 8202, Routes.PRAYER_TIMES)
      )
      views.setOnClickPendingIntent(
        R.id.shortcut_tile_adhkar,
        createRoutePendingIntent(context, 8203, Routes.ADHKAR_TASBIH)
      )
      views.setOnClickPendingIntent(
        R.id.shortcut_tile_quiz,
        createRoutePendingIntent(context, 8204, Routes.CATEGORIES)
      )

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
          SizeF(56f, 50f) to buildRemoteViewsForTier(context, ShortcutsWidgetLayoutTier.MINI),
          SizeF(130f, 100f) to buildRemoteViewsForTier(context, ShortcutsWidgetLayoutTier.GRID_2X2),
          SizeF(220f, 50f) to buildRemoteViewsForTier(context, ShortcutsWidgetLayoutTier.BAR_4X1)
        )
        appWidgetManager.updateAppWidget(appWidgetId, RemoteViews(sizeMapping))
      } else {
        val opts = explicitOptions ?: appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minW = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
        val minH = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 56)
        val views = buildRemoteViews(context, minW, minH)
        appWidgetManager.updateAppWidget(appWidgetId, views)
      }
    }
  }
}
