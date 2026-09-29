package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.audio.AudioFeedbackManager
import com.example.data.local.AppDatabase
import com.example.data.local.PlayerDao
import com.example.data.models.GameMode
import com.example.data.models.LifelineType
import com.example.data.models.QuestionStatus
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GameplayAtomicGuardTest {

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

  /**
   * Test 1 — Double Answer:
   * submitAnswer(A) followed by submitAnswer(B).
   * Expected: Only A is recorded, B is rejected.
   */
  @Test
  fun test1_doubleAnswer_onlyFirstRecorded() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val q = vm.currentQuestion!!
    val correctIdx = q.correctAnswerIndex
    val wrongIdx = (0..3).first { it != correctIdx }

    val firstSubmitted = vm.submitAnswer(correctIdx, soundEnabled = false, vibrationEnabled = false)
    val secondSubmitted = vm.submitAnswer(wrongIdx, soundEnabled = false, vibrationEnabled = false)

    assertTrue("First answer submission must succeed", firstSubmitted)
    assertFalse("Second answer submission on same question must be rejected", secondSubmitted)

    assertEquals("Selected option must remain the first one", correctIdx, vm.selectedOptionIndex.value)
    assertEquals("Current status must be ANSWERED_CORRECT", QuestionStatus.ANSWERED_CORRECT, vm.currentQuestionStatus.value)
    assertEquals(1, vm.correctAnswersCount.value)
    assertEquals(0, vm.wrongAnswersCount.value)
  }

  /**
   * Test 2 — Same Answer Twice:
   * submitAnswer(A) followed by submitAnswer(A).
   * Expected: Only one answer, one reward, one XP, one coin reward.
   */
  @Test
  fun test2_sameAnswerTwice_rewardedOnce() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val q = vm.currentQuestion!!
    val correctIdx = q.correctAnswerIndex

    val first = vm.submitAnswer(correctIdx, soundEnabled = false, vibrationEnabled = false)
    val expectedXp = vm.xpEarned.value
    val expectedCoins = vm.coinsEarned.value
    val expectedScore = vm.score.value

    val second = vm.submitAnswer(correctIdx, soundEnabled = false, vibrationEnabled = false)

    assertTrue("First submission must succeed", first)
    assertFalse("Duplicate submission must be rejected", second)
    assertEquals(1, vm.correctAnswersCount.value)
    assertEquals(expectedXp, vm.xpEarned.value)
    assertEquals(expectedCoins, vm.coinsEarned.value)
    assertEquals(expectedScore, vm.score.value)
  }

  /**
   * Test 3 — Rapid Multi Click:
   * Execute submitAnswer 20 times rapidly on the same question.
   * Expected: Exactly 1 accepted answer, 0 duplicate answers.
   */
  @Test
  fun test3_rapidMultiClick_exactlyOneAccepted() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val q = vm.currentQuestion!!
    var acceptedCount = 0

    // Rapidly submit 20 times alternating options
    for (i in 0 until 20) {
      val accepted = vm.submitAnswer(i % 4, soundEnabled = false, vibrationEnabled = false)
      if (accepted) acceptedCount++
    }

    assertEquals("Exactly 1 submission must be accepted", 1, acceptedCount)
    assertTrue("Answer must be locked", vm.isAnswerLocked.value)
    assertTrue("Answer must be marked submitted", vm.isAnswerSubmitted.value)
    val totalAnswered = vm.correctAnswersCount.value + vm.wrongAnswersCount.value
    assertEquals("Exactly one answer counted in stats", 1, totalAnswered)
  }

  /**
   * Test 4 — Answer Then Timer:
   * submitAnswer() then timer reaches 0.
   * Expected: ANSWERED, Not EXPIRED.
   */
  @Test
  fun test4_answerThenTimer_statusRemainsAnswered() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val q = vm.currentQuestion!!
    val correctIdx = q.correctAnswerIndex

    val submitted = vm.submitAnswer(correctIdx, soundEnabled = false, vibrationEnabled = false)
    assertTrue(submitted)

    // Advance 500ms before auto-advance triggers
    advanceTimeBy(500L)

    assertFalse("Timer must not trigger expiry after answer", vm.isTimeExpired.value)
    assertEquals(QuestionStatus.ANSWERED_CORRECT, vm.currentQuestionStatus.value)
    assertEquals(0, vm.wrongAnswersCount.value)
  }

  /**
   * Test 5 — Timer Then Answer:
   * Timer expires first, then user attempts submitAnswer().
   * Expected: EXPIRED, Answer rejected.
   */
  @Test
  fun test5_timerThenAnswer_answerRejected() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    // Let the timer count down to 0 (20s)
    advanceTimeBy(20_100L)
    testScheduler.runCurrent()

    assertTrue("Time must be expired", vm.isTimeExpired.value)
    assertEquals(QuestionStatus.EXPIRED, vm.currentQuestionStatus.value)
    assertEquals(1, vm.wrongAnswersCount.value)

    val q = vm.currentQuestion!!
    val answerSubmitted = vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    assertFalse("Answer must be rejected after timeout", answerSubmitted)
    assertEquals(QuestionStatus.EXPIRED, vm.currentQuestionStatus.value)
  }

  /**
   * Test 6 — Skip Then Answer:
   * skip() used, then attempt submitAnswer().
   * Expected: SKIPPED, Answer rejected.
   */
  @Test
  fun test6_skipThenAnswer_answerRejected() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val q1 = vm.currentQuestion!!
    val q1Id = q1.id

    // Use Skip lifeline
    vm.useLifeline(LifelineType.SKIP, isArabic = true)
    testScheduler.runCurrent()

    // Status of Q1 in questionStatuses must be SKIPPED
    assertEquals(QuestionStatus.SKIPPED, vm.questionStatuses.value[q1Id])
    assertEquals(QuestionStatus.SKIPPED, vm.currentQuestionStatus.value)

    // With manual Next button, question remains at index 0 until user taps Next
    assertEquals(0, vm.currentIndex.value)
    val answerSubmitted = vm.submitAnswer(q1.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    assertFalse("Answer must be rejected after skip", answerSubmitted)

    // Manual next button progresses to question 1
    val advanced = vm.nextQuestion()
    assertTrue(advanced)
    assertEquals(1, vm.currentIndex.value)
    assertEquals(0, vm.correctAnswersCount.value)
  }

  /**
   * Test 7 — Last Question:
   * Answer last question -> Complete Game -> Results Screen with valid index.
   */
  @Test
  fun test7_lastQuestion_completesGameCleanly() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    // Advance to question 9 (the 10th and last question)
    for (i in 0 until 9) {
      val q = vm.currentQuestion!!
      vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
      vm.nextQuestion()
    }
    assertEquals(9, vm.currentIndex.value)

    // Answer the 10th question
    val lastQ = vm.currentQuestion!!
    vm.submitAnswer(lastQ.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    vm.nextQuestion()
    testScheduler.runCurrent()

    assertTrue("Quiz must be marked finished on last question", vm.quizFinished.value)
    val result = vm.resultState.value
    assertNotNull("ResultState must not be null", result)
    assertEquals(10, result!!.totalQuestions)
    assertEquals(10, result.correctCount)
  }

  /**
   * Test 8 — No Duplicate Rewards:
   * Submit same answer multiple times.
   * Expected: XP = reward once, Coins = reward once, Stats = counted once.
   */
  @Test
  fun test8_noDuplicateRewards() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val q = vm.currentQuestion!!
    val expectedSingleXp = q.xpReward
    val expectedSingleCoins = q.coinReward

    vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)

    assertEquals(expectedSingleXp, vm.xpEarned.value)
    assertEquals(expectedSingleCoins, vm.coinsEarned.value)
    assertEquals(1, vm.correctAnswersCount.value)
    assertEquals(0, vm.wrongAnswersCount.value)
  }

  /**
   * Test 9 — Question Progression:
   * Q1 answer -> nextQuestion() -> currentQuestion = Q2 exactly once.
   * Repeated calls on unanswered Q2 do NOT advance to Q3.
   */
  @Test
  fun test9_questionProgression_noDoubleAdvance() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    assertEquals(0, vm.currentIndex.value)
    val q1 = vm.currentQuestion!!

    vm.submitAnswer(q1.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    vm.nextQuestion()

    assertEquals("Must advance to Q2 (index 1)", 1, vm.currentIndex.value)

    // Attempting to advance again without answering Q2 must be rejected
    vm.nextQuestion()
    vm.nextQuestion()
    assertEquals("Must remain at index 1 until Q2 is answered", 1, vm.currentIndex.value)
  }

  /**
   * Test 10 — Full Game Integrity:
   * 10 Questions: Correct + Wrong + Skipped + Expired = 10.
   * 0 Duplicate answers, 0 duplicate rewards.
   */
  @Test
  fun test10_fullGameIntegrity() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    // Q0: Correct
    vm.submitAnswer(vm.currentQuestion!!.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    vm.nextQuestion()

    // Q1: Wrong
    val wrongIdx1 = (0..3).first { it != vm.currentQuestion!!.correctAnswerIndex }
    vm.submitAnswer(wrongIdx1, soundEnabled = false, vibrationEnabled = false)
    vm.nextQuestion()

    // Q2: Correct with double clicks
    val correctIdx2 = vm.currentQuestion!!.correctAnswerIndex
    vm.submitAnswer(correctIdx2, soundEnabled = false, vibrationEnabled = false)
    vm.submitAnswer(correctIdx2, soundEnabled = false, vibrationEnabled = false)
    vm.nextQuestion()

    // Q3: Correct
    vm.submitAnswer(vm.currentQuestion!!.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    vm.nextQuestion()

    // Q4: Correct
    vm.submitAnswer(vm.currentQuestion!!.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    vm.nextQuestion()

    // Q5: Correct
    vm.submitAnswer(vm.currentQuestion!!.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    vm.nextQuestion()

    // Q6: Correct
    vm.submitAnswer(vm.currentQuestion!!.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    vm.nextQuestion()

    // Q7: Correct
    vm.submitAnswer(vm.currentQuestion!!.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    vm.nextQuestion()

    // Q8: Correct
    vm.submitAnswer(vm.currentQuestion!!.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    vm.nextQuestion()

    // Q9: Correct
    vm.submitAnswer(vm.currentQuestion!!.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    vm.nextQuestion()
    testScheduler.runCurrent()

    assertTrue("Quiz must be finished", vm.quizFinished.value)
    val result = vm.resultState.value
    assertNotNull(result)
    assertEquals(10, result!!.totalQuestions)
    assertEquals(9, result.correctCount)
    assertEquals(1, result.wrongCount)
  }

  /**
   * Test 11 — Auto-Advance Delay:
   * When submitAnswer() is called, auto-advance automatically transitions to the next question
   * after the scheduled delay without requiring manual interaction.
   */
  /**
   * Regression Test: Manual Next Button (No auto-advance)
   * After answering, question does NOT advance automatically even after time passes.
   * Only advances when nextQuestion() is manually invoked.
   */
  @Test
  fun test11_noAutoAdvance_requiresManualNext() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    assertEquals(0, vm.currentIndex.value)
    val q = vm.currentQuestion!!
    vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)

    assertFalse("Auto-advancing must be false", vm.isAutoAdvancing.value)
    assertEquals("Still at index 0 after answering", 0, vm.currentIndex.value)

    // Advance virtual time by 5000ms - must remain at index 0!
    advanceTimeBy(5000L)
    testScheduler.runCurrent()

    assertEquals("Must NOT automatically advance to index 1 after delay", 0, vm.currentIndex.value)

    // Manual tap on Next
    val advanced = vm.nextQuestion()
    assertTrue("Manual next must succeed", advanced)
    assertEquals("Must advance to index 1 after manual next", 1, vm.currentIndex.value)
    assertFalse("New question must not be locked", vm.isAnswerLocked.value)
    assertEquals(QuestionStatus.UNANSWERED, vm.currentQuestionStatus.value)
  }
}
