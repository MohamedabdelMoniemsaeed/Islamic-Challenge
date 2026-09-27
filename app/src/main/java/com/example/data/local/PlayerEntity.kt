package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.PlayerProfile

@Entity(tableName = "player_profile")
data class PlayerEntity(
  @PrimaryKey val id: Int = 1,
  val xp: Int = 0,
  val coins: Int = 100,
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
  fun toModel(): PlayerProfile = PlayerProfile(
    id = id,
    xp = xp,
    coins = coins,
    totalQuestions = totalQuestions,
    correctAnswers = correctAnswers,
    incorrectAnswers = incorrectAnswers,
    gamesPlayed = gamesPlayed,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastActiveEpochDay = lastActiveEpochDay,
    dailyRewardStreakDay = dailyRewardStreakDay,
    lastRewardClaimEpochDay = lastRewardClaimEpochDay,
    lastDailyChallengeEpochDay = lastDailyChallengeEpochDay,
    lifelines5050 = lifelines5050,
    lifelinesHint = lifelinesHint,
    lifelinesTime = lifelinesTime,
    lifelinesSkip = lifelinesSkip,
    hasCompletedOnboarding = hasCompletedOnboarding
  )

  companion object {
    fun fromModel(p: PlayerProfile): PlayerEntity = PlayerEntity(
      id = p.id,
      xp = p.xp,
      coins = p.coins,
      totalQuestions = p.totalQuestions,
      correctAnswers = p.correctAnswers,
      incorrectAnswers = p.incorrectAnswers,
      gamesPlayed = p.gamesPlayed,
      currentStreak = p.currentStreak,
      longestStreak = p.longestStreak,
      lastActiveEpochDay = p.lastActiveEpochDay,
      dailyRewardStreakDay = p.dailyRewardStreakDay,
      lastRewardClaimEpochDay = p.lastRewardClaimEpochDay,
      lastDailyChallengeEpochDay = p.lastDailyChallengeEpochDay,
      lifelines5050 = p.lifelines5050,
      lifelinesHint = p.lifelinesHint,
      lifelinesTime = p.lifelinesTime,
      lifelinesSkip = p.lifelinesSkip,
      hasCompletedOnboarding = p.hasCompletedOnboarding
    )
  }
}
