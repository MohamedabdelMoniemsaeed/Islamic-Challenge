package com.example.core.adhan

import android.icu.util.IslamicCalendar
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

enum class CalculationMethod(
  val id: String,
  val nameAr: String,
  val nameEn: String,
  val fajrAngle: Double,
  val ishaAngle: Double,
  val ishaMinutesAfterMaghrib: Int? = null
) {
  EGYPT(
    id = "egypt",
    nameAr = "الهيئة المصرية العامة للمساحة",
    nameEn = "Egyptian General Authority",
    fajrAngle = 19.5,
    ishaAngle = 17.5
  ),
  UMM_AL_QURA(
    id = "umm_al_qura",
    nameAr = "أم القرى - مكة المكرمة",
    nameEn = "Umm Al-Qura (Makkah)",
    fajrAngle = 18.5,
    ishaAngle = 0.0,
    ishaMinutesAfterMaghrib = 90
  ),
  MWL(
    id = "mwl",
    nameAr = "رابطة العالم الإسلامي",
    nameEn = "Muslim World League",
    fajrAngle = 18.0,
    ishaAngle = 17.0
  ),
  KARACHI(
    id = "karachi",
    nameAr = "جامعة العلوم الإسلامية بكراتشي",
    nameEn = "Univ. of Islamic Sciences, Karachi",
    fajrAngle = 18.0,
    ishaAngle = 18.0
  ),
  ISNA(
    id = "isna",
    nameAr = "الجمعية الإسلامية بأمريكا الشمالية",
    nameEn = "ISNA (North America)",
    fajrAngle = 15.0,
    ishaAngle = 15.0
  )
}

enum class AsrJuristicMethod(
  val id: String,
  val nameAr: String,
  val nameEn: String,
  val shadowFactor: Double
) {
  SHAFI("shafi", "الجمهور (الشافعي / المالكي / الحنبلي)", "Standard (Shafi'i, Maliki, Hanbali)", 1.0),
  HANAFI("hanafi", "المذهب الحنفي", "Hanafi", 2.0)
}

data class CityLocation(
  val id: String,
  val nameAr: String,
  val nameEn: String,
  val latitude: Double,
  val longitude: Double,
  val timeZoneId: String,
  val defaultMethod: CalculationMethod = CalculationMethod.MWL
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
  val timeStringAr: String,
  val timeStringEn: String,
  val timestampMillis: Long,
  val isNext: Boolean = false,
  val isCurrent: Boolean = false,
  val isTomorrow: Boolean = false
) {
  fun getTimeString(isArabic: Boolean): String = if (isArabic) timeStringAr else timeStringEn
}

data class MuadhinAudio(
  val id: String,
  val nameAr: String,
  val nameEn: String,
  val audioUrls: List<String>,
  val descriptionAr: String,
  val descriptionEn: String,
  val isOfflineBuiltIn: Boolean = false
) {
  fun getName(isArabic: Boolean): String = if (isArabic) nameAr else nameEn
  fun getDescription(isArabic: Boolean): String = if (isArabic) descriptionAr else descriptionEn
}

/**
 * High-accuracy astronomical prayer times calculation engine using full-quadrant atan2,
 * IANA TimeZone DST awareness, regional calculation methods, and Hijri calendar formatting.
 */
object PrayerTimesManager {

  val CITIES = listOf(
    CityLocation("cairo", "القاهرة", "Cairo", 30.0444, 31.2357, "Africa/Cairo", CalculationMethod.EGYPT),
    CityLocation("alexandria", "الإسكندرية", "Alexandria", 31.2001, 29.9187, "Africa/Cairo", CalculationMethod.EGYPT),
    CityLocation("makkah", "مكة المكرمة", "Makkah", 21.4225, 39.8262, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA),
    CityLocation("madinah", "المدينة المنورة", "Madinah", 24.4672, 39.6111, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA),
    CityLocation("riyadh", "الرياض", "Riyadh", 24.7136, 46.6753, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA),
    CityLocation("jerusalem", "القدس الشريف", "Jerusalem", 31.7683, 35.2137, "Asia/Jerusalem", CalculationMethod.MWL),
    CityLocation("amman", "عَمّان", "Amman", 31.9454, 35.9284, "Asia/Amman", CalculationMethod.MWL),
    CityLocation("dubai", "دبي", "Dubai", 25.2048, 55.2708, "Asia/Dubai", CalculationMethod.UMM_AL_QURA),
    CityLocation("kuwait", "الكويت", "Kuwait City", 29.3759, 47.9774, "Asia/Kuwait", CalculationMethod.UMM_AL_QURA),
    CityLocation("doha", "الدوحة", "Doha", 25.2854, 51.5310, "Asia/Qatar", CalculationMethod.UMM_AL_QURA),
    CityLocation("baghdad", "بغداد", "Baghdad", 33.3152, 44.3661, "Asia/Baghdad", CalculationMethod.MWL),
    CityLocation("rabat", "الرباط", "Rabat", 34.0209, -6.8416, "Africa/Casablanca", CalculationMethod.MWL),
    CityLocation("algiers", "الجزائر", "Algiers", 36.7538, 3.0588, "Africa/Algiers", CalculationMethod.MWL),
    CityLocation("tunis", "تونس", "Tunis", 36.8065, 10.1815, "Africa/Tunis", CalculationMethod.MWL),
    CityLocation("khartoum", "الخرطوم", "Khartoum", 15.5007, 32.5599, "Africa/Khartoum", CalculationMethod.EGYPT),
    CityLocation("istanbul", "إسطنبول", "Istanbul", 41.0082, 28.9784, "Europe/Istanbul", CalculationMethod.MWL)
  )

  val MUADHINS = listOf(
    MuadhinAudio(
      id = "makkah",
      nameAr = "أذان الحرم المكي الشريف",
      nameEn = "Makkah Haram Adhan",
      audioUrls = listOf(
        "https://www.islamcan.com/audio/adhan/azan1.mp3",
        "https://media.blubrry.com/muslim_central_quran/podcasts.quran.com/Adhan/Makkah.mp3"
      ),
      descriptionAr = "بصوت مؤذن المسجد الحرام بمكة المكرمة",
      descriptionEn = "Adhan from Al-Masjid Al-Haram, Makkah"
    ),
    MuadhinAudio(
      id = "madinah",
      nameAr = "أذان المسجد النبوي الشريف",
      nameEn = "Madinah Mosque Adhan",
      audioUrls = listOf(
        "https://www.islamcan.com/audio/adhan/azan2.mp3",
        "https://www.islamcan.com/audio/adhan/azan6.mp3"
      ),
      descriptionAr = "بصوت مؤذن المسجد النبوي بالمدينة المنورة",
      descriptionEn = "Adhan from Al-Masjid An-Nabawi, Madinah"
    ),
    MuadhinAudio(
      id = "alaqsa",
      nameAr = "أذان المسجد الأقصى المبارك",
      nameEn = "Al-Aqsa Mosque Adhan",
      audioUrls = listOf(
        "https://www.islamcan.com/audio/adhan/azan3.mp3",
        "https://www.islamcan.com/audio/adhan/azan7.mp3"
      ),
      descriptionAr = "الأذان من رحاب المسجد الأقصى بالقدس الشريف",
      descriptionEn = "Adhan from Al-Aqsa Mosque, Jerusalem"
    ),
    MuadhinAudio(
      id = "abdulbasit",
      nameAr = "أذان الشيخ عبد الباسط عبد الصمد",
      nameEn = "Sheikh Abdul Basit Adhan",
      audioUrls = listOf(
        "https://www.islamcan.com/audio/adhan/azan4.mp3",
        "https://www.islamcan.com/audio/adhan/azan16.mp3"
      ),
      descriptionAr = "تلاوة الأذان بصوت القارئ الشيخ عبد الباسط",
      descriptionEn = "Classic Adhan by Sheikh Abdul Basit"
    ),
    MuadhinAudio(
      id = "takbeerat",
      nameAr = "تكبيرات العيد المبارك",
      nameEn = "Eid Takbeerat",
      audioUrls = listOf(
        "https://www.islamcan.com/audio/adhan/azan20.mp3",
        "https://www.islamcan.com/audio/adhan/azan10.mp3"
      ),
      descriptionAr = "الله أكبر الله أكبر لا إله إلا الله",
      descriptionEn = "Allahu Akbar, Allahu Akbar, La ilaha illa Allah"
    ),
    MuadhinAudio(
      id = "offline_chime",
      nameAr = "نغمة تنبيه المسجد (بدون إنترنت)",
      nameEn = "Offline Mosque Chime",
      audioUrls = emptyList(),
      descriptionAr = "تنبيه صوتي هادئ يعمل دائماً بدون إنترنت",
      descriptionEn = "Gentle built-in acoustic chime — works 100% offline",
      isOfflineBuiltIn = true
    )
  )

  const val GPS_CITY_ID = "gps_current"

  fun inferCalculationMethodForCoordinates(latitude: Double, longitude: Double): CalculationMethod {
    return when {
      // Egypt
      latitude in 22.0..31.8 && longitude in 24.5..36.0 -> CalculationMethod.EGYPT
      // Arabian Peninsula & Gulf
      latitude in 12.0..32.5 && longitude in 34.5..60.0 -> CalculationMethod.UMM_AL_QURA
      // North America
      longitude < -50.0 && latitude > 15.0 -> CalculationMethod.ISNA
      // South Asia
      latitude in 5.0..38.0 && longitude in 60.5..98.0 -> CalculationMethod.KARACHI
      else -> CalculationMethod.MWL
    }
  }

  fun findNearestPresetCity(latitude: Double, longitude: Double): CityLocation {
    return CITIES.minByOrNull { city ->
      val dLat = city.latitude - latitude
      val dLon = city.longitude - longitude
      dLat * dLat + dLon * dLon
    } ?: CITIES.first()
  }

  fun createGpsCityLocation(
    latitude: Double,
    longitude: Double,
    localityAr: String? = null,
    localityEn: String? = null,
    timeZoneId: String = TimeZone.getDefault().id
  ): CityLocation {
    val nearest = findNearestPresetCity(latitude, longitude)
    val cleanAr = localityAr?.takeIf { it.isNotBlank() }
    val cleanEn = localityEn?.takeIf { it.isNotBlank() }
    val nameAr = if (cleanAr != null) "$cleanAr (GPS)" else "موقعي الحالي • قرب ${nearest.nameAr}"
    val nameEn = if (cleanEn != null) "$cleanEn (GPS)" else "My Location • Near ${nearest.nameEn}"

    return CityLocation(
      id = GPS_CITY_ID,
      nameAr = nameAr,
      nameEn = nameEn,
      latitude = latitude,
      longitude = longitude,
      timeZoneId = timeZoneId,
      defaultMethod = inferCalculationMethodForCoordinates(latitude, longitude)
    )
  }

  fun loadSavedCityLocation(prefs: android.content.SharedPreferences): CityLocation {
    val savedCityId = prefs.getString("selected_city_id", "cairo") ?: "cairo"
    if (savedCityId == GPS_CITY_ID && prefs.contains("gps_lat") && prefs.contains("gps_lon")) {
      val lat = java.lang.Double.longBitsToDouble(prefs.getLong("gps_lat", 0L))
      val lon = java.lang.Double.longBitsToDouble(prefs.getLong("gps_lon", 0L))
      val nameAr = prefs.getString("gps_city_ar", null)
      val nameEn = prefs.getString("gps_city_en", null)
      val tzId = prefs.getString("gps_tz_id", TimeZone.getDefault().id) ?: TimeZone.getDefault().id
      return CityLocation(
        id = GPS_CITY_ID,
        nameAr = nameAr ?: "موقعي الحالي (GPS)",
        nameEn = nameEn ?: "My Location (GPS)",
        latitude = lat,
        longitude = lon,
        timeZoneId = tzId,
        defaultMethod = inferCalculationMethodForCoordinates(lat, lon)
      )
    }
    return CITIES.find { it.id == savedCityId } ?: CITIES.first()
  }

  fun saveCitySelection(prefs: android.content.SharedPreferences, city: CityLocation) {
    val editor = prefs.edit()
      .putString("selected_city_id", city.id)
      .putString("calculation_method_id", city.defaultMethod.id)
    if (city.id == GPS_CITY_ID) {
      editor
        .putLong("gps_lat", java.lang.Double.doubleToRawLongBits(city.latitude))
        .putLong("gps_lon", java.lang.Double.doubleToRawLongBits(city.longitude))
        .putString("gps_city_ar", city.nameAr)
        .putString("gps_city_en", city.nameEn)
        .putString("gps_tz_id", city.timeZoneId)
    }
    editor.apply()
  }

  data class PrayerScheduleResult(
    val todayPrayers: List<PrayerTimeItem>,
    val nextPrayer: PrayerTimeItem
  )

  /**
   * Computes accurate prayer times for a given city, calculation method, Asr method, and instant.
   */
  fun calculateSchedule(
    city: CityLocation,
    method: CalculationMethod = city.defaultMethod,
    asrMethod: AsrJuristicMethod = AsrJuristicMethod.SHAFI,
    nowMillis: Long = System.currentTimeMillis()
  ): PrayerScheduleResult {
    val cityTz = TimeZone.getTimeZone(city.timeZoneId)
    val todayCal = Calendar.getInstance(cityTz).apply {
      timeInMillis = nowMillis
    }

    val todayRaw = computeDayPrayers(city, method, asrMethod, todayCal)

    // Determine the next obligatory prayer (excluding SUNRISE as a Salah, while keeping SUNRISE in schedule)
    val obligatoryIndices = listOf(0, 2, 3, 4, 5) // FAJR, DHUHR, ASR, MAGHRIB, ISHA
    var nextObligatoryIndex = -1
    for (idx in obligatoryIndices) {
      if (todayRaw[idx].timestampMillis > nowMillis) {
        nextObligatoryIndex = idx
        break
      }
    }

    val nextPrayer: PrayerTimeItem
    val currentObligatoryIndex: Int

    if (nextObligatoryIndex != -1) {
      val target = todayRaw[nextObligatoryIndex]
      nextPrayer = target.copy(isNext = true)
      val posInObligatory = obligatoryIndices.indexOf(nextObligatoryIndex)
      currentObligatoryIndex = if (posInObligatory > 0) obligatoryIndices[posInObligatory - 1] else 5
    } else {
      // All today's obligatory prayers passed -> compute tomorrow's Fajr
      val tomorrowCal = (todayCal.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, 1)
      }
      val tomorrowRaw = computeDayPrayers(city, method, asrMethod, tomorrowCal)
      nextPrayer = tomorrowRaw[0].copy(isNext = true, isTomorrow = true)
      currentObligatoryIndex = 5 // ISHA
    }

    val finalToday = todayRaw.mapIndexed { index, item ->
      item.copy(
        isNext = (index == nextObligatoryIndex),
        isCurrent = (index == currentObligatoryIndex)
      )
    }

    return PrayerScheduleResult(
      todayPrayers = finalToday,
      nextPrayer = nextPrayer
    )
  }

  fun calculatePrayerTimes(
    city: CityLocation,
    calendar: Calendar = Calendar.getInstance(),
    method: CalculationMethod = city.defaultMethod,
    asrMethod: AsrJuristicMethod = AsrJuristicMethod.SHAFI
  ): List<PrayerTimeItem> {
    return calculateSchedule(city, method, asrMethod, calendar.timeInMillis).todayPrayers
  }

  private fun computeDayPrayers(
    city: CityLocation,
    method: CalculationMethod,
    asrMethod: AsrJuristicMethod,
    cityCalendar: Calendar
  ): List<PrayerTimeItem> {
    val cityTz = TimeZone.getTimeZone(city.timeZoneId)
    val year = cityCalendar.get(Calendar.YEAR)
    val month = cityCalendar.get(Calendar.MONTH) + 1
    val day = cityCalendar.get(Calendar.DAY_OF_MONTH)

    // Compute exact timezone offset in hours (including DST for that date)
    val dayNoonCal = (cityCalendar.clone() as Calendar).apply {
      set(Calendar.HOUR_OF_DAY, 12)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val tzOffsetHours = cityTz.getOffset(dayNoonCal.timeInMillis) / 3_600_000.0

    val jDate = julianDate(year, month, day) - (city.longitude / (15.0 * 24.0))

    // Initial day-fraction estimates (PrayTimes iterative refinement)
    var fajrT = 5.0 / 24.0
    var sunriseT = 6.0 / 24.0
    var dhuhrT = 12.0 / 24.0
    var asrT = 13.0 / 24.0
    var maghribT = 18.0 / 24.0
    var ishaT = 18.0 / 24.0

    var fajrH = 0.0
    var sunriseH = 0.0
    var dhuhrH = 0.0
    var asrH = 0.0
    var maghribH = 0.0
    var ishaH = 0.0

    repeat(2) {
      dhuhrH = computeMidDay(jDate, dhuhrT)
      fajrH = dhuhrH - sunAngleTime(jDate, fajrT, method.fajrAngle, city.latitude)
      sunriseH = dhuhrH - sunAngleTime(jDate, sunriseT, 0.833, city.latitude)
      asrH = dhuhrH + computeAsrTime(jDate, asrT, asrMethod.shadowFactor, city.latitude)
      maghribH = dhuhrH + sunAngleTime(jDate, maghribT, 0.833, city.latitude)
      ishaH = if (method.ishaMinutesAfterMaghrib != null) {
        maghribH + (method.ishaMinutesAfterMaghrib / 60.0)
      } else {
        dhuhrH + sunAngleTime(jDate, ishaT, method.ishaAngle, city.latitude)
      }

      fajrT = fajrH / 24.0
      sunriseT = sunriseH / 24.0
      dhuhrT = dhuhrH / 24.0
      asrT = asrH / 24.0
      maghribT = maghribH / 24.0
      ishaT = ishaH / 24.0
    }

    val tzShift = tzOffsetHours - (city.longitude / 15.0)
    val rawList = listOf(
      PrayerName.FAJR to (fajrH + tzShift),
      PrayerName.SUNRISE to (sunriseH + tzShift),
      PrayerName.DHUHR to (dhuhrH + tzShift + 1.0 / 60.0), // +1 min safety margin for Zenith
      PrayerName.ASR to (asrH + tzShift),
      PrayerName.MAGHRIB to (maghribH + tzShift),
      PrayerName.ISHA to (ishaH + tzShift)
    )

    return rawList.map { (prayer, decimalHours) ->
      val (h, m) = formatHoursAndMinutes(decimalHours)
      val pCal = Calendar.getInstance(cityTz).apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, h)
        set(Calendar.MINUTE, m)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }
      PrayerTimeItem(
        prayer = prayer,
        timeStringAr = formatPrayerTime(h, m, isArabic = true),
        timeStringEn = formatPrayerTime(h, m, isArabic = false),
        timestampMillis = pCal.timeInMillis
      )
    }
  }

  private data class SunPosition(val declination: Double, val equationOfTime: Double)

  private fun sunPosition(jd: Double): SunPosition {
    val d = jd - 2451545.0
    val g = fixAngle(357.529 + 0.98560028 * d)
    val q = fixAngle(280.459 + 0.98564736 * d)
    val l = fixAngle(q + 1.915 * sin(dToR(g)) + 0.020 * sin(dToR(2 * g)))
    val e = 23.439 - 0.00000036 * d

    val raDeg = rToD(atan2(cos(dToR(e)) * sin(dToR(l)), cos(dToR(l))))
    val raHours = fixHour(raDeg / 15.0)
    val declination = rToD(asin(sin(dToR(e)) * sin(dToR(l))))

    var eqOfTime = (q / 15.0) - raHours
    if (eqOfTime > 12.0) eqOfTime -= 24.0
    if (eqOfTime < -12.0) eqOfTime += 24.0

    return SunPosition(declination, eqOfTime)
  }

  private fun computeMidDay(jDate: Double, t: Double): Double {
    val eqT = sunPosition(jDate + t).equationOfTime
    return fixHour(12.0 - eqT)
  }

  private fun sunAngleTime(jDate: Double, t: Double, angleBelowHorizon: Double, latitude: Double): Double {
    val decl = sunPosition(jDate + t).declination
    val cosHourAngle = (-sin(dToR(angleBelowHorizon)) - sin(dToR(latitude)) * sin(dToR(decl))) /
      (cos(dToR(latitude)) * cos(dToR(decl)))
    val clamped = cosHourAngle.coerceIn(-1.0, 1.0)
    return rToD(acos(clamped)) / 15.0
  }

  private fun computeAsrTime(jDate: Double, t: Double, shadowFactor: Double, latitude: Double): Double {
    val decl = sunPosition(jDate + t).declination
    val angle = -rToD(atan(1.0 / (shadowFactor + tan(dToR(abs(latitude - decl))))))
    return sunAngleTime(jDate, t, angle, latitude)
  }

  @android.annotation.SuppressLint("NewApi")
  fun getHijriDateString(isArabic: Boolean, timeZoneId: String = "Africa/Cairo", nowMillis: Long = System.currentTimeMillis()): String {
    val monthsAr = listOf(
      "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
      "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
      "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )
    val monthsEn = listOf(
      "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
      "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
      "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    fun format(day: Int, monthOneBased: Int, year: Int): String {
      val idx = (monthOneBased - 1).coerceIn(0, 11)
      return if (isArabic) {
        "$day ${monthsAr[idx]} $year هـ"
      } else {
        "$day ${monthsEn[idx]} $year AH"
      }
    }

    // 1. Standard Umm al-Qura HijrahDate (Android 8.0+ / JVM 8+)
    try {
      val zoneId = java.time.ZoneId.of(timeZoneId)
      val localDate = java.time.Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
      val hijrahDate = java.time.chrono.HijrahDate.from(localDate)
      val day = hijrahDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
      val month = hijrahDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
      val year = hijrahDate.get(java.time.temporal.ChronoField.YEAR)
      return format(day, month, year)
    } catch (_: Throwable) {}

    // 2. Android ICU IslamicCalendar fallback (API 24-25)
    try {
      val hijri = IslamicCalendar(android.icu.util.TimeZone.getTimeZone(timeZoneId), Locale.US)
      hijri.timeInMillis = nowMillis
      val day = hijri.get(IslamicCalendar.DAY_OF_MONTH)
      val monthOneBased = hijri.get(IslamicCalendar.MONTH) + 1
      val year = hijri.get(IslamicCalendar.YEAR)
      return format(day, monthOneBased, year)
    } catch (_: Throwable) {}

    // 3. Astronomical/tabular fallback
    val cal = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId)).apply { timeInMillis = nowMillis }
    val jd = julianDate(
      cal.get(Calendar.YEAR),
      cal.get(Calendar.MONTH) + 1,
      cal.get(Calendar.DAY_OF_MONTH)
    ) + 0.5
    val z = floor(jd)
    val l = z - 1948440.0 + 10632.0
    val n = floor((l - 1.0) / 10631.0)
    val l2 = l - 10631.0 * n + 354.0
    val j = (floor((10985.0 - l2) / 5316.0)) * (floor((50.0 * l2) / 17719.0)) +
      (floor(l2 / 5670.0)) * (floor((43.0 * l2) / 15238.0))
    val l3 = l2 - (floor((30.0 - j) / 15.0)) * (floor((17719.0 * j) / 50.0)) -
      (floor(j / 16.0)) * (floor((15238.0 * j) / 43.0)) + 29.0
    val m = floor((24.0 * l3) / 709.0).toInt()
    val d = (l3 - floor((709.0 * m) / 24.0)).toInt()
    val y = (30.0 * n + j - 30.0).toInt()
    return format(d, m, y)
  }

  fun getDuaAfterAdhan(): String {
    return "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ الْقَائِمَةِ، آتِ مُحَمَّداً الْوَسِيلَةَ وَالْفَضِيلَةَ، وَابْعَثْهُ مَقَاماً مَحْمُوداً الَّذِي وَعَدْتَهُ، [إِنَّكَ لَا تُخْلِفُ الْمِيعَادَ]."
  }

  fun getDuaVirtue(isArabic: Boolean = true): String {
    return if (isArabic) {
      "قال رسول الله ﷺ: «من قال حين يسمع النداء: اللهم رب هذه الدعوة التامة... حلت له شفاعتي يوم القيامة» [رواه البخاري]."
    } else {
      "The Prophet ﷺ said: 'Whoever says when hearing the Adhan: Allahumma Rabba hadhihi ad-da'wati at-tammah... my intercession will be permissible for him on the Day of Resurrection.' [Sahih Al-Bukhari]"
    }
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

  private fun fixAngle(angle: Double): Double {
    var a = angle - 360.0 * floor(angle / 360.0)
    if (a < 0) a += 360.0
    return a
  }

  private fun fixHour(hour: Double): Double {
    var h = hour - 24.0 * floor(hour / 24.0)
    if (h < 0) h += 24.0
    return h
  }

  private fun dToR(d: Double): Double = d * Math.PI / 180.0
  private fun rToD(r: Double): Double = r * 180.0 / Math.PI

  private fun formatHoursAndMinutes(decimalHours: Double): Pair<Int, Int> {
    val fixed = fixHour(decimalHours)
    val h = floor(fixed).toInt()
    val m = ((fixed - h) * 60.0).roundToInt()
    return if (m >= 60) Pair((h + 1) % 24, 0) else Pair(h, m)
  }

  private fun formatPrayerTime(hour24: Int, minute: Int, isArabic: Boolean): String {
    val isPm = hour24 >= 12
    val hour12 = when {
      hour24 == 0 -> 12
      hour24 > 12 -> hour24 - 12
      else -> hour24
    }
    val suffix = if (isArabic) {
      if (isPm) "م" else "ص"
    } else {
      if (isPm) "PM" else "AM"
    }
    return String.format(Locale.US, "%02d:%02d %s", hour12, minute, suffix)
  }
}
