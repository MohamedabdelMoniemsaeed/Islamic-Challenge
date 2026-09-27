package com.example.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector

enum class GameMode(
  val id: String,
  val titleEn: String,
  val titleAr: String,
  val descEn: String,
  val descAr: String,
  val icon: ImageVector,
  val defaultQuestionCount: Int,
  val hasTimer: Boolean,
  val timerSecondsPerQuestion: Int,
  val allowsPenalties: Boolean
) {
  QUICK_CHALLENGE(
    id = "quick",
    titleEn = "Quick Challenge",
    titleAr = "التحدي السريع",
    descEn = "10 random questions across all subjects",
    descAr = "١٠ أسئلة منوعة من جميع الأبواب",
    icon = Icons.Filled.Bolt,
    defaultQuestionCount = 10,
    hasTimer = true,
    timerSecondsPerQuestion = 20,
    allowsPenalties = true
  ),
  DAILY_CHALLENGE(
    id = "daily",
    titleEn = "Daily Challenge",
    titleAr = "التحدي اليومي",
    descEn = "Curated daily test with streak rewards",
    descAr = "أسئلة اليوم المتجددة مع مكافأة الحماس",
    icon = Icons.Filled.Today,
    defaultQuestionCount = 5,
    hasTimer = true,
    timerSecondsPerQuestion = 25,
    allowsPenalties = true
  ),
  CATEGORY_CHALLENGE(
    id = "category",
    titleEn = "Category Challenge",
    titleAr = "تحدي الأقسام",
    descEn = "10 focused questions in your chosen field",
    descAr = "١٠ أسئلة متخصصة في القسم المختار",
    icon = Icons.Filled.Category,
    defaultQuestionCount = 10,
    hasTimer = true,
    timerSecondsPerQuestion = 20,
    allowsPenalties = true
  ),
  TIME_CHALLENGE(
    id = "time",
    titleEn = "Time Challenge",
    titleAr = "تحدي الوقت",
    descEn = "Answer as many as you can in 60 seconds",
    descAr = "أجب عن أكبر عدد من الأسئلة في ٦٠ ثانية",
    icon = Icons.Filled.HourglassBottom,
    defaultQuestionCount = 20,
    hasTimer = true,
    timerSecondsPerQuestion = 60,
    allowsPenalties = true
  ),
  SURVIVAL_MODE(
    id = "survival",
    titleEn = "Survival Mode",
    titleAr = "تحدي الثبات",
    descEn = "One mistake ends the run. How far can you go?",
    descAr = "خطأ واحد ينهي الجولة. اختبر قوة ثباتك",
    icon = Icons.Filled.Shield,
    defaultQuestionCount = 30,
    hasTimer = true,
    timerSecondsPerQuestion = 20,
    allowsPenalties = true
  ),
  LEVEL_MODE(
    id = "level",
    titleEn = "Level Mode",
    titleAr = "تحدي المستويات",
    descEn = "Tiered escalation from Easy to Expert",
    descAr = "صعوبة متدرجة من السهل إلى الخبير",
    icon = Icons.Filled.EmojiEvents,
    defaultQuestionCount = 12,
    hasTimer = true,
    timerSecondsPerQuestion = 20,
    allowsPenalties = true
  ),
  PRACTICE_MODE(
    id = "practice",
    titleEn = "Practice Mode",
    titleAr = "نمط التعلّم والتكرار",
    descEn = "Untimed, relaxed learning with instant insight",
    descAr = "تعلّم هادئ بدون وقت ولا خسارة نقاط",
    icon = Icons.AutoMirrored.Filled.MenuBook,
    defaultQuestionCount = 15,
    hasTimer = false,
    timerSecondsPerQuestion = 0,
    allowsPenalties = false
  )
}
