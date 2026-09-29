package com.example

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.core.audio.AudioFeedbackManager
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizationManager
import com.example.data.local.AchievementEntity
import com.example.data.local.AppDatabase
import com.example.data.local.CategoryProgressEntity
import com.example.data.local.MIGRATION_1_2
import com.example.data.local.MIGRATION_2_3
import com.example.data.local.MIGRATION_3_4
import com.example.data.local.PlayerDao
import com.example.data.models.GameMode
import com.example.data.models.LifelineType
import com.example.data.models.QuizCategory
import com.example.data.repository.PlayerRepository
import com.example.data.repository.QuestionRepository
import com.example.ui.viewmodels.QuizViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReleaseReadinessIntegrationTest {

  private lateinit var context: Context
  private lateinit var db: AppDatabase
  private lateinit var playerDao: PlayerDao
  private lateinit var playerRepository: PlayerRepository
  private lateinit var questionRepository: QuestionRepository
  private lateinit var audioManager: AudioFeedbackManager

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    playerDao = db.playerDao()
    playerRepository = PlayerRepository(playerDao)
    questionRepository = QuestionRepository()
    audioManager = AudioFeedbackManager(context)
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun testFreshInstallAndFirstLaunchFlow() = runBlocking {
    // 1. Fresh install initial state
    val profile = playerRepository.getOrCreateProfile()
    assertNotNull(profile)
    assertEquals(1, profile.level)
    assertEquals(0, profile.xp)
    assertEquals(100, profile.coins)
    assertFalse("Onboarding should be uncompleted on fresh install", profile.hasCompletedOnboarding)
    assertEquals(3, profile.lifelines5050)
    assertEquals(3, profile.lifelinesHint)
    assertEquals(3, profile.lifelinesTime)
    assertEquals(2, profile.lifelinesSkip)

    // Initial User Settings
    val settings = playerRepository.getOrCreateSettings()
    assertNotNull(settings)
    assertTrue(settings.soundEnabled)
    assertFalse(settings.musicEnabled)
    assertTrue(settings.vibrationEnabled)
    assertEquals(0.8f, settings.soundVolume, 0.01f)
    assertEquals("SYSTEM", settings.themeMode)
    assertEquals("ARABIC", settings.languageCode)

    // 2. Complete onboarding
    playerRepository.completeOnboarding()
    val updatedProfile = playerRepository.getOrCreateProfile()
    assertTrue("Onboarding should be marked completed", updatedProfile.hasCompletedOnboarding)

    // 3. Play first game session
    val (postGameProfile, _) = playerRepository.recordGameResults(
      correctCount = 5,
      wrongCount = 0,
      xpGained = 150,
      coinsGained = 50,
      category = QuizCategory.PROPHETS,
      mode = GameMode.QUICK_CHALLENGE
    )

    assertEquals(150, postGameProfile.xp)
    assertEquals(150, postGameProfile.coins)
    assertEquals(5, postGameProfile.correctAnswers)
    assertEquals(1, postGameProfile.gamesPlayed)

    // Check category progress
    val catProgress = playerDao.getCategoryProgress(QuizCategory.PROPHETS.id)
    assertNotNull(catProgress)
    assertEquals(5, catProgress!!.questionsAnswered)
    assertEquals(5, catProgress.correctCount)

    // 4. Simulate app close and re-launch
    val reloadedProfile = playerRepository.getOrCreateProfile()
    assertEquals(150, reloadedProfile.xp)
    assertEquals(150, reloadedProfile.coins)
    assertTrue(reloadedProfile.hasCompletedOnboarding)
  }

  @Test
  fun testCompleteDatabaseUpgradeMigrationV1ToV3() {
    val dbName = "release_upgrade_test.db"
    context.deleteDatabase(dbName)

    // Step 1: Create v1 database schema directly
    val v1Db = FrameworkSQLiteOpenHelperFactory().create(
      androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
        .name(dbName)
        .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
          override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL(
              """
              CREATE TABLE IF NOT EXISTS `player_profile` (
                `id` INTEGER NOT NULL,
                `xp` INTEGER NOT NULL,
                `coins` INTEGER NOT NULL,
                `totalQuestions` INTEGER NOT NULL,
                `correctAnswers` INTEGER NOT NULL,
                `incorrectAnswers` INTEGER NOT NULL,
                `gamesPlayed` INTEGER NOT NULL,
                `currentStreak` INTEGER NOT NULL,
                `longestStreak` INTEGER NOT NULL,
                `lastActiveEpochDay` INTEGER NOT NULL,
                `dailyRewardStreakDay` INTEGER NOT NULL,
                `lastRewardClaimEpochDay` INTEGER NOT NULL,
                `lifelines5050` INTEGER NOT NULL,
                `lifelinesHint` INTEGER NOT NULL,
                `lifelinesTime` INTEGER NOT NULL,
                `lifelinesSkip` INTEGER NOT NULL,
                `hasCompletedOnboarding` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
              )
              """.trimIndent()
            )
            db.execSQL(
              """
              CREATE TABLE IF NOT EXISTS `category_progress` (
                `categoryId` TEXT NOT NULL,
                `questionsAnswered` INTEGER NOT NULL,
                `correctCount` INTEGER NOT NULL,
                `bestScore` INTEGER NOT NULL,
                `isCompleted` INTEGER NOT NULL,
                PRIMARY KEY(`categoryId`)
              )
              """.trimIndent()
            )
            db.execSQL(
              """
              CREATE TABLE IF NOT EXISTS `unlocked_achievements` (
                `achievementId` TEXT NOT NULL,
                `unlockedTimestamp` INTEGER NOT NULL,
                PRIMARY KEY(`achievementId`)
              )
              """.trimIndent()
            )
          }

          override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
        })
        .build()
    ).writableDatabase

    // Insert legacy v1 profile data
    v1Db.execSQL(
      """
      INSERT INTO `player_profile` VALUES (
        1, 600, 250, 40, 35, 5, 4, 3, 5, 19800, 2, 19800, 4, 3, 3, 2, 1
      )
      """.trimIndent()
    )
    v1Db.execSQL(
      """
      INSERT INTO `category_progress` VALUES ('QURAN', 15, 14, 140, 0)
      """.trimIndent()
    )
    v1Db.execSQL(
      """
      INSERT INTO `unlocked_achievements` VALUES ('ach_first_challenge', 1700000000)
      """.trimIndent()
    )
    v1Db.close()

    // Step 2: Open with AppDatabase v4 applying MIGRATION_1_2, MIGRATION_2_3, and MIGRATION_3_4
    val v3Db = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
      .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
      .allowMainThreadQueries()
      .build()

    val profile = runBlocking { v3Db.playerDao().getPlayerProfileSync() }
    assertNotNull("Player profile must be intact after v1->v3 migration", profile)
    assertEquals(600, profile!!.xp)
    assertEquals(250, profile.coins)
    assertEquals(35, profile.correctAnswers)
    assertEquals(0L, profile.lastDailyChallengeEpochDay) // Defaulted safely by v2 migration

    val catProgress = runBlocking { v3Db.playerDao().getCategoryProgress("QURAN") }
    assertNotNull("Category progress must be preserved", catProgress)
    assertEquals(14, catProgress!!.correctCount)

    val achievements = runBlocking { v3Db.playerDao().getUnlockedAchievements().first() }
    assertTrue("Achievement must remain unlocked", achievements.any { it.achievementId == "ach_first_challenge" })

    // User settings table created cleanly by v3 migration
    val repo = PlayerRepository(v3Db.playerDao())
    val activeSettings = runBlocking { repo.getOrCreateSettings() }
    assertNotNull(activeSettings)
    assertTrue(activeSettings.soundEnabled)

    v3Db.close()
    context.deleteDatabase(dbName)
  }

  @Test
  fun testResetProgressFlow() = runBlocking {
    // 1. Setup advanced user state
    playerRepository.completeOnboarding()
    playerRepository.recordGameResults(
      correctCount = 10,
      wrongCount = 0,
      xpGained = 400,
      coinsGained = 150,
      category = QuizCategory.SEERAH,
      mode = GameMode.QUICK_CHALLENGE
    )

    var profile = playerRepository.getOrCreateProfile()
    assertEquals(400, profile.xp)
    assertEquals(250, profile.coins)
    val achievementsBefore = playerRepository.unlockedAchievementsFlow.first()
    assertTrue(achievementsBefore.isNotEmpty())
    assertNotNull(playerDao.getCategoryProgress(QuizCategory.SEERAH.id))

    // 2. Perform Reset Progress
    playerRepository.resetAllProgress()

    // 3. Verify everything is reset cleanly
    profile = playerRepository.getOrCreateProfile()
    assertEquals(1, profile.level)
    assertEquals(0, profile.xp)
    assertEquals(100, profile.coins)
    assertEquals(0, profile.correctAnswers)
    assertEquals(0, profile.totalQuestions)
    assertEquals(0, profile.gamesPlayed)
    assertEquals(1, profile.currentStreak)

    // Verify achievements and category progress are completely cleared
    val achievementsAfter = playerRepository.unlockedAchievementsFlow.first()
    assertTrue("Achievements must be empty after reset", achievementsAfter.isEmpty())
    val catProgressAfter = playerDao.getAllCategoryProgress().first()
    assertTrue("Category progress must be empty after reset", catProgressAfter.isEmpty())
  }

  @Test
  fun testCompleteOfflineResilience() = runBlocking {
    // 1. All questions load completely offline
    val allQuestions = questionRepository.getAllQuestions()
    assertEquals(144, allQuestions.size)
    assertEquals(8, QuizCategory.entries.size)

    // 2. Quiz session runs with zero network interaction
    val quizVm = QuizViewModel(questionRepository, playerRepository, audioManager)
    quizVm.startQuiz(GameMode.QUICK_CHALLENGE, QuizCategory.WORSHIP)
    assertEquals(10, quizVm.questions.value.size)

    // Answer a question offline
    val firstQ = quizVm.currentQuestion
    assertNotNull(firstQ)
    quizVm.selectOption(firstQ!!.correctAnswerIndex, soundEnabled = true, vibrationEnabled = true)
    assertEquals(1, quizVm.correctAnswersCount.value)
    assertTrue(quizVm.score.value > 0)

    // 3. Audio & Haptics run cleanly without network
    audioManager.playCorrectFeedback(true, true)
    audioManager.playAchievementFeedback(true, true)

    // 4. Lifelines operate offline
    val lifelinesInitial = playerRepository.getOrCreateProfile().lifelines5050
    val used = playerRepository.useLifeline(LifelineType.FIFTY_FIFTY)
    assertTrue(used)
    assertEquals(lifelinesInitial - 1, playerRepository.getOrCreateProfile().lifelines5050)

    // 5. Localization works completely offline
    val ar = LocalizationManager.get(AppLanguage.ARABIC)
    val en = LocalizationManager.get(AppLanguage.ENGLISH)
    assertTrue(ar.appTitle.isNotBlank())
    assertTrue(en.appTitle.isNotBlank())
  }
}
