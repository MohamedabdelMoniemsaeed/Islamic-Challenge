package com.example.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.data.models.PlayerProfile
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.IslamicGeometricBackground

@Composable
fun PlayerProgressCard(
  profile: PlayerProfile,
  isArabic: Boolean,
  strings: AppStrings,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = EmeraldDark),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(22.dp))
      .clickable { onClick() }
      .testTag("player_progress_card")
  ) {
    Box(modifier = Modifier.fillMaxWidth()) {
      IslamicGeometricBackground(
        patternColor = GoldAccent.copy(alpha = 0.09f),
        strokeWidth = 1f
      )

      Column(modifier = Modifier.padding(20.dp)) {
        // Top row: Level title + XP Pill
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "${strings.currentLevel} ${profile.level}",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = GoldAccent
            )
            Text(
              text = profile.getTitle(isArabic),
              fontSize = 14.sp,
              color = Color.White.copy(alpha = 0.92f),
              fontWeight = FontWeight.Medium
            )
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = GoldAccent.copy(alpha = 0.22f)
          ) {
            Text(
              text = "${profile.xp} ${strings.xp}",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = GoldLight,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar
        LinearProgressIndicator(
          progress = { profile.levelProgressFraction },
          modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp)),
          color = GoldAccent,
          trackColor = Color.White.copy(alpha = 0.18f),
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom row: Current tier progress and remaining XP for next level
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${profile.xp} / ${profile.xpForNextLevelCeiling} XP",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.75f)
          )

          val remainingText = if (isArabic) {
            "${profile.xpRemainingForNextLevel} XP للمستوى التالي"
          } else {
            "${profile.xpRemainingForNextLevel} XP to next level"
          }

          Text(
            text = remainingText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = GoldLight
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Accuracy & Total answered
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = if (isArabic) "دقة الإجابات: ${profile.accuracyPercentage}%" else "Accuracy: ${profile.accuracyPercentage}%",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.65f)
          )

          Text(
            text = if (isArabic) "الأسئلة: ${profile.totalQuestions}" else "Questions: ${profile.totalQuestions}",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.65f)
          )
        }
      }
    }
  }
}
