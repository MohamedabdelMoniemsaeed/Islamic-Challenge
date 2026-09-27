package com.example.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.ui.graphics.vector.ImageVector

enum class LifelineType(
  val id: String,
  val nameEn: String,
  val nameAr: String,
  val costCoins: Int,
  val icon: ImageVector
) {
  FIFTY_FIFTY(
    id = "50_50",
    nameEn = "50 / 50",
    nameAr = "٥٠ / ٥٠",
    costCoins = 50,
    icon = Icons.Filled.Percent
  ),
  HINT(
    id = "hint",
    nameEn = "Hint",
    nameAr = "تلميح",
    costCoins = 40,
    icon = Icons.AutoMirrored.Filled.HelpOutline
  ),
  EXTRA_TIME(
    id = "extra_time",
    nameEn = "+15s Time",
    nameAr = "+١٥ث وقت",
    costCoins = 30,
    icon = Icons.Filled.HourglassTop
  ),
  SKIP(
    id = "skip",
    nameEn = "Skip",
    nameAr = "تخطي",
    costCoins = 60,
    icon = Icons.Filled.SkipNext
  )
}
