package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizationManager
import com.example.data.models.GameMode
import com.example.data.models.LifelineType
import com.example.data.models.PlayerProfile
import com.example.data.models.Question
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.viewmodels.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
  quizViewModel: QuizViewModel,
  profile: PlayerProfile,
  language: AppLanguage,
  soundEnabled: Boolean,
  vibrationEnabled: Boolean,
  onQuizFinished: () -> Unit,
  onExit: () -> Unit
) {
  val strings = LocalizationManager.get(language)
  val isAr = language.isRtl

  val questions by quizViewModel.questions.collectAsState()
  val currentIndex by quizViewModel.currentIndex.collectAsState()
  val selectedOptionIndex by quizViewModel.selectedOptionIndex.collectAsState()
  val isAnswerSubmitted by quizViewModel.isAnswerSubmitted.collectAsState()
  val isAnswerLocked by quizViewModel.isAnswerLocked.collectAsState()
  val isAutoAdvancing by quizViewModel.isAutoAdvancing.collectAsState()
  val isTimeExpired by quizViewModel.isTimeExpired.collectAsState()
  val isPaused by quizViewModel.isPaused.collectAsState()
  val eliminatedOptions by quizViewModel.eliminatedOptions.collectAsState()
  val remainingSeconds by quizViewModel.remainingSeconds.collectAsState()
  val activeHint by quizViewModel.activeHint.collectAsState()
  val score by quizViewModel.score.collectAsState()
  val lastEarnedXp by quizViewModel.lastEarnedXp.collectAsState()
  val lastEarnedCoins by quizViewModel.lastEarnedCoins.collectAsState()
  val quizFinished by quizViewModel.quizFinished.collectAsState()

  val isLocked = isAnswerLocked || isAnswerSubmitted || isTimeExpired

  var showExitConfirmDialog by remember { mutableStateOf(false) }

  if (quizFinished) {
    onQuizFinished()
    return
  }

  val currentQuestion = quizViewModel.currentQuestion ?: return

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = if (isAr) currentQuestion.category.titleAr else currentQuestion.category.titleEn,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(
            onClick = {
              quizViewModel.pauseQuiz()
              showExitConfirmDialog = true
            },
            modifier = Modifier.testTag("quiz_exit_btn")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Exit Quiz"
            )
          }
        },
        actions = {
          // Pause Button
          IconButton(
            onClick = { quizViewModel.pauseQuiz() },
            modifier = Modifier.testTag("quiz_pause_btn")
          ) {
            Icon(
              imageVector = Icons.Filled.Pause,
              contentDescription = strings.pauseTitle,
              tint = EmeraldPrimary
            )
          }

          // Score chip
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = GoldAccent.copy(alpha = 0.15f),
            modifier = Modifier.padding(end = 12.dp)
          ) {
            Text(
              text = "$score ${strings.xp}",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = EmeraldDark,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .testTag("quiz_screen_body")
    ) {
      // 1. Progress Bar & Question Counter & Visual Timer Warning
      QuizHeaderMetrics(
        currentIndex = currentIndex,
        totalQuestions = questions.size,
        remainingSeconds = remainingSeconds,
        isTimeExpired = isTimeExpired,
        strings = strings
      )

      Spacer(modifier = Modifier.height(12.dp))

      // 2. Question Display Card
      QuestionDisplayCard(
        question = currentQuestion,
        isArabic = isAr,
        questionIndex = currentIndex + 1
      )

      Spacer(modifier = Modifier.height(16.dp))

      // 3. 4 Answer Options
      OptionsSection(
        question = currentQuestion,
        isArabic = isAr,
        selectedIndex = selectedOptionIndex,
        isSubmitted = isAnswerSubmitted,
        isLocked = isLocked,
        eliminatedIndices = eliminatedOptions,
        onSelectOption = { idx ->
          quizViewModel.selectOption(idx, soundEnabled, vibrationEnabled)
        }
      )

      Spacer(modifier = Modifier.height(16.dp))

      // 4. Lifelines Bar (Active if not submitted and not locked and not paused)
      if (!isLocked) {
        LifelinesBar(
          profile = profile,
          isArabic = isAr,
          strings = strings,
          onUseLifeline = { type ->
            quizViewModel.useLifeline(type, isAr)
          }
        )
      }

      // 5. Answer Interaction & Feedback (Short, calm animation with rewards or correction)
      AnimatedVisibility(
        visible = isAnswerSubmitted,
        enter = fadeIn(animationSpec = tween(220)) + slideInVertically(
          animationSpec = tween(220),
          initialOffsetY = { it / 3 }
        )
      ) {
        AnswerFeedbackSection(
          question = currentQuestion,
          isArabic = isAr,
          selectedIndex = selectedOptionIndex,
          isTimeExpired = isTimeExpired,
          isAutoAdvancing = isAutoAdvancing,
          earnedXp = lastEarnedXp,
          earnedCoins = lastEarnedCoins,
          isLastQuestion = currentIndex == questions.size - 1,
          strings = strings,
          onNext = { quizViewModel.nextQuestion() }
        )
      }

      Spacer(modifier = Modifier.height(32.dp))
    }
  }

  // Pause Overlay Dialog
  if (isPaused && !showExitConfirmDialog) {
    PauseOverlayDialog(
      strings = strings,
      onResume = { quizViewModel.resumeQuiz() },
      onRestart = { quizViewModel.restartRound() },
      onExitClick = { showExitConfirmDialog = true }
    )
  }

  // Exit Confirmation Dialog
  if (showExitConfirmDialog) {
    AlertDialog(
      onDismissRequest = {
        showExitConfirmDialog = false
        if (isPaused) {
          // Keep paused or allow resume
        }
      },
      title = {
        Text(
          text = strings.exitConfirmTitle,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(
          text = strings.exitConfirmDesc,
          fontSize = 14.sp,
          lineHeight = 20.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showExitConfirmDialog = false
            quizViewModel.pauseTimer()
            onExit()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
          modifier = Modifier.testTag("confirm_quiz_exit_btn")
        ) {
          Text(text = strings.exitQuiz, color = Color.White)
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showExitConfirmDialog = false
            quizViewModel.resumeQuiz()
          }
        ) {
          Text(text = strings.cancel)
        }
      }
    )
  }

  // Hint Alert Dialog
  if (activeHint != null) {
    AlertDialog(
      onDismissRequest = { quizViewModel.dismissHint() },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = EmeraldPrimary
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = strings.hintDialogTitle, fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Text(
          text = activeHint ?: strings.noHintAvailable,
          fontSize = 15.sp,
          lineHeight = 22.sp
        )
      },
      confirmButton = {
        Button(
          onClick = { quizViewModel.dismissHint() },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
          Text(text = strings.gotIt)
        }
      }
    )
  }
}

@Composable
private fun QuizHeaderMetrics(
  currentIndex: Int,
  totalQuestions: Int,
  remainingSeconds: Int,
  isTimeExpired: Boolean,
  strings: com.example.core.localization.AppStrings
) {
  val isWarning = remainingSeconds in 1..5

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val warningAlpha by infiniteTransition.animateFloat(
    initialValue = 0.5f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(400),
      repeatMode = RepeatMode.Reverse
    ),
    label = "warningAlpha"
  )

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "${strings.questionProgress} ${currentIndex + 1} / $totalQuestions",
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      if (isTimeExpired) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFFEE2E2)
        ) {
          Text(
            text = "⚠️ ${strings.timeUpAlert}",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFFB91C1C),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      } else if (remainingSeconds > 0) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (isWarning) Color(0xFFFEE2E2).copy(alpha = warningAlpha) else EmeraldPrimary.copy(alpha = 0.1f),
          border = if (isWarning) BorderStroke(1.dp, Color(0xFFB91C1C)) else null
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            if (isWarning) {
              Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = Color(0xFFB91C1C),
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
              text = "⏱ $remainingSeconds ${strings.timerSeconds}",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = if (isWarning) Color(0xFFB91C1C) else EmeraldPrimary
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    val progress = if (totalQuestions > 0) (currentIndex + 1).toFloat() / totalQuestions.toFloat() else 0f
    LinearProgressIndicator(
      progress = { progress },
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp)),
      color = EmeraldPrimary,
      trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
  }
}

@Composable
private fun QuestionDisplayCard(
  question: Question,
  isArabic: Boolean,
  questionIndex: Int
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp)
      .testTag("question_card")
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = EmeraldPrimary.copy(alpha = 0.12f),
        modifier = Modifier.padding(bottom = 10.dp)
      ) {
        Text(
          text = if (isArabic) "السؤال رقم $questionIndex" else "Question #$questionIndex",
          color = EmeraldPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }

      Text(
        text = question.getQuestion(isArabic),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        fontSize = 17.sp,
        lineHeight = 26.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Composable
private fun OptionsSection(
  question: Question,
  isArabic: Boolean,
  selectedIndex: Int?,
  isSubmitted: Boolean,
  isLocked: Boolean,
  eliminatedIndices: Set<Int>,
  onSelectOption: (Int) -> Unit
) {
  val options = question.getOptions(isArabic)

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    options.forEachIndexed { index, optionText ->
      val isEliminated = eliminatedIndices.contains(index)
      val isSelected = selectedIndex == index
      val isCorrect = index == question.correctAnswerIndex

      OptionItemCard(
        text = optionText,
        index = index,
        isSelected = isSelected,
        isSubmitted = isSubmitted,
        isLocked = isLocked,
        isCorrect = isCorrect,
        isEliminated = isEliminated,
        onSelect = { onSelectOption(index) }
      )
    }
  }
}

@Composable
private fun OptionItemCard(
  text: String,
  index: Int,
  isSelected: Boolean,
  isSubmitted: Boolean,
  isLocked: Boolean,
  isCorrect: Boolean,
  isEliminated: Boolean,
  onSelect: () -> Unit
) {
  val targetColor = when {
    isEliminated -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    isSubmitted && isCorrect -> Color(0xFFDCFCE7)
    isSubmitted && isSelected && !isCorrect -> Color(0xFFFEE2E2)
    isSelected -> EmeraldPrimary.copy(alpha = 0.14f)
    else -> MaterialTheme.colorScheme.surface
  }

  val borderColor by animateColorAsState(
    targetValue = when {
      isEliminated -> Color.Transparent
      isSubmitted && isCorrect -> Color(0xFF22C55E)
      isSubmitted && isSelected && !isCorrect -> Color(0xFFEF4444)
      isSelected -> EmeraldPrimary
      else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    },
    label = "optionBorderColor"
  )

  val optionLetter = when (index) {
    0 -> "أ"
    1 -> "ب"
    2 -> "ج"
    else -> "د"
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = targetColor),
    border = BorderStroke(1.5.dp, borderColor),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(enabled = !isLocked && !isSubmitted && !isEliminated) { onSelect() }
      .testTag("quiz_option_$index")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = CircleShape,
        color = when {
          isSubmitted && isCorrect -> Color(0xFF22C55E)
          isSubmitted && isSelected && !isCorrect -> Color(0xFFEF4444)
          isSelected -> EmeraldPrimary
          else -> MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier.size(32.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = optionLetter,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (isSelected || (isSubmitted && isCorrect)) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isEliminated) Color.Gray.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun LifelinesBar(
  profile: PlayerProfile,
  isArabic: Boolean,
  strings: com.example.core.localization.AppStrings,
  onUseLifeline: (LifelineType) -> Unit
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp)
      .testTag("lifelines_bar")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 10.dp, horizontal = 12.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      LifelineButtonItem(
        name = strings.lifeline5050,
        count = profile.lifelines5050,
        cost = "50 🪙",
        icon = LifelineType.FIFTY_FIFTY.icon,
        tag = "lifeline_5050",
        onClick = { onUseLifeline(LifelineType.FIFTY_FIFTY) }
      )
      LifelineButtonItem(
        name = strings.lifelineHint,
        count = profile.lifelinesHint,
        cost = "40 🪙",
        icon = LifelineType.HINT.icon,
        tag = "lifeline_hint",
        onClick = { onUseLifeline(LifelineType.HINT) }
      )
      LifelineButtonItem(
        name = strings.lifelineTime,
        count = profile.lifelinesTime,
        cost = "30 🪙",
        icon = LifelineType.EXTRA_TIME.icon,
        tag = "lifeline_time",
        onClick = { onUseLifeline(LifelineType.EXTRA_TIME) }
      )
      LifelineButtonItem(
        name = strings.lifelineSkip,
        count = profile.lifelinesSkip,
        cost = "60 🪙",
        icon = LifelineType.SKIP.icon,
        tag = "lifeline_skip",
        onClick = { onUseLifeline(LifelineType.SKIP) }
      )
    }
  }
}

@Composable
private fun LifelineButtonItem(
  name: String,
  count: Int,
  cost: String,
  icon: ImageVector,
  tag: String,
  onClick: () -> Unit
) {
  val isEnabled = count > 0

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clickable(enabled = isEnabled) { onClick() }
      .padding(4.dp)
      .testTag(tag)
  ) {
    Box(
      modifier = Modifier
        .size(42.dp)
        .clip(CircleShape)
        .background(
          if (isEnabled) EmeraldPrimary.copy(alpha = 0.12f)
          else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = name,
        tint = if (isEnabled) EmeraldPrimary else Color.Gray,
        modifier = Modifier.size(20.dp)
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "$name ($count)",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = if (isEnabled) MaterialTheme.colorScheme.onSurface else Color.Gray
    )
  }
}

@Composable
private fun AnswerFeedbackSection(
  question: Question,
  isArabic: Boolean,
  selectedIndex: Int?,
  isTimeExpired: Boolean,
  isAutoAdvancing: Boolean = false,
  earnedXp: Int,
  earnedCoins: Int,
  isLastQuestion: Boolean,
  strings: com.example.core.localization.AppStrings,
  onNext: () -> Unit
) {
  val isCorrect = selectedIndex == question.correctAnswerIndex && !isTimeExpired

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isCorrect) Color(0xFFF0FDF4) else Color(0xFFFFF1F2)
    ),
    border = BorderStroke(
      1.dp,
      if (isCorrect) Color(0xFF86EFAC) else Color(0xFFFECDD3)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp)
      .testTag("answer_feedback_card")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      // Header: Status + Earned Rewards
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (isCorrect) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null,
            tint = if (isCorrect) Color(0xFF15803D) else Color(0xFFBE123C),
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isTimeExpired) strings.timeUpAlert
                   else if (isCorrect) strings.correctAlert
                   else strings.incorrectAlert,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (isCorrect) Color(0xFF15803D) else Color(0xFFBE123C)
          )
        }

        // Earned Rewards Pill
        if (isCorrect && (earnedXp > 0 || earnedCoins > 0)) {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF15803D)
            ) {
              Text(
                text = "+$earnedXp XP",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFD97706)
            ) {
              Text(
                text = "+$earnedCoins 🪙",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }
      }

      // If wrong, show correct answer clearly
      if (!isCorrect) {
        Spacer(modifier = Modifier.height(10.dp))
        val correctOptionText = question.getOptions(isArabic).getOrNull(question.correctAnswerIndex) ?: ""
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFDCFCE7),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isArabic) "الإجابة الصحيحة:" else "Correct Answer:",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = Color(0xFF15803D)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = correctOptionText,
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
              color = Color(0xFF166534)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = question.getExplanation(isArabic),
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF1F2937),
        lineHeight = 22.sp
      )

      if (question.source.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "📖 ${strings.source}: ${question.source}",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFF4B5563)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = onNext,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isCorrect) EmeraldPrimary else Color(0xFFBE123C)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("quiz_next_action_btn")
      ) {
        Text(
          text = if (isLastQuestion) strings.finishQuiz else strings.nextQuestion,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      }

      if (isAutoAdvancing) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isArabic) "الانتقال التلقائي..." else "Auto-advancing...",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}

@Composable
private fun PauseOverlayDialog(
  strings: com.example.core.localization.AppStrings,
  onResume: () -> Unit,
  onRestart: () -> Unit,
  onExitClick: () -> Unit
) {
  Dialog(onDismissRequest = onResume) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("pause_overlay_card")
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(EmeraldPrimary.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.Pause,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = strings.pauseTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Resume Button
        Button(
          onClick = onResume,
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("pause_resume_btn")
        ) {
          Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = strings.resumeGame, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Restart Round Button
        OutlinedButton(
          onClick = onRestart,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("pause_restart_btn")
        ) {
          Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = strings.restartRound, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Exit Button
        TextButton(
          onClick = onExitClick,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("pause_exit_btn")
        ) {
          Text(
            text = strings.exitQuiz,
            color = Color(0xFFEF4444),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
          )
        }
      }
    }
  }
}
