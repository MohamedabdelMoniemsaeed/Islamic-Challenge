package com.example

import com.example.data.models.AchievementsProvider
import com.example.data.models.GameMode
import com.example.data.models.PlayerProfile
import com.example.data.models.QuizCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HomeDashboardUnitTest {

  @Test
  fun `player profile calculates level and progress correctly`() {
    val profileLevel1 = PlayerProfile(xp = 100, totalQuestions = 10, correctAnswers = 8)
    assertEquals(1, profileLevel1.level)
    assertEquals(50, profileLevel1.xpRemainingForNextLevel)
    assertEquals(80, profileLevel1.accuracyPercentage)

    val profileLevel2 = PlayerProfile(xp = 250, totalQuestions = 20, correctAnswers = 18)
    assertEquals(2, profileLevel2.level)
    assertEquals(100, profileLevel2.xpRemainingForNextLevel)
    assertEquals(90, profileLevel2.accuracyPercentage)

    // Progress fraction is within 0.0 and 1.0
    assertTrue(profileLevel2.levelProgressFraction in 0f..1f)
  }

  @Test
  fun `daily challenge completion tracking is accurate for today`() {
    val today = LocalDate.now().toEpochDay()
    val yesterday = today - 1L

    val completedProfile = PlayerProfile(lastDailyChallengeEpochDay = today)
    val notCompletedProfile = PlayerProfile(lastDailyChallengeEpochDay = yesterday)
    val freshProfile = PlayerProfile(lastDailyChallengeEpochDay = 0L)

    assertEquals(today, completedProfile.lastDailyChallengeEpochDay)
    assertTrue(completedProfile.lastDailyChallengeEpochDay == today)
    assertFalse(notCompletedProfile.lastDailyChallengeEpochDay == today)
    assertFalse(freshProfile.lastDailyChallengeEpochDay == today)
  }

  @Test
  fun `categories include all 8 core Islamic categories`() {
    val categories = QuizCategory.values()
    assertEquals(8, categories.size)

    val categoryIds = categories.map { it.id }.toSet()
    assertTrue(categoryIds.contains("quran"))
    assertTrue(categoryIds.contains("seerah"))
    assertTrue(categoryIds.contains("prophets"))
    assertTrue(categoryIds.contains("worship"))
    assertTrue(categoryIds.contains("ramadan"))
    assertTrue(categoryIds.contains("adhkar"))
    assertTrue(categoryIds.contains("manners"))
    assertTrue(categoryIds.contains("general"))
  }

  @Test
  fun `game modes have valid configurations`() {
    val quick = GameMode.QUICK_CHALLENGE
    assertEquals(10, quick.defaultQuestionCount)
    assertTrue(quick.hasTimer)
    assertEquals(20, quick.timerSecondsPerQuestion)

    val daily = GameMode.DAILY_CHALLENGE
    assertNotNull(daily)
    assertTrue(daily.hasTimer)

    val practice = GameMode.PRACTICE_MODE
    assertFalse(practice.hasTimer)
    assertFalse(practice.allowsPenalties)
  }

  @Test
  fun `achievements provider contains valid awards`() {
    val achievements = AchievementsProvider.allAchievements
    assertTrue(achievements.isNotEmpty())
    assertTrue(achievements.any { it.id == "ach_first_challenge" })
    assertTrue(achievements.any { it.id == "ach_streak_3" })
    assertTrue(achievements.any { it.id == "ach_streak_7" })
  }
}
