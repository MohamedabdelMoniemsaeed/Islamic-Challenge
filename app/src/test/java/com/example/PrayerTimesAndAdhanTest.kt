package com.example

import com.example.core.adhkar.AdhkarCategory
import com.example.core.adhkar.AdhkarRepository
import com.example.core.adhan.AsrJuristicMethod
import com.example.core.adhan.CalculationMethod
import com.example.core.adhan.PrayerName
import com.example.core.adhan.PrayerTimesManager
import com.example.core.adhan.QiblahCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class PrayerTimesAndAdhanTest {

  @Test
  fun `prayer times are strictly ordered and Dhuhr is near solar noon`() {
    val cities = listOf("cairo", "makkah", "riyadh", "istanbul", "rabat")
    for (cityId in cities) {
      val city = PrayerTimesManager.CITIES.first { it.id == cityId }
      val tz = TimeZone.getTimeZone(city.timeZoneId)
      val midMorning = Calendar.getInstance(tz).apply {
        set(2025, Calendar.MARCH, 15, 9, 0, 0)
        set(Calendar.MILLISECOND, 0)
      }

      val schedule = PrayerTimesManager.calculateSchedule(
        city = city,
        method = city.defaultMethod,
        asrMethod = AsrJuristicMethod.SHAFI,
        nowMillis = midMorning.timeInMillis
      )

      val prayers = schedule.todayPrayers
      assertEquals(6, prayers.size)
      for (i in 0 until prayers.lastIndex) {
        assertTrue(
          "In ${city.id}, ${prayers[i].prayer} must precede ${prayers[i + 1].prayer}",
          prayers[i].timestampMillis < prayers[i + 1].timestampMillis
        )
      }

      // Verify Dhuhr is between 11:00 and 14:30 local time (proving no 12h atan quadrant shift)
      val dhuhr = prayers.first { it.prayer == PrayerName.DHUHR }
      val dhuhrCal = Calendar.getInstance(tz).apply { timeInMillis = dhuhr.timestampMillis }
      val dhuhrHour = dhuhrCal.get(Calendar.HOUR_OF_DAY)
      assertTrue("Dhuhr hour in ${city.id} was $dhuhrHour", dhuhrHour in 11..14)
    }
  }

  @Test
  fun `next prayer never selects SUNRISE and rolls over to tomorrow Fajr after Isha`() {
    val cairo = PrayerTimesManager.CITIES.first { it.id == "cairo" }
    val tz = TimeZone.getTimeZone(cairo.timeZoneId)

    // 1. Just after Fajr -> next obligatory prayer MUST be DHUHR, never SUNRISE
    val baseline = Calendar.getInstance(tz).apply {
      set(2025, Calendar.JUNE, 21, 6, 0, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val morningSchedule = PrayerTimesManager.calculateSchedule(cairo, CalculationMethod.EGYPT, nowMillis = baseline.timeInMillis)
    val fajr = morningSchedule.todayPrayers.first { it.prayer == PrayerName.FAJR }
    val afterFajrSchedule = PrayerTimesManager.calculateSchedule(
      city = cairo,
      method = CalculationMethod.EGYPT,
      nowMillis = fajr.timestampMillis + 60_000L
    )
    assertNotEquals(PrayerName.SUNRISE, afterFajrSchedule.nextPrayer.prayer)
    assertEquals(PrayerName.DHUHR, afterFajrSchedule.nextPrayer.prayer)
    assertFalse(afterFajrSchedule.nextPrayer.isTomorrow)

    // 2. Late night after Isha (23:55) -> next prayer MUST be tomorrow's FAJR
    val lateNight = Calendar.getInstance(tz).apply {
      set(2025, Calendar.JUNE, 21, 23, 55, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val nightSchedule = PrayerTimesManager.calculateSchedule(
      city = cairo,
      method = CalculationMethod.EGYPT,
      nowMillis = lateNight.timeInMillis
    )
    assertEquals(PrayerName.FAJR, nightSchedule.nextPrayer.prayer)
    assertTrue(nightSchedule.nextPrayer.isTomorrow)
    assertTrue(nightSchedule.nextPrayer.timestampMillis > lateNight.timeInMillis)
  }

  @Test
  fun `qiblah bearing is accurate for known coordinates`() {
    // Cairo (~30.04, 31.23) -> Makkah (21.42, 39.83) is Southeast (~136 deg)
    val cairo = PrayerTimesManager.CITIES.first { it.id == "cairo" }
    val cairoBearing = QiblahCalculator.calculateQiblahBearing(cairo.latitude, cairo.longitude)
    assertTrue("Cairo Qiblah bearing was $cairoBearing", cairoBearing in 132.0..140.0)
    assertEquals("الجنوب الشرقي", QiblahCalculator.getDirectionLabelAr(cairoBearing))
    assertEquals("Southeast", QiblahCalculator.getDirectionLabelEn(cairoBearing))
  }

  @Test
  fun `hijri date and offline muadhin fallback are present`() {
    val hijriAr = PrayerTimesManager.getHijriDateString(isArabic = true)
    val hijriEn = PrayerTimesManager.getHijriDateString(isArabic = false)
    assertTrue(hijriAr.isNotBlank())
    assertTrue(hijriEn.isNotBlank())

    assertTrue(PrayerTimesManager.MUADHINS.any { it.isOfflineBuiltIn })
    assertTrue(AdhkarRepository.TASBIH_PHRASES.isNotEmpty())
    assertTrue(AdhkarRepository.ALL_ADHKAR.any { it.category == AdhkarCategory.MORNING })
    assertTrue(AdhkarRepository.ALL_ADHKAR.any { it.category == AdhkarCategory.EVENING })
    assertTrue(AdhkarRepository.ALL_ADHKAR.any { it.category == AdhkarCategory.AFTER_PRAYER })
  }
}
