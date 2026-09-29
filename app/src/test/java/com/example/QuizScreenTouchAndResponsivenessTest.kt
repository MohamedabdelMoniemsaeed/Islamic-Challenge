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
class QuizScreenTouchAndResponsivenessTest {

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
   * Test 1: Question 1 -> Answer -> Next -> Question 2 is immediately responsive and unlocked.
   * Proves that the answer lock from Question 1 is cleanly reset before Question 2 is rendered.
   */
  @Test
  fun test1_sequentialQuestions_optionsAreImmediatelyUnlockedAndResponsive() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    // Q1 initial state
    assertEquals(0, vm.currentIndex.value)
    assertFalse(vm.isAnswerLocked.value)
    assertFalse(vm.isAnswerSubmitted.value)
    assertEquals(QuestionStatus.UNANSWERED, vm.currentQuestionStatus.value)

    // Answer Q1
    val q1 = vm.currentQuestion!!
    val acceptedQ1 = vm.submitAnswer(q1.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    assertTrue("Q1 answer must be accepted", acceptedQ1)
    assertTrue("Q1 must be locked after answer", vm.isAnswerLocked.value)
    assertEquals(QuestionStatus.ANSWERED_CORRECT, vm.currentQuestionStatus.value)

    // Move to Q2
    val advanced = vm.nextQuestion()
    assertTrue("Advancing to next question must succeed", advanced)
    assertEquals(1, vm.currentIndex.value)

    // Verify Q2 is immediately UNLOCKED and ready for touch
    assertFalse("Q2 isAnswerLocked must be false", vm.isAnswerLocked.value)
    assertFalse("Q2 isAnswerSubmitted must be false", vm.isAnswerSubmitted.value)
    assertEquals("Q2 status must be UNANSWERED", QuestionStatus.UNANSWERED, vm.currentQuestionStatus.value)

    // Verify Q2 options immediately accept touch
    val q2 = vm.currentQuestion!!
    val acceptedQ2 = vm.submitAnswer(q2.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    assertTrue("Q2 answer must be immediately accepted on touch", acceptedQ2)
    assertEquals(QuestionStatus.ANSWERED_CORRECT, vm.currentQuestionStatus.value)
  }

  /**
   * Test 2: Multi-question round (5 questions) smoothly processes every question without freezing.
   */
  @Test
  fun test2_multiQuestionRound_allQuestionsAnswerableWithoutFreeze() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    for (i in 0 until 5) {
      assertEquals("Must be at question $i", i, vm.currentIndex.value)
      assertFalse("Question $i must not be locked before touch", vm.isAnswerLocked.value)
      assertFalse("Question $i must not be submitted before touch", vm.isAnswerSubmitted.value)
      assertEquals("Question $i status must be UNANSWERED", QuestionStatus.UNANSWERED, vm.currentQuestionStatus.value)

      val q = vm.currentQuestion!!
      val answered = vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
      assertTrue("Question $i must accept answer on touch", answered)
      assertTrue("Question $i must be locked after touch", vm.isAnswerLocked.value)

      if (i < 4) {
        val advanced = vm.nextQuestion()
        assertTrue("Must advance from question $i to ${i + 1}", advanced)
      }
    }
  }

  /**
   * Test 3: Rapid clicks on an option (50 rapid taps) only accept first tap and prevent duplicate scoring.
   */
  @Test
  fun test3_rapidClicksOnSameOption_onlyFirstAcceptedAndLocksQuestion() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val q = vm.currentQuestion!!
    val results = mutableListOf<Boolean>()
    repeat(50) {
      results.add(vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false))
    }

    assertEquals("First tap must be accepted", true, results[0])
    assertTrue("All subsequent 49 rapid taps must be rejected", results.drop(1).all { !it })
    assertEquals("Correct answer count must be exactly 1", 1, vm.correctAnswersCount.value)
    assertEquals("XP earned must be exactly q.xpReward", q.xpReward, vm.xpEarned.value)
  }

  /**
   * Test 4: Pause and resume cycle does not leave question locked.
   */
  @Test
  fun test4_pauseAndResumeCycle_doesNotLeaveQuestionLocked() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    vm.pauseQuiz()
    assertTrue("Quiz must be paused", vm.isPaused.value)
    // Touches rejected while paused
    val rejectedDuringPause = vm.submitAnswer(0, soundEnabled = false, vibrationEnabled = false)
    assertFalse("Touch must be rejected while paused", rejectedDuringPause)

    // Resume
    vm.resumeQuiz()
    assertFalse("Quiz must no longer be paused", vm.isPaused.value)
    assertFalse("Question must NOT be locked after resume", vm.isAnswerLocked.value)
    assertEquals(QuestionStatus.UNANSWERED, vm.currentQuestionStatus.value)

    // Touch must now work
    val q = vm.currentQuestion!!
    val accepted = vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    assertTrue("Touch must work after resume", accepted)
  }

  /**
   * Test 5: Timer ticking does not lock question before expiration.
   */
  @Test
  fun test5_timerTicksDoNotLockQuestionBeforeExpiration() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    // Advance 5 seconds
    advanceTimeBy(5_000L)
    testScheduler.runCurrent()
    assertEquals(15, vm.remainingSeconds.value)
    assertFalse("Must not be locked at 15s remaining", vm.isAnswerLocked.value)
    assertEquals(QuestionStatus.UNANSWERED, vm.currentQuestionStatus.value)

    // Advance 10 more seconds (5s remaining - warning zone)
    advanceTimeBy(10_000L)
    testScheduler.runCurrent()
    assertEquals(5, vm.remainingSeconds.value)
    assertFalse("Must not be locked at 5s remaining", vm.isAnswerLocked.value)

    // Tap answer during warning zone
    val q = vm.currentQuestion!!
    val accepted = vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    assertTrue("Answer must be accepted during warning zone", accepted)
  }

  /**
   * Test 6: Skip lifeline locks current question and allows next question to be fully responsive.
   */
  @Test
  fun test6_skipQuestion_locksCurrentQuestion_advancesCleanlyOnNext() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    vm.useLifeline(LifelineType.SKIP, isArabic = true)
    testScheduler.runCurrent()

    assertTrue("Skipped question must be locked", vm.isAnswerLocked.value)
    assertEquals(QuestionStatus.SKIPPED, vm.currentQuestionStatus.value)

    val advanced = vm.nextQuestion()
    assertTrue("Must advance after skip", advanced)
    assertEquals(1, vm.currentIndex.value)
    assertFalse("Next question must not be locked", vm.isAnswerLocked.value)
    assertEquals(QuestionStatus.UNANSWERED, vm.currentQuestionStatus.value)

    val q2 = vm.currentQuestion!!
    val accepted = vm.submitAnswer(q2.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    assertTrue("Next question must accept answer after skip", accepted)
  }

  /**
   * Test 7: Audio and vibration feedback enabled does not block answer submission.
   */
  @Test
  fun test7_audioFeedbackDispatchedAsync_doesNotBlockSubmitAnswer() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val q = vm.currentQuestion!!
    val accepted = vm.submitAnswer(q.correctAnswerIndex, soundEnabled = true, vibrationEnabled = true)
    assertTrue("Submit answer must return true without blocking", accepted)
    assertEquals(QuestionStatus.ANSWERED_CORRECT, vm.currentQuestionStatus.value)
  }
}
