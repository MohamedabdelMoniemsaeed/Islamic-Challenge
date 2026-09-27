package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.audio.AudioFeedbackManager
import com.example.data.models.GameMode
import com.example.data.models.LifelineType
import com.example.data.models.PlayerProfile
import com.example.data.models.Question
import com.example.data.models.QuestionStatus
import com.example.data.models.QuizCategory
import com.example.data.repository.PlayerRepository
import com.example.data.repository.QuestionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Collections

data class QuizResultState(
  val mode: GameMode,
  val category: QuizCategory?,
  val totalQuestions: Int,
  val correctCount: Int,
  val wrongCount: Int,
  val finalScore: Int,
  val accuracyPercentage: Int,
  val xpGained: Int,
  val coinsGained: Int,
  val didLevelUp: Boolean,
  val newLevel: Int
)

class QuizViewModel(
  private val questionRepository: QuestionRepository,
  private val playerRepository: PlayerRepository,
  private val audioManager: AudioFeedbackManager
) : ViewModel() {

  private val _questions = MutableStateFlow<List<Question>>(emptyList())
  val questions: StateFlow<List<Question>> = _questions.asStateFlow()

  private val _currentIndex = MutableStateFlow(0)
  val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

  private val _selectedOptionIndex = MutableStateFlow<Int?>(null)
  val selectedOptionIndex: StateFlow<Int?> = _selectedOptionIndex.asStateFlow()

  private val _isAnswerSubmitted = MutableStateFlow(false)
  val isAnswerSubmitted: StateFlow<Boolean> = _isAnswerSubmitted.asStateFlow()

  private val _isAnswerLocked = MutableStateFlow(false)
  val isAnswerLocked: StateFlow<Boolean> = _isAnswerLocked.asStateFlow()

  private val _currentQuestionStatus = MutableStateFlow(QuestionStatus.UNANSWERED)
  val currentQuestionStatus: StateFlow<QuestionStatus> = _currentQuestionStatus.asStateFlow()

  private val _questionStatuses = MutableStateFlow<Map<String, QuestionStatus>>(emptyMap())
  val questionStatuses: StateFlow<Map<String, QuestionStatus>> = _questionStatuses.asStateFlow()

  private val _isTimeExpired = MutableStateFlow(false)
  val isTimeExpired: StateFlow<Boolean> = _isTimeExpired.asStateFlow()

  private val _isAutoAdvancing = MutableStateFlow(false)
  val isAutoAdvancing: StateFlow<Boolean> = _isAutoAdvancing.asStateFlow()

  private val _isPaused = MutableStateFlow(false)
  val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

  private val _eliminatedOptions = MutableStateFlow<Set<Int>>(emptySet())
  val eliminatedOptions: StateFlow<Set<Int>> = _eliminatedOptions.asStateFlow()

  private val _remainingSeconds = MutableStateFlow(20)
  val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

  private val _activeHint = MutableStateFlow<String?>(null)
  val activeHint: StateFlow<String?> = _activeHint.asStateFlow()

  private val _correctAnswersCount = MutableStateFlow(0)
  val correctAnswersCount: StateFlow<Int> = _correctAnswersCount.asStateFlow()

  private val _wrongAnswersCount = MutableStateFlow(0)
  val wrongAnswersCount: StateFlow<Int> = _wrongAnswersCount.asStateFlow()

  private val _score = MutableStateFlow(0)
  val score: StateFlow<Int> = _score.asStateFlow()

  private val _xpEarned = MutableStateFlow(0)
  val xpEarned: StateFlow<Int> = _xpEarned.asStateFlow()

  private val _coinsEarned = MutableStateFlow(0)
  val coinsEarned: StateFlow<Int> = _coinsEarned.asStateFlow()

  private val _lastEarnedXp = MutableStateFlow(0)
  val lastEarnedXp: StateFlow<Int> = _lastEarnedXp.asStateFlow()

  private val _lastEarnedCoins = MutableStateFlow(0)
  val lastEarnedCoins: StateFlow<Int> = _lastEarnedCoins.asStateFlow()

  private val _quizFinished = MutableStateFlow(false)
  val quizFinished: StateFlow<Boolean> = _quizFinished.asStateFlow()

  private val _resultState = MutableStateFlow<QuizResultState?>(null)
  val resultState: StateFlow<QuizResultState?> = _resultState.asStateFlow()

  private var currentMode: GameMode = GameMode.QUICK_CHALLENGE
  private var currentCategory: QuizCategory? = null
  private var timerJob: Job? = null
  private var autoAdvanceJob: Job? = null

  private val answerLock = Any()
  private val advanceLock = Any()
  private val answeredQuestionIds = Collections.synchronizedSet(mutableSetOf<String>())

  val currentQuestion: Question?
    get() = _questions.value.getOrNull(_currentIndex.value)

  fun startQuiz(
    mode: GameMode,
    category: QuizCategory? = null
  ) {
    autoAdvanceJob?.cancel()
    autoAdvanceJob = null
    timerJob?.cancel()

    currentMode = mode
    currentCategory = category
    val loaded = questionRepository.getQuestionsForMode(mode, category)
    _questions.value = loaded
    _currentIndex.value = 0
    _selectedOptionIndex.value = null
    _isAnswerSubmitted.value = false
    _isAnswerLocked.value = false
    _currentQuestionStatus.value = QuestionStatus.UNANSWERED
    _questionStatuses.value = emptyMap()
    answeredQuestionIds.clear()
    _isTimeExpired.value = false
    _isAutoAdvancing.value = false
    _isPaused.value = false
    _eliminatedOptions.value = emptySet()
    _correctAnswersCount.value = 0
    _wrongAnswersCount.value = 0
    _score.value = 0
    _xpEarned.value = 0
    _coinsEarned.value = 0
    _lastEarnedXp.value = 0
    _lastEarnedCoins.value = 0
    _quizFinished.value = false
    _resultState.value = null

    startTimerForCurrentQuestion()
  }

  private fun startTimerForCurrentQuestion() {
    timerJob?.cancel()
    if (!currentMode.hasTimer) {
      _remainingSeconds.value = 0
      return
    }

    _remainingSeconds.value = currentMode.timerSecondsPerQuestion
    _isTimeExpired.value = false
    timerJob = viewModelScope.launch {
      while (_remainingSeconds.value > 0 && !_isAnswerSubmitted.value && !_isAnswerLocked.value) {
        delay(1000L)
        if (!_isPaused.value && !_isAnswerSubmitted.value && !_isAnswerLocked.value) {
          _remainingSeconds.value = _remainingSeconds.value - 1
          if (_remainingSeconds.value == 5) {
            audioManager.playTimerWarningFeedback(soundEnabled = false, vibrationEnabled = true)
          }
        }
      }
      if (_remainingSeconds.value <= 0 && !_isAnswerSubmitted.value && !_isAnswerLocked.value && !_isPaused.value) {
        if (currentMode.allowsPenalties) {
          handleTimeExpired()
        }
      }
    }
  }

  private fun handleTimeExpired() {
    synchronized(answerLock) {
      val q = currentQuestion ?: return
      if (_isAnswerLocked.value || _isAnswerSubmitted.value || _isTimeExpired.value || answeredQuestionIds.contains(q.id)) {
        return
      }
      _isTimeExpired.value = true
      _isAnswerLocked.value = true
      _isAnswerSubmitted.value = true
      _currentQuestionStatus.value = QuestionStatus.EXPIRED
      answeredQuestionIds.add(q.id)
      _questionStatuses.value = _questionStatuses.value + (q.id to QuestionStatus.EXPIRED)
      _wrongAnswersCount.value = _wrongAnswersCount.value + 1
      _lastEarnedXp.value = 0
      _lastEarnedCoins.value = 0
      audioManager.playIncorrectFeedback(soundEnabled = true, vibrationEnabled = true)

      if (currentMode == GameMode.SURVIVAL_MODE) {
        finishQuiz()
      } else {
        scheduleAutoAdvance()
      }
    }
  }

  /**
   * Central atomic submission guard for answering questions.
   * Ensures that:
   * 1. Only one answer can ever be submitted per question in a round.
   * 2. Double-clicks or rapid multi-touches are strictly ignored.
   * 3. Timer is cancelled synchronously to prevent expiry race conditions.
   * 4. Auto-advance timer is scheduled comfortably.
   */
  fun submitAnswer(
    answerIndex: Int,
    soundEnabled: Boolean = true,
    vibrationEnabled: Boolean = true
  ): Boolean {
    synchronized(answerLock) {
      val q = currentQuestion ?: return false

      if (_isAnswerLocked.value || _isAnswerSubmitted.value || _isTimeExpired.value || _isPaused.value) {
        return false
      }
      if (answeredQuestionIds.contains(q.id) || _currentQuestionStatus.value != QuestionStatus.UNANSWERED) {
        return false
      }
      if (_eliminatedOptions.value.contains(answerIndex)) {
        return false
      }

      // 1. Immediate lock & timer stop
      _isAnswerLocked.value = true
      _isAnswerSubmitted.value = true
      _selectedOptionIndex.value = answerIndex
      answeredQuestionIds.add(q.id)
      timerJob?.cancel()

      // 2. Record answer and rewards
      val isCorrect = answerIndex == q.correctAnswerIndex
      val status = if (isCorrect) QuestionStatus.ANSWERED_CORRECT else QuestionStatus.ANSWERED_WRONG
      _currentQuestionStatus.value = status
      _questionStatuses.value = _questionStatuses.value + (q.id to status)

      if (isCorrect) {
        val earnedXp = q.xpReward
        val earnedCoins = q.coinReward
        _lastEarnedXp.value = earnedXp
        _lastEarnedCoins.value = earnedCoins
        _correctAnswersCount.value = _correctAnswersCount.value + 1
        _score.value = _score.value + (q.difficulty.xpMultiplier * 10)
        _xpEarned.value = _xpEarned.value + earnedXp
        _coinsEarned.value = _coinsEarned.value + earnedCoins
        audioManager.playCorrectFeedback(soundEnabled, vibrationEnabled)
      } else {
        _lastEarnedXp.value = 0
        _lastEarnedCoins.value = 0
        _wrongAnswersCount.value = _wrongAnswersCount.value + 1
        audioManager.playIncorrectFeedback(soundEnabled, vibrationEnabled)
      }

      // 3. Schedule automatic transition to next question (or results)
      scheduleAutoAdvance()
      return true
    }
  }

  fun selectOption(optionIndex: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) {
    submitAnswer(optionIndex, soundEnabled, vibrationEnabled)
  }

  fun scheduleAutoAdvance(delayMs: Long = 1600L) {
    autoAdvanceJob?.cancel()
    _isAutoAdvancing.value = true
    autoAdvanceJob = viewModelScope.launch {
      delay(delayMs)
      _isAutoAdvancing.value = false
      nextQuestion()
    }
  }

  fun nextQuestion() {
    synchronized(advanceLock) {
      autoAdvanceJob?.cancel()
      autoAdvanceJob = null
      _isAutoAdvancing.value = false

      val currIdx = _currentIndex.value
      val total = _questions.value.size

      if (currIdx >= total) {
        finishQuiz()
        return
      }

      // Must be answered, expired, or skipped to advance; cannot advance an UNANSWERED question
      if (!_isAnswerSubmitted.value && !_isTimeExpired.value && _currentQuestionStatus.value == QuestionStatus.UNANSWERED) {
        return
      }

      val wasWrong = _currentQuestionStatus.value == QuestionStatus.ANSWERED_WRONG || _isTimeExpired.value
      if (currentMode == GameMode.SURVIVAL_MODE && wasWrong) {
        finishQuiz()
        return
      }

      val nextIdx = currIdx + 1
      if (nextIdx < total) {
        _currentIndex.value = nextIdx
        _selectedOptionIndex.value = null
        _isAnswerSubmitted.value = false
        _isAnswerLocked.value = false
        _currentQuestionStatus.value = QuestionStatus.UNANSWERED
        _isTimeExpired.value = false
        _eliminatedOptions.value = emptySet()
        _lastEarnedXp.value = 0
        _lastEarnedCoins.value = 0
        startTimerForCurrentQuestion()
      } else {
        finishQuiz()
      }
    }
  }

  fun useLifeline(type: LifelineType, isArabic: Boolean) {
    if (_isAnswerSubmitted.value || _isAnswerLocked.value || _isTimeExpired.value || _isPaused.value) return
    val q = currentQuestion ?: return

    viewModelScope.launch {
      val success = playerRepository.useLifeline(type)
      if (!success) return@launch

      when (type) {
        LifelineType.FIFTY_FIFTY -> {
          if (_isAnswerSubmitted.value || _isAnswerLocked.value) return@launch
          val wrongIndices = (0 until 4).filter { it != q.correctAnswerIndex }
          val toEliminate = wrongIndices.shuffled().take(2).toSet()
          _eliminatedOptions.value = toEliminate
        }
        LifelineType.HINT -> {
          if (_isAnswerSubmitted.value || _isAnswerLocked.value) return@launch
          _activeHint.value = q.getHint(isArabic).ifEmpty {
            if (isArabic) "تأمل في سياق السؤال بدقة واستعن بالله." else "Reflect upon the context of the question carefully."
          }
        }
        LifelineType.EXTRA_TIME -> {
          if (_isAnswerSubmitted.value || _isAnswerLocked.value) return@launch
          _remainingSeconds.value = _remainingSeconds.value + 15
        }
        LifelineType.SKIP -> {
          synchronized(answerLock) {
            if (_isAnswerSubmitted.value || _isAnswerLocked.value || answeredQuestionIds.contains(q.id)) {
              return@launch
            }
            _isAnswerLocked.value = true
            _isAnswerSubmitted.value = true
            _currentQuestionStatus.value = QuestionStatus.SKIPPED
            answeredQuestionIds.add(q.id)
            _questionStatuses.value = _questionStatuses.value + (q.id to QuestionStatus.SKIPPED)
            timerJob?.cancel()
          }
          nextQuestion()
        }
      }
    }
  }

  fun dismissHint() {
    _activeHint.value = null
  }

  fun pauseQuiz() {
    _isPaused.value = true
    timerJob?.cancel()
    autoAdvanceJob?.cancel()
  }

  fun resumeQuiz() {
    _isPaused.value = false
    if (!_isAnswerSubmitted.value && !_isAnswerLocked.value && !_quizFinished.value) {
      resumeTimer()
    } else if ((_isAnswerSubmitted.value || _isTimeExpired.value) && !_quizFinished.value) {
      scheduleAutoAdvance()
    }
  }

  fun restartRound() {
    _isPaused.value = false
    startQuiz(currentMode, currentCategory)
  }

  fun pauseTimer() {
    timerJob?.cancel()
  }

  fun resumeTimer() {
    if (_remainingSeconds.value > 0 && !_isAnswerSubmitted.value && !_isAnswerLocked.value && !_quizFinished.value && currentMode.hasTimer) {
      timerJob?.cancel()
      timerJob = viewModelScope.launch {
        while (_remainingSeconds.value > 0 && !_isAnswerSubmitted.value && !_isAnswerLocked.value && !_isPaused.value) {
          delay(1000L)
          if (!_isPaused.value && !_isAnswerSubmitted.value && !_isAnswerLocked.value) {
            _remainingSeconds.value = _remainingSeconds.value - 1
            if (_remainingSeconds.value == 5) {
              audioManager.playTimerWarningFeedback(soundEnabled = false, vibrationEnabled = true)
            }
          }
        }
        if (_remainingSeconds.value <= 0 && !_isAnswerSubmitted.value && !_isAnswerLocked.value && !_isPaused.value) {
          if (currentMode.allowsPenalties) {
            handleTimeExpired()
          }
        }
      }
    }
  }

  private fun finishQuiz() {
    autoAdvanceJob?.cancel()
    autoAdvanceJob = null
    timerJob?.cancel()
    if (_quizFinished.value) return
    _quizFinished.value = true

    viewModelScope.launch {
      val (updatedProfile, didLevelUp) = playerRepository.recordGameResults(
        correctCount = _correctAnswersCount.value,
        wrongCount = _wrongAnswersCount.value,
        xpGained = _xpEarned.value,
        coinsGained = _coinsEarned.value,
        category = currentCategory,
        mode = currentMode
      )

      val total = _correctAnswersCount.value + _wrongAnswersCount.value
      val accuracy = if (total > 0) ((_correctAnswersCount.value.toFloat() / total) * 100).toInt() else 0

      _resultState.value = QuizResultState(
        mode = currentMode,
        category = currentCategory,
        totalQuestions = total,
        correctCount = _correctAnswersCount.value,
        wrongCount = _wrongAnswersCount.value,
        finalScore = _score.value,
        accuracyPercentage = accuracy,
        xpGained = _xpEarned.value,
        coinsGained = _coinsEarned.value,
        didLevelUp = didLevelUp,
        newLevel = updatedProfile.level
      )
    }
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
    autoAdvanceJob?.cancel()
  }
}
