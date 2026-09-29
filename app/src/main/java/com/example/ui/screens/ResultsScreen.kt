package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizationManager
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.IslamicGeometricBackground
import com.example.ui.viewmodels.QuizResultState

@Composable
fun ResultsScreen(
  result: QuizResultState,
  language: AppLanguage,
  onPlayAgain: () -> Unit,
  onReturnHome: () -> Unit,
  onChooseCategory: () -> Unit
) {
  val strings = LocalizationManager.get(language)
  val isAr = language.isRtl

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    IslamicGeometricBackground(
      patternColor = EmeraldPrimary.copy(alpha = 0.05f),
      strokeWidth = 1f
    )

    // Solid status bar background matching header/surface color
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .windowInsetsTopHeight(WindowInsets.statusBars)
        .background(MaterialTheme.colorScheme.surface)
        .align(Alignment.TopCenter)
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 20.dp)
        .verticalScroll(rememberScrollState())
        .testTag("results_screen_body"),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // Celebration Trophy / Emblem
      Box(
        modifier = Modifier
          .size(130.dp)
          .clip(CircleShape)
          .background(GoldAccent.copy(alpha = 0.15f))
          .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_reward_trophy),
          contentDescription = "Reward Trophy",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = strings.challengeComplete,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = EmeraldDark,
        textAlign = TextAlign.Center
      )

      // Level Up Card (if occurred)
      if (result.didLevelUp) {
        Spacer(modifier = Modifier.height(14.dp))
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = GoldContainer),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("results_level_up_card")
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "🎉 ${strings.levelUpTitle}!",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF78350F)
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "${strings.currentLevel} ${result.newLevel}",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 20.sp,
              color = EmeraldDark
            )
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
              progress = { 0.25f },
              color = EmeraldPrimary,
              trackColor = GoldAccent.copy(alpha = 0.3f),
              modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Main Stats Card
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          // Accuracy Banner
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = strings.accuracy,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${result.accuracyPercentage}%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              )
            }

            Surface(
              shape = RoundedCornerShape(14.dp),
              color = EmeraldPrimary.copy(alpha = 0.12f)
            ) {
              Text(
                text = "${result.finalScore} ${strings.questionScore}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Rewards summary (XP & Coins)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            MetricPill(
              label = strings.totalXpGained,
              value = "+${result.xpGained} XP",
              color = EmeraldPrimary,
              modifier = Modifier.weight(1f)
            )
            MetricPill(
              label = strings.totalCoinsGained,
              value = "+${result.coinsGained} 🪙",
              color = Color(0xFFD97706),
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Answers breakdown (Correct vs Wrong)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            BreakdownPill(
              label = strings.correctAnswers,
              count = result.correctCount,
              icon = Icons.Filled.Check,
              color = Color(0xFF10B981),
              modifier = Modifier.weight(1f)
            )
            BreakdownPill(
              label = strings.wrongAnswers,
              count = result.wrongCount,
              icon = Icons.Filled.Close,
              color = Color(0xFFEF4444),
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Action Buttons
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Play Again Button
        Button(
          onClick = onPlayAgain,
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("results_play_again_btn")
        ) {
          Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = strings.playAgain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        // Choose Category Button
        OutlinedButton(
          onClick = onChooseCategory,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("results_choose_category_btn")
        ) {
          Icon(imageVector = Icons.Filled.Category, contentDescription = null, tint = EmeraldPrimary)
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = strings.chooseCategory, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = EmeraldDark)
        }

        // Return Home Button
        OutlinedButton(
          onClick = onReturnHome,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("results_home_btn")
        ) {
          Icon(imageVector = Icons.Filled.Home, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = strings.returnHome, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun MetricPill(
  label: String,
  value: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = color.copy(alpha = 0.1f),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = label,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = color
      )
    }
  }
}

@Composable
private fun BreakdownPill(
  label: String,
  count: Int,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = color.copy(alpha = 0.1f),
    modifier = modifier
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "$label: $count",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = color
      )
    }
  }
}
