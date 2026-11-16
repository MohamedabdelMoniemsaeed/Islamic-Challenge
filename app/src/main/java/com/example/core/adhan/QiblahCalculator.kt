package com.example.core.adhan

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object QiblahCalculator {
  private const val KAABA_LAT = 21.422487
  private const val KAABA_LON = 39.826206

  /**
   * Computes Qiblah bearing in degrees clockwise from True North [0, 360).
   */
  fun calculateQiblahBearing(latitude: Double, longitude: Double): Double {
    val lat1 = Math.toRadians(latitude)
    val lon1 = Math.toRadians(longitude)
    val lat2 = Math.toRadians(KAABA_LAT)
    val lon2 = Math.toRadians(KAABA_LON)

    val dLon = lon2 - lon1
    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
    var bearing = Math.toDegrees(atan2(y, x))
    if (bearing < 0) bearing += 360.0
    return bearing
  }

  fun getDirectionLabelAr(bearing: Double): String {
    return when {
      bearing >= 337.5 || bearing < 22.5 -> "الشمال"
      bearing < 67.5 -> "الشمال الشرقي"
      bearing < 112.5 -> "الشرق"
      bearing < 157.5 -> "الجنوب الشرقي"
      bearing < 202.5 -> "الجنوب"
      bearing < 247.5 -> "الجنوب الغربي"
      bearing < 292.5 -> "الغرب"
      else -> "الشمال الغربي"
    }
  }

  fun getDirectionLabelEn(bearing: Double): String {
    return when {
      bearing >= 337.5 || bearing < 22.5 -> "North"
      bearing < 67.5 -> "Northeast"
      bearing < 112.5 -> "East"
      bearing < 157.5 -> "Southeast"
      bearing < 202.5 -> "South"
      bearing < 247.5 -> "Southwest"
      bearing < 292.5 -> "West"
      else -> "Northwest"
    }
  }
}
