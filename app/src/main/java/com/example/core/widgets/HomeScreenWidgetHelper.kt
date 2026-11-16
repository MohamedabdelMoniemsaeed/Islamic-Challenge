package com.example.core.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity
import com.example.R
import com.example.ui.navigation.Routes

enum class HomeShortcutTarget(
  val id: String,
  val route: String,
  val labelAr: String,
  val labelEn: String,
  @DrawableRes val iconRes: Int
) {
  PRAYER_TIMES(
    id = "pinned_prayer_times",
    route = Routes.PRAYER_TIMES,
    labelAr = "مواقيت الصلاة",
    labelEn = "Prayer Times",
    iconRes = R.drawable.ic_widget_prayer
  ),
  QIBLAH(
    id = "pinned_qiblah",
    route = Routes.PRAYER_TIMES,
    labelAr = "اتجاه القبلة",
    labelEn = "Qiblah",
    iconRes = R.drawable.ic_widget_qibla
  ),
  ADHKAR_TASBIH(
    id = "pinned_adhkar",
    route = Routes.ADHKAR_TASBIH,
    labelAr = "الأذكار والمسبحة",
    labelEn = "Adhkar & Tasbih",
    iconRes = R.drawable.ic_widget_adhkar
  ),
  QUIZ_CHALLENGE(
    id = "pinned_quiz",
    route = Routes.CATEGORIES,
    labelAr = "التحدي الإسلامي",
    labelEn = "Islamic Quiz",
    iconRes = R.drawable.ic_widget_quiz
  )
}

object HomeScreenWidgetHelper {

  const val EXTRA_OPEN_ROUTE = "extra_open_route"

  fun updateAllHomeWidgets(context: Context) {
    PrayerTimesWidgetProvider.updateAllWidgets(context)
    IslamicShortcutsWidgetProvider.updateAllWidgets(context)
  }

  fun isPinAppWidgetSupported(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
    return try {
      AppWidgetManager.getInstance(context)?.isRequestPinAppWidgetSupported == true
    } catch (_: Throwable) {
      false
    }
  }

  fun requestPinPrayerTimesWidget(context: Context): Boolean {
    return requestPinWidget(context, PrayerTimesWidgetProvider::class.java)
  }

  fun requestPinShortcutsWidget(context: Context): Boolean {
    return requestPinWidget(context, IslamicShortcutsWidgetProvider::class.java)
  }

  private fun requestPinWidget(context: Context, providerClass: Class<*>): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
    return try {
      val manager = AppWidgetManager.getInstance(context) ?: return false
      if (!manager.isRequestPinAppWidgetSupported) return false
      val component = ComponentName(context, providerClass)
      manager.requestPinAppWidget(component, null, null)
    } catch (_: Throwable) {
      false
    }
  }

  fun requestPinShortcutIcon(
    context: Context,
    target: HomeShortcutTarget,
    isArabic: Boolean
  ): Boolean {
    return try {
      if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
        return false
      }
      val label = if (isArabic) target.labelAr else target.labelEn
      val intent = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        putExtra(EXTRA_OPEN_ROUTE, target.route)
      }
      val shortcut = ShortcutInfoCompat.Builder(context, target.id)
        .setShortLabel(label)
        .setLongLabel(label)
        .setIcon(IconCompat.createWithResource(context, target.iconRes))
        .setIntent(intent)
        .build()

      ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    } catch (_: Throwable) {
      false
    }
  }
}
