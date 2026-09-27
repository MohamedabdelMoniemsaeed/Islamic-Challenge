package com.example.core.utils

import java.util.Calendar

object DateUtils {
  fun getTodayEpochDay(): Long {
    val cal = Calendar.getInstance()
    val timeMs = cal.timeInMillis
    val offsetMs = cal.timeZone.getOffset(timeMs)
    return (timeMs + offsetMs) / 86400000L
  }
}
