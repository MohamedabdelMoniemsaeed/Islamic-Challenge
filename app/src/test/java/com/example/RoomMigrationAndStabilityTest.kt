package com.example

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.CategoryProgressEntity
import com.example.data.local.MIGRATION_1_2
import com.example.data.local.MIGRATION_2_3
import com.example.data.local.MIGRATION_3_4
import com.example.data.local.PlayerDao
import com.example.data.local.PlayerEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.models.GameMode
import com.example.data.models.QuizCategory
import com.example.data.repository.PlayerRepository
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
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomMigrationAndStabilityTest {

  private lateinit var db: AppDatabase
  private lateinit var playerDao: PlayerDao
  private lateinit var playerRepository: PlayerRepository

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    playerDao = db.playerDao()
    playerRepository = PlayerRepository(playerDao)
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun testMigration1To2PreservesPlayerData() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dbName = "migration_test.db"
    context.deleteDatabase(dbName)

    // Step 1: Create Version 1 database and table schema (without lastDailyChallengeEpochDay)
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

    // Step 2: Insert Player Data in v1
    v1Db.execSQL(
      """
      INSERT INTO `player_profile` (
        `id`, `xp`, `coins`, `totalQuestions`, `correctAnswers`, `incorrectAnswers`,
        `gamesPlayed`, `currentStreak`, `longestStreak`, `lastActiveEpochDay`,
        `dailyRewardStreakDay`, `lastRewardClaimEpochDay`, `lifelines5050`,
        `lifelinesHint`, `lifelinesTime`, `lifelinesSkip`, `hasCompletedOnboarding`
      ) VALUES (
        1, 350, 180, 25, 22, 3, 4, 3, 5, 19500, 2, 19500, 4, 3, 2, 1, 1
      )
      """.trimIndent()
    )
    v1Db.close()

    // Step 3: Open database with AppDatabase (Version 4) and MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4
    val v3Db = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
      .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
      .allowMainThreadQueries()
      .build()

    val profile = runBlocking { v3Db.playerDao().getPlayerProfileSync() }
    assertNotNull("Player profile must be retrieved after migration", profile)
    assertEquals(350, profile!!.xp)
    assertEquals(180, profile.coins)
    assertEquals(25, profile.totalQuestions)
    assertEquals(22, profile.correctAnswers)
    assertEquals(3, profile.incorrectAnswers)
    assertEquals(4, profile.gamesPlayed)
    assertEquals(3, profile.currentStreak)
    assertEquals(5, profile.longestStreak)
    assertEquals(1, profile.id)
    assertTrue(profile.hasCompletedOnboarding)
    // Verify default value for new column
    assertEquals(0L, profile.lastDailyChallengeEpochDay)

    v3Db.close()
    context.deleteDatabase(dbName)
  }

  @Test
  fun testUserSettingsPersistenceAndRetrieval() = runBlocking {
    val initialSettings = playerRepository.getOrCreateSettings()
    assertNotNull(initialSettings)
    assertTrue(initialSettings.soundEnabled)
    assertFalse(initialSettings.musicEnabled)
    assertTrue(initialSettings.vibrationEnabled)
    assertEquals(0.8f, initialSettings.soundVolume, 0.01f)

    // Update settings
    val customSettings = UserSettingsEntity(
      id = 1,
      soundEnabled = false,
      musicEnabled = true,
      vibrationEnabled = false,
      soundVolume = 0.5f,
      themeMode = "DARK",
      languageCode = "ENGLISH"
    )
    playerRepository.updateUserSettings(customSettings)

    val fetched = playerDao.getUserSettingsSync()
    assertNotNull(fetched)
    assertFalse(fetched!!.soundEnabled)
    assertTrue(fetched.musicEnabled)
    assertFalse(fetched.vibrationEnabled)
    assertEquals(0.5f, fetched.soundVolume, 0.01f)
    assertEquals("DARK", fetched.themeMode)
    assertEquals("ENGLISH", fetched.languageCode)
    assertEquals(true, fetched.toDarkModeBoolean())
  }

  @Test
  fun testMigration2To3CreatesUserSettingsTable() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dbName = "migration_2_3_test.db"
    context.deleteDatabase(dbName)

    // Step 1: Create Version 2 database
    val v2Db = FrameworkSQLiteOpenHelperFactory().create(
      androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
        .name(dbName)
        .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(2) {
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
                `lastDailyChallengeEpochDay` INTEGER NOT NULL,
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
    v2Db.close()

    // Step 2: Open with AppDatabase v4 and MIGRATION_2_3, MIGRATION_3_4
    val v3Db = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
      .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
      .allowMainThreadQueries()
      .build()

    val initialSettings = runBlocking { v3Db.playerDao().getUserSettingsSync() }
    // Table exists, query succeeds without sqlite error
    assertEquals(null, initialSettings)

    // Verify insert into newly created table succeeds
    runBlocking {
      v3Db.playerDao().saveUserSettings(UserSettingsEntity(id = 1, soundEnabled = true))
      val saved = v3Db.playerDao().getUserSettingsSync()
      assertNotNull(saved)
      assertTrue(saved!!.soundEnabled)
      assertTrue(saved.notificationsEnabled)
      assertFalse(saved.showInAppBanner)
    }

    v3Db.close()
    context.deleteDatabase(dbName)
  }

  @Test
  fun testMigration3To4AddsNotificationColumns() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dbName = "migration_3_4_test.db"
    context.deleteDatabase(dbName)

    // Step 1: Create Version 3 database (without notificationsEnabled and showInAppBanner)
    val v3Db = FrameworkSQLiteOpenHelperFactory().create(
      androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
        .name(dbName)
        .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(3) {
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
                `lastDailyChallengeEpochDay` INTEGER NOT NULL,
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
            db.execSQL(
              """
              CREATE TABLE IF NOT EXISTS `user_settings` (
                `id` INTEGER NOT NULL,
                `soundEnabled` INTEGER NOT NULL DEFAULT 1,
                `musicEnabled` INTEGER NOT NULL DEFAULT 0,
                `vibrationEnabled` INTEGER NOT NULL DEFAULT 1,
                `soundVolume` REAL NOT NULL DEFAULT 0.8,
                `themeMode` TEXT NOT NULL DEFAULT 'SYSTEM',
                `languageCode` TEXT NOT NULL DEFAULT 'ARABIC',
                PRIMARY KEY(`id`)
              )
              """.trimIndent()
            )
          }

          override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
        })
        .build()
    ).writableDatabase

    // Insert v3 row
    v3Db.execSQL(
      """
      INSERT INTO `user_settings` (`id`, `soundEnabled`, `musicEnabled`, `vibrationEnabled`, `soundVolume`, `themeMode`, `languageCode`)
      VALUES (1, 1, 0, 1, 0.8, 'SYSTEM', 'ARABIC')
      """.trimIndent()
    )
    v3Db.close()

    // Step 2: Open with AppDatabase v4 and MIGRATION_3_4
    val v4Db = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
      .addMigrations(MIGRATION_3_4)
      .allowMainThreadQueries()
      .build()

    val settings = runBlocking { v4Db.playerDao().getUserSettingsSync() }
    assertNotNull(settings)
    assertTrue("Notifications must default to enabled in migration", settings!!.notificationsEnabled)
    assertFalse("In-app banner must default to false in migration", settings.showInAppBanner)
    assertTrue(settings.soundEnabled)

    v4Db.close()
    context.deleteDatabase(dbName)
  }

  @Test
  fun testDailyChallengeCompletionAndIdempotency() = runBlocking {
    val initialProfile = playerRepository.getOrCreateProfile()
    val todayEpoch = LocalDate.now().toEpochDay()

    // Initially daily challenge is not completed today
    assertFalse(playerRepository.isDailyChallengeCompletedToday(initialProfile))

    // 1st completion today
    val (afterFirst, _) = playerRepository.recordGameResults(
      correctCount = 10,
      wrongCount = 0,
      xpGained = 100,
      coinsGained = 50,
      category = QuizCategory.QURAN,
      mode = GameMode.DAILY_CHALLENGE
    )

    assertEquals(todayEpoch, afterFirst.lastDailyChallengeEpochDay)
    assertTrue(playerRepository.isDailyChallengeCompletedToday(afterFirst))
    val xpAfterFirst = afterFirst.xp
    val coinsAfterFirst = afterFirst.coins

    // 2nd completion on same day (idempotent: bonus daily XP/Coins must NOT be awarded again)
    val (afterSecond, _) = playerRepository.recordGameResults(
      correctCount = 10,
      wrongCount = 0,
      xpGained = 100,
      coinsGained = 50,
      category = QuizCategory.QURAN,
      mode = GameMode.DAILY_CHALLENGE
    )

    // Base XP gained is 100, but bonusDailyXp (+50) was NOT awarded a second time
    assertEquals(xpAfterFirst + 100, afterSecond.xp)
    // Base Coins gained is 50, but bonusDailyCoins (+25) was NOT awarded a second time
    assertEquals(coinsAfterFirst + 50, afterSecond.coins)
    assertEquals(todayEpoch, afterSecond.lastDailyChallengeEpochDay)
  }

  @Test
  fun testAtomicResetAndDataIntegrity() = runBlocking {
    // Record game result and save category progress
    playerRepository.recordGameResults(
      correctCount = 5,
      wrongCount = 5,
      xpGained = 50,
      coinsGained = 20,
      category = QuizCategory.SEERAH,
      mode = GameMode.QUICK_CHALLENGE
    )

    val catProgress = playerDao.getCategoryProgress(QuizCategory.SEERAH.id)
    assertNotNull(catProgress)
    assertEquals(10, catProgress!!.questionsAnswered)

    // Reset all progress atomically
    playerRepository.resetAllProgress()

    val profileAfterReset = playerDao.getPlayerProfileSync()
    assertNotNull(profileAfterReset)
    assertEquals(0, profileAfterReset!!.xp)
    assertEquals(100, profileAfterReset.coins) // Default starting coins

    val catAfterReset = playerDao.getCategoryProgress(QuizCategory.SEERAH.id)
    assertEquals(null, catAfterReset)
  }
}
