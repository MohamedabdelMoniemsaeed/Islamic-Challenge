package com.example.data.repository

import com.example.data.local.AchievementEntity
import com.example.data.local.CategoryProgressEntity
import com.example.data.local.PlayerDao
import com.example.data.local.PlayerEntity
import com.example.core.utils.DateUtils
import com.example.data.models.Achievement
import com.example.data.models.DailyRewardDay
import com.example.data.models.LifelineType
import com.example.data.models.PlayerProfile
import com.example.data.models.QuizCategory
import com.example.data.models.defaultDailyRewards
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlayerRepository(private val playerDao: PlayerDao) {

  val playerProfileFlow: Flow<PlayerProfile> = playerDao.getPlayerProfile().map { entity ->
    entity?.toModel() ?: PlayerProfile()
  }

  val unlockedAchievementsFlow: Flow<Set<String>> = playerDao.getUnlockedAchievements().map { list ->
    list.map { it.achievementId }.toSet()
  }

  val categoryProgressFlow: Flow<Map<String, CategoryProgressEntity>> = playerDao.getAllCategoryProgress().map { list ->
    list.associateBy { it.categoryId }
  }

  suspend fun getOrCreateProfile(): PlayerProfile {
    val existing = playerDao.getPlayerProfileSync()
    if (existing == null) {
      val defaultProfile = PlayerProfile(lastActiveEpochDay = DateUtils.getTodayEpochDay())
      playerDao.insertOrUpdatePlayer(PlayerEntity.fromModel(defaultProfile))
      return defaultProfile
    }
    return existing.toModel()
  }

  suspend fun completeOnboarding() {
    val profile = getOrCreateProfile()
    val updated = profile.copy(hasCompletedOnboarding = true)
    playerDao.insertOrUpdatePlayer(PlayerEntity.fromModel(updated))
  }

  suspend fun recordGameResults(
    correctCount: Int,
    wrongCount: Int,
    xpGained: Int,
    coinsGained: Int,
    category: QuizCategory?,
    mode: com.example.data.models.GameMode = com.example.data.models.GameMode.QUICK_CHALLENGE
  ): Pair<PlayerProfile, Boolean> {
    val current = getOrCreateProfile()
    val oldLevel = current.level

    val todayEpoch = DateUtils.getTodayEpochDay()
    val newStreak: Int
    val newLongest: Int

    when {
      current.lastActiveEpochDay == todayEpoch -> {
        newStreak = current.currentStreak
        newLongest = current.longestStreak
      }
      current.lastActiveEpochDay == todayEpoch - 1L -> {
        newStreak = current.currentStreak + 1
        newLongest = maxOf(newStreak, current.longestStreak)
      }
      else -> {
        newStreak = 1
        newLongest = maxOf(1, current.longestStreak)
      }
    }

    // Daily Challenge completion bonus (idempotent: only once per day)
    val isDailyMode = mode == com.example.data.models.GameMode.DAILY_CHALLENGE
    val isFirstDailyToday = isDailyMode && current.lastDailyChallengeEpochDay != todayEpoch
    val bonusDailyXp = if (isFirstDailyToday) 50 else 0
    val bonusDailyCoins = if (isFirstDailyToday) 25 else 0
    val updatedDailyEpoch = if (isDailyMode) todayEpoch else current.lastDailyChallengeEpochDay

    val updatedProfile = current.copy(
      xp = current.xp + xpGained + bonusDailyXp,
      coins = current.coins + coinsGained + bonusDailyCoins,
      totalQuestions = current.totalQuestions + correctCount + wrongCount,
      correctAnswers = current.correctAnswers + correctCount,
      incorrectAnswers = current.incorrectAnswers + wrongCount,
      gamesPlayed = current.gamesPlayed + 1,
      currentStreak = newStreak,
      longestStreak = newLongest,
      lastActiveEpochDay = todayEpoch,
      lastDailyChallengeEpochDay = updatedDailyEpoch
    )

    // Determine achievements to unlock
    val achievementsToUnlock = mutableListOf<AchievementEntity>()
    if (updatedProfile.gamesPlayed >= 1) achievementsToUnlock.add(AchievementEntity("ach_first_challenge"))
    if (updatedProfile.currentStreak >= 3) achievementsToUnlock.add(AchievementEntity("ach_streak_3"))
    if (updatedProfile.currentStreak >= 7) achievementsToUnlock.add(AchievementEntity("ach_streak_7"))
    if (correctCount >= 10 && wrongCount == 0) achievementsToUnlock.add(AchievementEntity("ach_perfect_10"))
    if (updatedProfile.totalQuestions >= 50) achievementsToUnlock.add(AchievementEntity("ach_questions_50"))
    if (updatedProfile.totalQuestions >= 100) achievementsToUnlock.add(AchievementEntity("ach_knowledge_seeker"))

    // Determine category progress if applicable
    val updatedCategoryProgress: CategoryProgressEntity? = if (category != null) {
      val prev = playerDao.getCategoryProgress(category.id) ?: CategoryProgressEntity(category.id)
      val newScore = correctCount * 10
      prev.copy(
        questionsAnswered = prev.questionsAnswered + correctCount + wrongCount,
        correctCount = prev.correctCount + correctCount,
        bestScore = maxOf(prev.bestScore, newScore),
        isCompleted = prev.isCompleted || (prev.questionsAnswered + correctCount + wrongCount >= 10)
      )
    } else {
      null
    }

    // Atomic execution for profile, category progress, and achievements
    playerDao.recordGameResultAtomic(
      player = PlayerEntity.fromModel(updatedProfile),
      categoryProgress = updatedCategoryProgress,
      achievementsToUnlock = achievementsToUnlock
    )

    val didLevelUp = updatedProfile.level > oldLevel
    return Pair(updatedProfile, didLevelUp)
  }

  suspend fun useLifeline(type: LifelineType): Boolean {
    val current = getOrCreateProfile()
    val updated = when (type) {
      LifelineType.FIFTY_FIFTY -> if (current.lifelines5050 > 0) current.copy(lifelines5050 = current.lifelines5050 - 1) else null
      LifelineType.HINT -> if (current.lifelinesHint > 0) current.copy(lifelinesHint = current.lifelinesHint - 1) else null
      LifelineType.EXTRA_TIME -> if (current.lifelinesTime > 0) current.copy(lifelinesTime = current.lifelinesTime - 1) else null
      LifelineType.SKIP -> if (current.lifelinesSkip > 0) current.copy(lifelinesSkip = current.lifelinesSkip - 1) else null
    }

    return if (updated != null) {
      playerDao.insertOrUpdatePlayer(PlayerEntity.fromModel(updated))
      true
    } else {
      false
    }
  }

  suspend fun purchaseLifeline(type: LifelineType): Boolean {
    val current = getOrCreateProfile()
    if (current.coins < type.costCoins) return false

    val updated = when (type) {
      LifelineType.FIFTY_FIFTY -> current.copy(coins = current.coins - type.costCoins, lifelines5050 = current.lifelines5050 + 1)
      LifelineType.HINT -> current.copy(coins = current.coins - type.costCoins, lifelinesHint = current.lifelinesHint + 1)
      LifelineType.EXTRA_TIME -> current.copy(coins = current.coins - type.costCoins, lifelinesTime = current.lifelinesTime + 1)
      LifelineType.SKIP -> current.copy(coins = current.coins - type.costCoins, lifelinesSkip = current.lifelinesSkip + 1)
    }
    playerDao.insertOrUpdatePlayer(PlayerEntity.fromModel(updated))
    return true
  }

  suspend fun claimDailyReward(): DailyRewardDay? {
    val current = getOrCreateProfile()
    val todayEpoch = DateUtils.getTodayEpochDay()

    if (current.lastRewardClaimEpochDay == todayEpoch) {
      return null // Already claimed today
    }

    val nextDayIndex = (current.dailyRewardStreakDay % 7) + 1
    val reward = defaultDailyRewards.find { it.dayNumber == nextDayIndex } ?: defaultDailyRewards[0]

    val updated = current.copy(
      coins = current.coins + reward.coinAmount,
      xp = current.xp + reward.xpAmount,
      dailyRewardStreakDay = nextDayIndex,
      lastRewardClaimEpochDay = todayEpoch,
      lifelines5050 = if (reward.lifelineBonusType == LifelineType.FIFTY_FIFTY) current.lifelines5050 + 1 else current.lifelines5050,
      lifelinesHint = if (reward.lifelineBonusType == LifelineType.HINT) current.lifelinesHint + 1 else current.lifelinesHint,
      lifelinesTime = if (reward.lifelineBonusType == LifelineType.EXTRA_TIME) current.lifelinesTime + 1 else current.lifelinesTime
    )

    playerDao.insertOrUpdatePlayer(PlayerEntity.fromModel(updated))
    return reward
  }

  fun canClaimDailyReward(profile: PlayerProfile): Boolean {
    val todayEpoch = DateUtils.getTodayEpochDay()
    return profile.lastRewardClaimEpochDay != todayEpoch
  }

  fun isDailyChallengeCompletedToday(profile: PlayerProfile): Boolean {
    val todayEpoch = DateUtils.getTodayEpochDay()
    return profile.lastDailyChallengeEpochDay == todayEpoch
  }

  private suspend fun checkAchievements(profile: PlayerProfile, sessionCorrect: Int, sessionWrong: Int) {
    // First Challenge
    if (profile.gamesPlayed >= 1) {
      playerDao.unlockAchievement(AchievementEntity("ach_first_challenge"))
    }
    // 3-Day Streak
    if (profile.currentStreak >= 3) {
      playerDao.unlockAchievement(AchievementEntity("ach_streak_3"))
    }
    // 7-Day Streak
    if (profile.currentStreak >= 7) {
      playerDao.unlockAchievement(AchievementEntity("ach_streak_7"))
    }
    // Perfect Challenge
    if (sessionCorrect >= 10 && sessionWrong == 0) {
      playerDao.unlockAchievement(AchievementEntity("ach_perfect_10"))
    }
    // 50 Questions
    if (profile.totalQuestions >= 50) {
      playerDao.unlockAchievement(AchievementEntity("ach_questions_50"))
    }
    // Knowledge Seeker (100 questions)
    if (profile.totalQuestions >= 100) {
      playerDao.unlockAchievement(AchievementEntity("ach_knowledge_seeker"))
    }
  }

  suspend fun resetAllProgress() {
    val fresh = PlayerProfile()
    playerDao.resetAllData(PlayerEntity.fromModel(fresh))
  }

  val userSettingsFlow: Flow<com.example.data.local.UserSettingsEntity> = playerDao.getUserSettings().map { entity ->
    entity ?: com.example.data.local.UserSettingsEntity()
  }

  suspend fun getOrCreateSettings(): com.example.data.local.UserSettingsEntity {
    val existing = playerDao.getUserSettingsSync()
    if (existing == null) {
      val defaultSettings = com.example.data.local.UserSettingsEntity()
      playerDao.saveUserSettings(defaultSettings)
      return defaultSettings
    }
    return existing
  }

  suspend fun updateUserSettings(settings: com.example.data.local.UserSettingsEntity) {
    playerDao.saveUserSettings(settings)
  }
}
