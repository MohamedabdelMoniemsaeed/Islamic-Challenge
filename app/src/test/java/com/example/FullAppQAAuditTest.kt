package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.audio.AudioFeedbackManager
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizationManager
import com.example.data.local.AppDatabase
import com.example.data.local.PlayerDao
import com.example.data.models.GameMode
import com.example.data.models.LifelineType
import com.example.data.models.PlayerProfile
import com.example.data.models.QuizCategory
import com.example.data.repository.PlayerRepository
import com.example.data.repository.QuestionRepository
import com.example.ui.viewmodels.QuizViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FullAppQAAuditTest {

  private val testDispatcher = UnconfinedTestDispatcher()
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

  @Test
  fun testLevelSystemCalculations() {
    // Level 1: 0..149 XP
    val p1 = PlayerProfile(xp = 0)
    assertEquals(1, p1.level)
    assertEquals("Beginner", p1.currentLevelTitleEn)
    assertEquals("طالب مبتدئ", p1.currentLevelTitleAr)
    assertEquals(0, p1.xpForCurrentLevelFloor)
    assertEquals(150, p1.xpForNextLevelCeiling)
    assertEquals(0f, p1.levelProgressFraction, 0.001f)
    assertEquals(150, p1.xpRemainingForNextLevel)

    // Level 2: 150..349 XP
    val p2 = PlayerProfile(xp = 200)
    assertEquals(2, p2.level)
    assertEquals("Student", p2.currentLevelTitleEn)
    assertEquals("مستفيد", p2.currentLevelTitleAr)
    assertEquals(150, p2.xpForCurrentLevelFloor)
    assertEquals(350, p2.xpForNextLevelCeiling)
    assertEquals(0.25f, p2.levelProgressFraction, 0.001f)
    assertEquals(150, p2.xpRemainingForNextLevel)

    // Level 3: 350..649 XP
    val p3 = PlayerProfile(xp = 500)
    assertEquals(3, p3.level)
    assertEquals("Learner", p3.currentLevelTitleEn)
    assertEquals(350, p3.xpForCurrentLevelFloor)
    assertEquals(650, p3.xpForNextLevelCeiling)

    // Level 5: 1800 ceiling, 1100 floor
    val p5 = PlayerProfile(xp = 1500)
    assertEquals(5, p5.level)
    assertEquals("Advanced Learner", p5.currentLevelTitleEn)
    assertEquals(1100, p5.xpForCurrentLevelFloor)
    assertEquals(1800, p5.xpForNextLevelCeiling)

    // Level 10: xp >= 3900 + (10 - 8) * 1500 = 6900
    val p10 = PlayerProfile(xp = 7000)
    assertEquals(10, p10.level)
    assertTrue(p10.xpForCurrentLevelFloor <= 7000)
    assertTrue(p10.xpForNextLevelCeiling > 7000)
  }

  @Test
  fun testQuestionRepositoryIntegrity() {
    val allQuestions = questionRepository.getAllQuestions()
    assertTrue("Repository should have questions loaded", allQuestions.isNotEmpty())

    // Validate that every question has valid text, 4 options, and correct index within 0..3
    for (q in allQuestions) {
      assertTrue("Question text AR must not be empty", q.questionAr.isNotBlank())
      assertTrue("Question text EN must not be empty", q.questionEn.isNotBlank())
      assertEquals("Question must have exactly 4 options in AR", 4, q.optionsAr.size)
      assertEquals("Question must have exactly 4 options in EN", 4, q.optionsEn.size)
      assertTrue("Correct answer index must be 0..3", q.correctAnswerIndex in 0..3)
      assertTrue("Explanation AR must not be empty", q.explanationAr.isNotBlank())
      assertTrue("Explanation EN must not be empty", q.explanationEn.isNotBlank())
    }

    // Verify all categories have questions available
    for (cat in QuizCategory.entries) {
      val catQuestions = questionRepository.getQuestionsForCategory(cat)
      assertTrue("Category ${cat.id} must have questions", catQuestions.isNotEmpty())
    }
  }

  @Test
  fun testLifelinesConsumptionAndPurchase() = runBlocking {
    playerRepository.getOrCreateProfile()

    // Default lifelines: 50_50=3, Hint=3, ExtraTime=3, Skip=2
    val used5050 = playerRepository.useLifeline(LifelineType.FIFTY_FIFTY)
    assertTrue(used5050)
    var profile = playerRepository.getOrCreateProfile()
    assertEquals(2, profile.lifelines5050)

    // Consume all skips
    assertTrue(playerRepository.useLifeline(LifelineType.SKIP))
    assertTrue(playerRepository.useLifeline(LifelineType.SKIP))
    assertFalse(playerRepository.useLifeline(LifelineType.SKIP)) // No more skips left

    profile = playerRepository.getOrCreateProfile()
    assertEquals(0, profile.lifelinesSkip)

    // Purchase skip with coins (cost = 60, default profile coins = 100)
    val purchased = playerRepository.purchaseLifeline(LifelineType.SKIP)
    assertTrue(purchased)
    profile = playerRepository.getOrCreateProfile()
    assertEquals(1, profile.lifelinesSkip)
    assertEquals(40, profile.coins) // 100 - 60 = 40
  }

  @Test
  fun testDailyRewardStreakProgressionAndIdempotency() = runBlocking {
    playerRepository.getOrCreateProfile()
    val today = LocalDate.now().toEpochDay()

    // First claim
    val firstClaim = playerRepository.claimDailyReward()
    assertNotNull(firstClaim)
    assertEquals(1, firstClaim!!.dayNumber)
    val updatedProfile = playerRepository.getOrCreateProfile()
    assertEquals(today, updatedProfile.lastRewardClaimEpochDay)

    // Duplicate claim on same day -> must return null
    val secondClaim = playerRepository.claimDailyReward()
    assertNull("Duplicate claim on same day must be rejected", secondClaim)
  }

  @Test
  fun testQuizViewModelFlowAndIdempotency() = runBlocking {
    playerRepository.getOrCreateProfile()
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val currentQ = vm.currentQuestion
    assertNotNull("Current question must be available", currentQ)

    // Select correct option
    val correctIdx = currentQ!!.correctAnswerIndex
    vm.selectOption(correctIdx, soundEnabled = false, vibrationEnabled = false)

    assertTrue(vm.isAnswerSubmitted.value)
    assertEquals(1, vm.correctAnswersCount.value)
    assertEquals(0, vm.wrongAnswersCount.value)
    assertTrue(vm.score.value > 0)

    // Duplicate selection on the same question must be ignored
    val scoreBefore = vm.score.value
    vm.selectOption(correctIdx, soundEnabled = false, vibrationEnabled = false)
    assertEquals(scoreBefore, vm.score.value)

    // Test Pause and Resume
    vm.pauseTimer()
    vm.resumeTimer()

    // Advance to end of quiz
    for (i in 1 until 10) {
      vm.nextQuestion()
      vm.selectOption(vm.currentQuestion!!.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    }

    // Trigger quiz finish
    vm.nextQuestion()

    var attempts = 0
    while (vm.resultState.value == null && attempts < 30) {
      kotlinx.coroutines.delay(50L)
      testDispatcher.scheduler.advanceUntilIdle()
      attempts++
    }

    assertTrue("Quiz must be marked finished", vm.quizFinished.value)
    val result = vm.resultState.value
    assertNotNull("Result state must be generated", result)
    assertEquals(10, result!!.totalQuestions)
    assertEquals(10, result.correctCount)
  }

  @Test
  fun testLocalizationStringsConsistency() {
    val arStrings = LocalizationManager.get(AppLanguage.ARABIC)
    val enStrings = LocalizationManager.get(AppLanguage.ENGLISH)

    // Verify key UI text is defined and non-empty in both languages
    assertTrue(arStrings.appTitle.isNotBlank())
    assertTrue(enStrings.appTitle.isNotBlank())
    assertTrue(arStrings.dailyChallenge.isNotBlank())
    assertTrue(enStrings.dailyChallenge.isNotBlank())
    assertTrue(arStrings.quickChallenge.isNotBlank())
    assertTrue(enStrings.quickChallenge.isNotBlank())
    assertTrue(arStrings.streakDays.isNotBlank())
    assertTrue(enStrings.streakDays.isNotBlank())
    assertTrue(arStrings.coins.isNotBlank())
    assertTrue(enStrings.coins.isNotBlank())
    assertTrue(arStrings.soundVolume.isNotBlank())
    assertTrue(enStrings.soundVolume.isNotBlank())
    assertTrue(arStrings.backgroundMusic.isNotBlank())
    assertTrue(enStrings.backgroundMusic.isNotBlank())
    assertTrue(arStrings.pauseTitle.isNotBlank())
    assertTrue(enStrings.pauseTitle.isNotBlank())
    assertTrue(arStrings.resumeGame.isNotBlank())
    assertTrue(enStrings.resumeGame.isNotBlank())
    assertTrue(arStrings.restartRound.isNotBlank())
    assertTrue(enStrings.restartRound.isNotBlank())
    assertTrue(arStrings.exitQuiz.isNotBlank())
    assertTrue(enStrings.exitQuiz.isNotBlank())
    assertTrue(arStrings.timeUpAlert.isNotBlank())
    assertTrue(enStrings.timeUpAlert.isNotBlank())
    assertTrue(arStrings.credits.isNotBlank())
    assertTrue(enStrings.credits.isNotBlank())
  }

  @Test
  fun testQuizPauseAndRestartRound() = runBlocking {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    assertFalse(vm.isPaused.value)
    vm.pauseQuiz()
    assertTrue(vm.isPaused.value)

    vm.resumeQuiz()
    assertFalse(vm.isPaused.value)

    vm.restartRound()
    assertFalse(vm.isPaused.value)
    assertEquals(0, vm.currentIndex.value)
    assertFalse(vm.isAnswerSubmitted.value)
    assertEquals(0, vm.correctAnswersCount.value)
  }

  @Test
  fun testAudioFeedbackManagerSafeExecution() {
    audioManager.updateVolume(0.5f)
    audioManager.playCorrectFeedback(soundEnabled = true, vibrationEnabled = true)
    audioManager.playIncorrectFeedback(soundEnabled = true, vibrationEnabled = true)
    audioManager.playClickFeedback(soundEnabled = true, vibrationEnabled = true)
    audioManager.playLevelUpFeedback(soundEnabled = true, vibrationEnabled = true)
    audioManager.playAchievementFeedback(soundEnabled = true, vibrationEnabled = true)
    audioManager.playTimerWarningFeedback(soundEnabled = true, vibrationEnabled = true)
    audioManager.playDailyChallengeCompleteFeedback(soundEnabled = true, vibrationEnabled = true)
    // Execution completed with zero crashes
    assertTrue(true)
  }
}
