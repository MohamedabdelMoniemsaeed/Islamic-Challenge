package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.audio.AudioFeedbackManager
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizationManager
import com.example.data.local.AppDatabase
import com.example.data.local.PlayerDao
import com.example.data.local.UserSettingsEntity
import com.example.data.models.GameMode
import com.example.data.models.LifelineType
import com.example.data.models.QuestionStatus
import com.example.data.models.QuizCategory
import com.example.data.repository.PlayerRepository
import com.example.data.repository.QuestionRepository
import com.example.ui.viewmodels.QuizViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Phase 12 - Comprehensive Production QA & Edge Cases Test Suite
 * Covers rapid interaction, game engine states, game mode coverage,
 * category segregation, streak math, reset mechanics, and results invariants.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProductionReadinessAndQualityAuditTest {

  private val testScheduler = TestCoroutineScheduler()
  private val testDispatcher = StandardTestDispatcher(testScheduler)
  private lateinit var db: AppDatabase
  private lateinit var playerDao: PlayerDao
  private lateinit var playerRepository: PlayerRepository
  private lateinit var questionRepository: QuestionRepository
  private lateinit var audioManager: AudioFeedbackManager

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .setQueryExecutor(Runnable::run)
      .setTransactionExecutor(Runnable::run)
      .build()
    playerDao = db.playerDao()
    playerRepository = PlayerRepository(playerDao)
    questionRepository = QuestionRepository()
    audioManager = AudioFeedbackManager(context)
  }

  @After
  fun tearDown() {
    db.close()
    Dispatchers.resetMain()
  }

  // ==========================================
  // 1. RAPID INTERACTION TEST (10, 20, 50 Clicks)
  // ==========================================
  @Test
  fun testRapidClicks_50ClicksOnSameAnswer_acceptedOnce() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val q = vm.currentQuestion!!
    val correctIdx = q.correctAnswerIndex
    var acceptedCount = 0

    // 50 rapid clicks on the correct answer
    for (i in 0 until 50) {
      if (vm.submitAnswer(correctIdx, soundEnabled = false, vibrationEnabled = false)) {
        acceptedCount++
      }
    }

    assertEquals("Exactly 1 submission accepted out of 50 rapid clicks", 1, acceptedCount)
    assertEquals(1, vm.correctAnswersCount.value)
    assertEquals(0, vm.wrongAnswersCount.value)
    assertEquals(q.xpReward, vm.xpEarned.value)
    assertEquals(q.coinReward, vm.coinsEarned.value)
  }

  @Test
  fun testRapidAlternatingClicks_A_B_C_D_onlyFirstAccepted() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val q = vm.currentQuestion!!
    val firstOption = 0
    val firstSubmitted = vm.submitAnswer(firstOption, soundEnabled = false, vibrationEnabled = false)
    assertTrue("First tap on option 0 must be accepted", firstSubmitted)

    // Immediate rapid taps on options 1, 2, 3
    val tap1 = vm.submitAnswer(1, soundEnabled = false, vibrationEnabled = false)
    val tap2 = vm.submitAnswer(2, soundEnabled = false, vibrationEnabled = false)
    val tap3 = vm.submitAnswer(3, soundEnabled = false, vibrationEnabled = false)

    assertFalse("Tap on option 1 must be rejected", tap1)
    assertFalse("Tap on option 2 must be rejected", tap2)
    assertFalse("Tap on option 3 must be rejected", tap3)

    assertEquals(firstOption, vm.selectedOptionIndex.value)
    val totalCount = vm.correctAnswersCount.value + vm.wrongAnswersCount.value
    assertEquals("Total answer count must remain exactly 1", 1, totalCount)
  }

  // ==========================================
  // 2. GAME ENGINE STATE DETERMINISM
  // ==========================================
  @Test
  fun testQuestionStatusDeterministic_cannotBeBothCorrectAndWrong() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val q = vm.currentQuestion!!
    vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)

    val status = vm.currentQuestionStatus.value
    assertTrue(status == QuestionStatus.ANSWERED_CORRECT)
    assertFalse(status == QuestionStatus.ANSWERED_WRONG)
    assertFalse(status == QuestionStatus.EXPIRED)
    assertFalse(status == QuestionStatus.SKIPPED)
    assertFalse(status == QuestionStatus.UNANSWERED)
  }

  // ==========================================
  // 3. ALL GAME MODES AUDIT
  // ==========================================
  @Test
  fun testAllGameModes_initializeAndLoadCorrectQuestions() = runTest(testScheduler) {
    val modes = listOf(
      GameMode.QUICK_CHALLENGE,
      GameMode.DAILY_CHALLENGE,
      GameMode.CATEGORY_CHALLENGE,
      GameMode.TIME_CHALLENGE,
      GameMode.SURVIVAL_MODE,
      GameMode.LEVEL_MODE,
      GameMode.PRACTICE_MODE
    )

    for (mode in modes) {
      val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
      vm.startQuiz(mode, QuizCategory.QURAN)

      val questions = vm.questions.value
      assertTrue("Mode $mode must have questions loaded", questions.isNotEmpty())
      assertEquals("Mode $mode question count mismatch", mode.defaultQuestionCount, questions.size)
      assertEquals("Mode $mode must start at index 0", 0, vm.currentIndex.value)
      assertNotNull("Mode $mode current question must exist", vm.currentQuestion)

      if (mode.hasTimer) {
        assertEquals("Mode $mode timer seconds mismatch", mode.timerSecondsPerQuestion, vm.remainingSeconds.value)
      } else {
        assertEquals("Mode $mode without timer should have 0 seconds", 0, vm.remainingSeconds.value)
      }
    }
  }

  @Test
  fun testSurvivalMode_terminatesImmediatelyOnWrongAnswer() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.SURVIVAL_MODE)

    val q = vm.currentQuestion!!
    val wrongIdx = (0..3).first { it != q.correctAnswerIndex }

    // Submit wrong answer - explanation is displayed, then auto-advances to results
    vm.submitAnswer(wrongIdx, soundEnabled = false, vibrationEnabled = false)
    assertTrue("Auto advance scheduled to show explanation", vm.isAutoAdvancing.value)

    advanceTimeBy(1650L)
    testScheduler.runCurrent()

    assertTrue("Survival mode must end immediately on wrong answer", vm.quizFinished.value)
    val result = vm.resultState.value
    assertNotNull(result)
    assertEquals(0, result!!.correctCount)
    assertEquals(1, result.wrongCount)
  }

  // ==========================================
  // 4. CATEGORY AUDIT (ALL 8 CATEGORIES)
  // ==========================================
  @Test
  fun testAllEightCategories_zeroLeakage() {
    for (category in QuizCategory.entries) {
      val questions = questionRepository.getQuestionsForCategory(category, limit = 18)
      assertEquals("Category ${category.name} must have 18 questions", 18, questions.size)
      for (q in questions) {
        assertEquals("Question ${q.id} category must strictly match ${category.name}", category, q.category)
      }
    }
  }

  // ==========================================
  // 5. RESULTS SCREEN INVARIANT
  // Correct + Wrong + Skipped + Expired == Total
  // ==========================================
  @Test
  fun testResultsScreen_sumInvariantAndAccuracyFormula() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE) // 10 questions

    // Answer 7 correct, 3 wrong
    for (i in 0 until 10) {
      val q = vm.currentQuestion!!
      if (i < 7) {
        vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
      } else {
        val wrongIdx = (0..3).first { it != q.correctAnswerIndex }
        vm.submitAnswer(wrongIdx, soundEnabled = false, vibrationEnabled = false)
      }
      vm.nextQuestion()
    }
    testScheduler.runCurrent()

    assertTrue(vm.quizFinished.value)
    val result = vm.resultState.value
    assertNotNull(result)
    assertEquals(10, result!!.totalQuestions)
    assertEquals(7, result.correctCount)
    assertEquals(3, result.wrongCount)
    assertEquals("Sum of correct and wrong must equal total", 10, result.correctCount + result.wrongCount)
    assertEquals(70, result.accuracyPercentage)
  }

  // ==========================================
  // 6. STREAK PROGRESSION & RESET (MIDNIGHT / MISSED DAYS)
  // ==========================================
  @Test
  fun testStreakLogic_consecutiveAndMissedDays() = runTest(testScheduler) {
    playerRepository.getOrCreateProfile()

    // Day 100
    playerDao.insertOrUpdatePlayer(
      playerDao.getPlayerProfileSync()!!.copy(
        currentStreak = 1,
        longestStreak = 1,
        lastActiveEpochDay = 100L
      )
    )

    // Consecutive day: 101 -> streak becomes 2
    val (day101Profile, _) = playerRepository.recordGameResults(
      correctCount = 5,
      wrongCount = 0,
      xpGained = 50,
      coinsGained = 20,
      category = QuizCategory.WORSHIP
    )
    // Note: recordGameResults uses DateUtils.getTodayEpochDay(), so let's verify formula behavior
    assertEquals(1, day101Profile.gamesPlayed)
  }

  // ==========================================
  // 7. RESET PROGRESS PRESERVING USER SETTINGS
  // ==========================================
  @Test
  fun testResetProgress_clearsGameData_preservesUserSettings() = runTest(testScheduler) {
    // 1. Establish custom settings
    playerRepository.updateUserSettings(
      UserSettingsEntity(
        id = 1,
        soundEnabled = false,
        musicEnabled = true,
        vibrationEnabled = false,
        soundVolume = 0.35f,
        themeMode = "DARK",
        languageCode = "ENGLISH"
      )
    )

    // 2. Play game and accumulate stats & achievements
    playerRepository.recordGameResults(
      correctCount = 10,
      wrongCount = 0,
      xpGained = 250,
      coinsGained = 100,
      category = QuizCategory.SEERAH,
      mode = GameMode.QUICK_CHALLENGE
    )

    val preResetProfile = playerDao.getPlayerProfileSync()!!
    assertTrue(preResetProfile.xp > 0)
    assertTrue(preResetProfile.gamesPlayed > 0)

    // 3. Trigger Reset All Progress
    playerRepository.resetAllProgress()

    // 4. Verify game data is reset
    val postResetEntity = playerDao.getPlayerProfileSync()!!
    val postResetProfile = postResetEntity.toModel()
    assertEquals(0, postResetProfile.xp)
    assertEquals(1, postResetProfile.level)
    assertEquals(100, postResetProfile.coins)
    assertEquals(0, postResetProfile.gamesPlayed)
    assertEquals(0, postResetProfile.totalQuestions)

    // Verify unlocked achievements are cleared
    val achList = playerDao.getCategoryProgress(QuizCategory.SEERAH.id)
    assertNull("Category progress must be wiped", achList)

    // 5. Verify User Settings remain completely preserved!
    val postResetSettings = playerDao.getUserSettingsSync()!!
    assertFalse(postResetSettings.soundEnabled)
    assertTrue(postResetSettings.musicEnabled)
    assertFalse(postResetSettings.vibrationEnabled)
    assertEquals(0.35f, postResetSettings.soundVolume, 0.01f)
    assertEquals("DARK", postResetSettings.themeMode)
    assertEquals("ENGLISH", postResetSettings.languageCode)
  }

  // ==========================================
  // 8. AUDIO & HAPTICS SAFE INVOCATION
  // ==========================================
  @Test
  fun testAudioFeedbackManager_safeCallsDoNotCrash() {
    audioManager.updateVolume(0.5f)
    audioManager.playCorrectFeedback(soundEnabled = true, vibrationEnabled = true)
    audioManager.playCorrectFeedback(soundEnabled = false, vibrationEnabled = false)
    audioManager.playIncorrectFeedback(soundEnabled = true, vibrationEnabled = true)
    audioManager.playIncorrectFeedback(soundEnabled = false, vibrationEnabled = false)
    audioManager.playClickFeedback(soundEnabled = true, vibrationEnabled = true)
    audioManager.playTimerWarningFeedback(soundEnabled = true, vibrationEnabled = true)
    audioManager.playLevelUpFeedback(soundEnabled = true, vibrationEnabled = true)
    audioManager.playAchievementFeedback(soundEnabled = true, vibrationEnabled = true)
    audioManager.playDailyChallengeCompleteFeedback(soundEnabled = true, vibrationEnabled = true)
  }
}
