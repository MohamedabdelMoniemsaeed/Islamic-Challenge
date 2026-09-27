package com.example.data.models

data class PlayerProfile(
  val id: Int = 1,
  val xp: Int = 0,
  val coins: Int = 100, // starting gift
  val totalQuestions: Int = 0,
  val correctAnswers: Int = 0,
  val incorrectAnswers: Int = 0,
  val gamesPlayed: Int = 0,
  val currentStreak: Int = 1,
  val longestStreak: Int = 1,
  val lastActiveEpochDay: Long = 0L,
  val dailyRewardStreakDay: Int = 0,
  val lastRewardClaimEpochDay: Long = 0L,
  val lastDailyChallengeEpochDay: Long = 0L,
  val lifelines5050: Int = 3,
  val lifelinesHint: Int = 3,
  val lifelinesTime: Int = 3,
  val lifelinesSkip: Int = 2,
  val hasCompletedOnboarding: Boolean = false
) {
  val level: Int
    get() {
      return when {
        xp < 150 -> 1
        xp < 350 -> 2
        xp < 650 -> 3
        xp < 1100 -> 4
        xp < 1800 -> 5
        xp < 2700 -> 6
        xp < 3900 -> 7
        else -> 8 + (xp - 3900) / 1500
      }
    }

  val currentLevelTitleEn: String
    get() = when (level) {
      1 -> "Beginner"
      2 -> "Student"
      3 -> "Learner"
      4 -> "Knowledge Seeker"
      5 -> "Advanced Learner"
      6 -> "Dedicated Seeker"
      7 -> "Proficient Learner"
      else -> "Knowledge Enthusiast"
    }

  val currentLevelTitleAr: String
    get() = when (level) {
      1 -> "طالب مبتدئ"
      2 -> "مستفيد"
      3 -> "متعلم"
      4 -> "باحث عن المعرفة"
      5 -> "دارس متقدم"
      6 -> "مجتهد مثابر"
      7 -> "متقن"
      else -> "متميز في المعرفة"
    }

  fun getTitle(isArabic: Boolean): String = if (isArabic) currentLevelTitleAr else currentLevelTitleEn

  val xpForCurrentLevelFloor: Int
    get() = when (level) {
      1 -> 0
      2 -> 150
      3 -> 350
      4 -> 650
      5 -> 1100
      6 -> 1800
      7 -> 2700
      else -> 3900 + (level - 8) * 1500
    }

  val xpForNextLevelCeiling: Int
    get() = when (level) {
      1 -> 150
      2 -> 350
      3 -> 650
      4 -> 1100
      5 -> 1800
      6 -> 2700
      7 -> 3900
      else -> 3900 + (level - 7) * 1500
    }

  val levelProgressFraction: Float
    get() {
      val range = (xpForNextLevelCeiling - xpForCurrentLevelFloor).coerceAtLeast(1)
      val currentInTier = (xp - xpForCurrentLevelFloor).coerceAtLeast(0)
      return (currentInTier.toFloat() / range.toFloat()).coerceIn(0f, 1f)
    }

  val xpRemainingForNextLevel: Int
    get() = (xpForNextLevelCeiling - xp).coerceAtLeast(0)

  val accuracyPercentage: Int
    get() = if (totalQuestions > 0) ((correctAnswers.toFloat() / totalQuestions) * 100).toInt() else 0
}
