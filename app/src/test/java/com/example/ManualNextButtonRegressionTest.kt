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
class ManualNextButtonRegressionTest {

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
   * TEST 1: بعد الإجابة لا يتم الانتقال تلقائيًا.
   * Expected: currentQuestionIndex unchanged حتى يضغط المستخدم Next.
   */
  @Test
  fun test1_noAutoAdvance_currentIndexRemainsUnchangedUntilManualNext() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    assertEquals("Initial index must be 0", 0, vm.currentIndex.value)
    val q = vm.currentQuestion!!
    vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)

    assertEquals("Index must remain 0 immediately after answering", 0, vm.currentIndex.value)
    assertFalse("Auto-advancing must be false", vm.isAutoAdvancing.value)

    // Simulate extensive time passing (1.6s, 5s, 10s)
    advanceTimeBy(10_000L)
    testScheduler.runCurrent()

    assertEquals("Index must remain 0 after 10 seconds of inactivity", 0, vm.currentIndex.value)
    assertFalse("Quiz must not finish on its own", vm.quizFinished.value)

    // Only manual Next click moves to question 1
    val advanced = vm.nextQuestion()
    assertTrue("Manual next must succeed", advanced)
    assertEquals("Index must now be 1", 1, vm.currentIndex.value)
  }

  /**
   * TEST 2: الإجابة الأولى فقط يتم قبولها.
   */
  @Test
  fun test2_onlyFirstAnswerAccepted() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val q = vm.currentQuestion!!
    val correct = q.correctAnswerIndex
    val wrong = (0..3).first { it != correct }

    val firstAccepted = vm.submitAnswer(correct, soundEnabled = false, vibrationEnabled = false)
    assertTrue("First submission must be accepted", firstAccepted)
    assertEquals(correct, vm.selectedOptionIndex.value)

    // Subsequent submissions on same question
    val secondAccepted = vm.submitAnswer(wrong, soundEnabled = false, vibrationEnabled = false)
    val thirdAccepted = vm.submitAnswer(correct, soundEnabled = false, vibrationEnabled = false)

    assertFalse("Second submission must be rejected", secondAccepted)
    assertFalse("Third submission must be rejected", thirdAccepted)
    assertEquals("Selected option must remain the first one", correct, vm.selectedOptionIndex.value)
    assertEquals(1, vm.correctAnswersCount.value)
    assertEquals(0, vm.wrongAnswersCount.value)
  }

  /**
   * TEST 3: لا يمكن الإجابة على السؤال بعد التصحيح.
   */
  @Test
  fun test3_cannotAnswerAfterCorrection() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val q = vm.currentQuestion!!
    val wrong = (0..3).first { it != q.correctAnswerIndex }
    val correct = q.correctAnswerIndex

    // Submit wrong answer
    val firstAttempt = vm.submitAnswer(wrong, soundEnabled = false, vibrationEnabled = false)
    assertTrue("Initial answer accepted", firstAttempt)
    assertEquals(QuestionStatus.ANSWERED_WRONG, vm.currentQuestionStatus.value)
    assertTrue(vm.isAnswerLocked.value)

    // Try to correct to the right answer
    val correctionAttempt = vm.submitAnswer(correct, soundEnabled = false, vibrationEnabled = false)
    assertFalse("Correction after lock must be strictly rejected", correctionAttempt)
    assertEquals(QuestionStatus.ANSWERED_WRONG, vm.currentQuestionStatus.value)
    assertEquals(0, vm.correctAnswersCount.value)
    assertEquals(1, vm.wrongAnswersCount.value)
  }

  /**
   * TEST 4: زر Next ينقل لسؤال واحد فقط.
   */
  @Test
  fun test4_singleNextAdvancesByExactlyOneQuestion() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    assertEquals(0, vm.currentIndex.value)
    val q1 = vm.currentQuestion!!
    vm.submitAnswer(q1.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)

    val moved = vm.nextQuestion()
    assertTrue(moved)
    assertEquals("Should advance from index 0 to 1 only", 1, vm.currentIndex.value)
    assertEquals(QuestionStatus.UNANSWERED, vm.currentQuestionStatus.value)
    assertFalse(vm.isAnswerLocked.value)
    assertFalse(vm.isAnswerSubmitted.value)
  }

  /**
   * TEST 5: 50 ضغطة سريعة على Next لا تتخطى أكثر من سؤال.
   */
  @Test
  fun test5_fiftyRapidNextClicksDoNotSkipMultipleQuestions() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    assertEquals(0, vm.currentIndex.value)
    val q1 = vm.currentQuestion!!
    vm.submitAnswer(q1.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)

    // First click advances to index 1
    val firstClick = vm.nextQuestion()
    assertTrue("First next click must advance", firstClick)
    assertEquals(1, vm.currentIndex.value)

    // 49 rapid subsequent clicks on the newly unanswered question
    for (i in 2..50) {
      val result = vm.nextQuestion()
      assertFalse("Click $i must be rejected because question 1 is unanswered", result)
    }

    assertEquals("Index must remain 1 after 50 rapid clicks", 1, vm.currentIndex.value)
  }

  /**
   * TEST 6: آخر سؤال يفتح ResultsScreen مرة واحدة.
   */
  @Test
  fun test6_lastQuestionOpensResultsOnceOnly() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE) // 10 questions

    // Answer all questions 0 through 8
    for (step in 0 until 9) {
      val q = vm.currentQuestion!!
      vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
      val adv = vm.nextQuestion()
      assertTrue(adv)
    }

    assertEquals(9, vm.currentIndex.value) // Last question (index 9 of 10)
    val lastQ = vm.currentQuestion!!
    vm.submitAnswer(lastQ.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    assertFalse("Quiz must not finish until user clicks View Results", vm.quizFinished.value)

    // Click "View Results" (calls nextQuestion)
    val finish1 = vm.nextQuestion()
    assertTrue("First finish click must succeed", finish1)
    assertTrue("Quiz must now be finished", vm.quizFinished.value)

    // 20 rapid subsequent clicks on finish
    for (i in 1..20) {
      val finishSubsequent = vm.nextQuestion()
      assertFalse("Subsequent finish click $i must be rejected", finishSubsequent)
    }

    testScheduler.runCurrent()
    assertNotNull("Result state must exist", vm.resultState.value)
    assertEquals(10, vm.resultState.value!!.correctCount)
    assertEquals(0, vm.resultState.value!!.wrongCount)
  }

  /**
   * TEST 7: Skip ثم محاولة الإجابة يتم رفضها.
   */
  @Test
  fun test7_skipThenAttemptAnswerIsRejected() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val q = vm.currentQuestion!!
    // Use SKIP lifeline
    vm.useLifeline(LifelineType.SKIP, isArabic = true)
    testScheduler.runCurrent()

    assertEquals(QuestionStatus.SKIPPED, vm.currentQuestionStatus.value)
    assertTrue("Answer locked after skip", vm.isAnswerLocked.value)
    assertTrue("Answer submitted after skip", vm.isAnswerSubmitted.value)
    assertEquals("Current index remains 0 after skip; waiting for Next button", 0, vm.currentIndex.value)

    // Attempting to answer after skip must be rejected
    val answerAttempt = vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    assertFalse("Answering skipped question must be rejected", answerAttempt)
    assertEquals(QuestionStatus.SKIPPED, vm.currentQuestionStatus.value)

    // Manual next button progresses to next question
    val advanced = vm.nextQuestion()
    assertTrue(advanced)
    assertEquals(1, vm.currentIndex.value)
  }

  /**
   * TEST 8: Expire ثم محاولة الإجابة يتم رفضها.
   */
  @Test
  fun test8_expireThenAttemptAnswerIsRejected() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val q = vm.currentQuestion!!
    assertEquals(20, vm.remainingSeconds.value)

    // Let the question timer run down (20 seconds + buffer)
    advanceTimeBy(20_100L)
    testScheduler.runCurrent()

    assertTrue("Time must be expired", vm.isTimeExpired.value)
    assertEquals(QuestionStatus.EXPIRED, vm.currentQuestionStatus.value)
    assertTrue(vm.isAnswerLocked.value)
    assertEquals("Index remains 0 to let user view explanation", 0, vm.currentIndex.value)

    // Attempt to submit answer after timeout
    val answeredAfterExpiry = vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    assertFalse("Submission after expiry must be rejected", answeredAfterExpiry)
    assertEquals(QuestionStatus.EXPIRED, vm.currentQuestionStatus.value)
    assertEquals(0, vm.correctAnswersCount.value)
    assertEquals(1, vm.wrongAnswersCount.value)

    // Manual Next button moves to next question
    val advanced = vm.nextQuestion()
    assertTrue(advanced)
    assertEquals(1, vm.currentIndex.value)
  }

  /**
   * TEST 9: Timer لا ينتقل بالسؤال تلقائيًا بعد الإجابة.
   */
  @Test
  fun test9_timerDoesNotAutoAdvanceAfterAnswer() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val q = vm.currentQuestion!!
    // Answer with 15 seconds remaining
    advanceTimeBy(5_000L)
    testScheduler.runCurrent()
    assertEquals(15, vm.remainingSeconds.value)

    vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)

    // Timer must be stopped immediately
    val remainingAtAnswer = vm.remainingSeconds.value

    // Advance by 30 seconds
    advanceTimeBy(30_000L)
    testScheduler.runCurrent()

    assertEquals("Index must NOT advance after 30 seconds", 0, vm.currentIndex.value)
    assertEquals("Timer seconds must not decrease after answer", remainingAtAnswer, vm.remainingSeconds.value)
    assertFalse("Time expired must not become true if already answered", vm.isTimeExpired.value)
    assertFalse("Quiz must not finish automatically", vm.quizFinished.value)
  }

  /**
   * TEST 10: XP وCoins لا تتكرر بسبب الضغط على Next.
   */
  @Test
  fun test10_xpAndCoinsDoNotDuplicateOnNext() = runTest(testScheduler) {
    val vm = QuizViewModel(questionRepository, playerRepository, audioManager)
    vm.startQuiz(GameMode.QUICK_CHALLENGE)

    val q = vm.currentQuestion!!
    val expectedXp = q.xpReward
    val expectedCoins = q.coinReward

    vm.submitAnswer(q.correctAnswerIndex, soundEnabled = false, vibrationEnabled = false)
    assertEquals(expectedXp, vm.xpEarned.value)
    assertEquals(expectedCoins, vm.coinsEarned.value)

    // Rapidly tap Next 10 times
    for (i in 1..10) {
      vm.nextQuestion()
    }

    // Question progressed to index 1, XP and coins must be identical to single answer rewards
    assertEquals(1, vm.currentIndex.value)
    assertEquals("XP must not increase due to Next button", expectedXp, vm.xpEarned.value)
    assertEquals("Coins must not increase due to Next button", expectedCoins, vm.coinsEarned.value)
  }
}
