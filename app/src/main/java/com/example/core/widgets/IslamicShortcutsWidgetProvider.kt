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
import com.example.core.notification.AppNotificationManager
import com.example.ui.navigation.Routes

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

    fun buildRemoteViews(context: Context): RemoteViews {
      val isAr = AppNotificationManager(context).isSavedArabic()
      val views = RemoteViews(context.packageName, R.layout.widget_islamic_shortcuts)

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
      appWidgetId: Int
    ) {
      val views = buildRemoteViews(context)
      appWidgetManager.updateAppWidget(appWidgetId, views)
    }
  }
}
