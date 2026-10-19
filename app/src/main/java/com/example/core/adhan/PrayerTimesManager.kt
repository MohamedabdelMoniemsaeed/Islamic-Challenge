package com.example.core.adhan

import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

/**
 * Supported Islamic prayer calculation cities
 */
data class CityLocation(
  val id: String,
  val nameAr: String,
  val nameEn: String,
  val latitude: Double,
  val longitude: Double,
  val timezoneOffsetHours: Double
)

enum class PrayerName(val arName: String, val enName: String) {
  FAJR("الفجر", "Fajr"),
  SUNRISE("الشروق", "Sunrise"),
  DHUHR("الظهر", "Dhuhr"),
  ASR("العصر", "Asr"),
  MAGHRIB("المغرب", "Maghrib"),
  ISHA("العشاء", "Isha")
}

data class PrayerTimeItem(
  val prayer: PrayerName,
  val timeString: String,
  val timestampMillis: Long,
  val isNext: Boolean = false,
  val isCurrent: Boolean = false
)

data class MuadhinAudio(
  val id: String,
  val nameAr: String,
  val nameEn: String,
  val audioUrl: String,
  val description: String
)

/**
 * High-accuracy astronomical prayer times calculation engine
 */
object PrayerTimesManager {

  val CITIES = listOf(
    CityLocation("makkah", "مكة المكرمة", "Makkah", 21.4225, 39.8262, 3.0),
    CityLocation("madinah", "المدينة المنورة", "Madinah", 24.4672, 39.6111, 3.0),
    CityLocation("cairo", "القاهرة", "Cairo", 30.0444, 31.2357, 2.0),
    CityLocation("alexandria", "الإسكندرية", "Alexandria", 31.2001, 29.9187, 2.0),
    CityLocation("riyadh", "الرياض", "Riyadh", 24.7136, 46.6753, 3.0),
    CityLocation("jerusalem", "القدس الشريف", "Jerusalem", 31.7683, 35.2137, 2.0),
    CityLocation("amman", "عَمّان", "Amman", 31.9454, 35.9284, 3.0),
    CityLocation("dubai", "دبي", "Dubai", 25.2048, 55.2708, 4.0),
    CityLocation("kuwait", "الكويت", "Kuwait City", 29.3759, 47.9774, 3.0),
    CityLocation("doha", "الدوحة", "Doha", 25.2854, 51.5310, 3.0),
    CityLocation("baghdad", "بغداد", "Baghdad", 33.3152, 44.3661, 3.0),
    CityLocation("rabat", "الرباط", "Rabat", 34.0209, -6.8416, 1.0),
    CityLocation("algiers", "الجزائر", "Algiers", 36.7538, 3.0588, 1.0),
    CityLocation("tunis", "تونس", "Tunis", 36.8065, 10.1815, 1.0),
    CityLocation("khartoum", "الخرطوم", "Khartoum", 15.5007, 32.5599, 2.0),
    CityLocation("istanbul", "إسطنبول", "Istanbul", 41.0082, 28.9784, 3.0)
  )

  val MUADHINS = listOf(
    MuadhinAudio(
      id = "makkah",
      nameAr = "أذان الحرم المكي الشريف",
      nameEn = "Makkah Haram Adhan",
      audioUrl = "https://media.sd.ma/assabile/adhan_3432/001.mp3",
      description = "بأصوات مؤذني المسجد الحرام"
    ),
    MuadhinAudio(
      id = "madinah",
      nameAr = "أذان المسجد النبوي الشريف",
      nameEn = "Madinah Mosque Adhan",
      audioUrl = "https://media.sd.ma/assabile/adhan_3432/003.mp3",
      description = "بأصوات مؤذني المسجد النبوي بالمدينة المنورة"
    ),
    MuadhinAudio(
      id = "alaqsa",
      nameAr = "أذان المسجد الأقصى المبارك",
      nameEn = "Al-Aqsa Mosque Adhan",
      audioUrl = "https://media.sd.ma/assabile/adhan_3432/007.mp3",
      description = "الأذان من رحاب القدس الشريف"
    ),
    MuadhinAudio(
      id = "abdulbasit",
      nameAr = "أذان الشيخ عبد الباسط عبد الصمد",
      nameEn = "Sheikh Abdul Basit Adhan",
      audioUrl = "https://media.sd.ma/assabile/adhan_3432/004.mp3",
      description = "بصوت قيثارة السماء الشيخ عبد الباسط"
    ),
    MuadhinAudio(
      id = "takbeerat",
      nameAr = "تكبيرات العيد المبارك",
      nameEn = "Eid Takbeerat",
      audioUrl = "https://media.sd.ma/assabile/adhan_3432/011.mp3",
      description = "الله أكبر الله أكبر لا إله إلا الله"
    )
  )

  /**
   * Computes accurate prayer times for a given city and calendar date
   */
  fun calculatePrayerTimes(city: CityLocation, calendar: Calendar = Calendar.getInstance()): List<PrayerTimeItem> {
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    // Julian date calculation
    val jd = julianDate(year, month, day) - (city.longitude / (15.0 * 24.0))

    // Sun coordinates
    val d = jd - 2451545.0
    val g = fixAngle(357.529 + 0.98560028 * d)
    val q = fixAngle(280.459 + 0.98564736 * d)
    val l = fixAngle(q + 1.915 * sin(dToR(g)) + 0.020 * sin(dToR(2 * g)))
    val e = 23.439 - 0.00000036 * d
    val ra = fixAngle(rToD(atan(cos(dToR(e)) * tan(dToR(l))))) / 15.0
    val declination = rToD(asin(sin(dToR(e)) * sin(dToR(l))))
    val eqOfTime = (q / 15.0) - ra

    // Noon / Dhuhr base calculation
    val baseDhuhr = 12.0 + city.timezoneOffsetHours - (city.longitude / 15.0) - eqOfTime

    // Sun altitude angles
    val fajrAngle = 18.0 // Standard angle
    val ishaAngle = 17.5 // Standard angle

    val fajrHours = baseDhuhr - sunHourAngle(-fajrAngle, city.latitude, declination)
    val sunriseHours = baseDhuhr - sunHourAngle(-0.833, city.latitude, declination)
    val asrRatio = 1.0 // Shafi'i / Standard shadow ratio
    val asrAngle = rToD(atan(1.0 / (asrRatio + tan(dToR(abs(city.latitude - declination))))))
    val asrHours = baseDhuhr + sunHourAngle(asrAngle, city.latitude, declination)
    val maghribHours = baseDhuhr + sunHourAngle(-0.833, city.latitude, declination)
    val ishaHours = baseDhuhr + sunHourAngle(-ishaAngle, city.latitude, declination)

    val times = listOf(
      PrayerName.FAJR to fajrHours,
      PrayerName.SUNRISE to sunriseHours,
      PrayerName.DHUHR to baseDhuhr,
      PrayerName.ASR to asrHours,
      PrayerName.MAGHRIB to maghribHours,
      PrayerName.ISHA to ishaHours
    )

    val currentMillis = System.currentTimeMillis()
    var nextFound = false

    val result = mutableListOf<PrayerTimeItem>()
    val timestamps = mutableListOf<Long>()

    for ((prayer, hours) in times) {
      val (h, m) = formatHoursAndMinutes(hours)
      val pCal = Calendar.getInstance().apply {
        time = calendar.time
        set(Calendar.HOUR_OF_DAY, h)
        set(Calendar.MINUTE, m)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }
      timestamps.add(pCal.timeInMillis)
    }

    // Determine upcoming and active prayers
    var nextPrayerIndex = -1
    for (i in timestamps.indices) {
      if (timestamps[i] > currentMillis) {
        nextPrayerIndex = i
        break
      }
    }
    // If all prayers passed today, Fajr tomorrow is next
    if (nextPrayerIndex == -1) {
      nextPrayerIndex = 0
    }

    val currentPrayerIndex = if (nextPrayerIndex > 0) nextPrayerIndex - 1 else times.size - 1

    for (i in times.indices) {
      val (prayer, hours) = times[i]
      val (h, m) = formatHoursAndMinutes(hours)
      val timeFormatted = formatPrayerTime(h, m)
      result.add(
        PrayerTimeItem(
          prayer = prayer,
          timeString = timeFormatted,
          timestampMillis = timestamps[i],
          isNext = (i == nextPrayerIndex),
          isCurrent = (i == currentPrayerIndex)
        )
      )
    }

    return result
  }

  fun getDuaAfterAdhan(): String {
    return "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ الْقَائِمَةِ، آتِ مُحَمَّداً الْوَسِيلَةَ وَالْفَضِيلَةَ، وَابْعَثْهُ مَقَاماً مَحْمُوداً الَّذِي وَعَدْتَهُ، [إِنَّكَ لَا تُخْلِفُ الْمِيعَادَ]."
  }

  fun getDuaVirtue(): String {
    return "قال رسول الله ﷺ: «من قال حين يسمع النداء: اللهم رب هذه الدعوة التامة... حلت له شفاعتي يوم القيامة» [رواه البخاري]."
  }

  private fun julianDate(year: Int, month: Int, day: Int): Double {
    var y = year
    var m = month
    if (m <= 2) {
      y -= 1
      m += 12
    }
    val a = floor(y / 100.0)
    val b = 2 - a + floor(a / 4.0)
    return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
  }

  private fun sunHourAngle(angle: Double, latitude: Double, declination: Double): Double {
    val cosHourAngle = (sin(dToR(angle)) - sin(dToR(latitude)) * sin(dToR(declination))) /
      (cos(dToR(latitude)) * cos(dToR(declination)))
    val clamped = cosHourAngle.coerceIn(-1.0, 1.0)
    return rToD(acos(clamped)) / 15.0
  }

  private fun fixAngle(angle: Double): Double {
    var a = angle - 360.0 * floor(angle / 360.0)
    if (a < 0) a += 360.0
    return a
  }

  private fun dToR(d: Double): Double = d * Math.PI / 180.0
  private fun rToD(r: Double): Double = r * 180.0 / Math.PI

  private fun formatHoursAndMinutes(decimalHours: Double): Pair<Int, Int> {
    var fixed = decimalHours
    while (fixed < 0) fixed += 24.0
    while (fixed >= 24) fixed -= 24.0
    val h = floor(fixed).toInt()
    val m = ((fixed - h) * 60.0).roundToInt()
    return if (m >= 60) Pair((h + 1) % 24, 0) else Pair(h, m)
  }

  private fun formatPrayerTime(hour24: Int, minute: Int): String {
    val isPm = hour24 >= 12
    val hour12 = when {
      hour24 == 0 -> 12
      hour24 > 12 -> hour24 - 12
      else -> hour24
    }
    val suffix = if (isPm) "م" else "ص"
    return String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, suffix)
  }
}
