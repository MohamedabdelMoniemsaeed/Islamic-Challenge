package com.example.ui.screens.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.data.models.GameMode
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.NavyDark

@Composable
fun GameModesSection(
  isArabic: Boolean,
  strings: AppStrings,
  onStartQuickChallenge: () -> Unit,
  onNavigateToCategories: () -> Unit,
  onStartPracticeMode: () -> Unit,
  onStartTimeChallenge: () -> Unit,
  onStartSurvivalMode: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = if (isArabic) "🎮 أوضاع اللعب" else "🎮 Game Modes",
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onBackground,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = 12.dp)
    )

    // Primary Two Modes: Quick Challenge & Category Challenge
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // 1. Quick Challenge Card
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldDark),
        modifier = Modifier
          .weight(1f)
          .height(180.dp)
          .testTag("quick_challenge_card")
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(GoldAccent.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.Bolt,
                contentDescription = null,
                tint = GoldLight,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = strings.quickChallenge,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (isArabic) "١٠ أسئلة • ٢٠ ثانية" else "10 questions • 20s",
              color = Color.White.copy(alpha = 0.8f),
              fontSize = 11.sp
            )
          }

          Button(
            onClick = onStartQuickChallenge,
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(38.dp)
              .testTag("quick_challenge_start_btn")
          ) {
            Text(
              text = if (isArabic) "ابدأ" else "Play",
              color = NavyDark,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // 2. Category Challenge Card
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
          .weight(1f)
          .height(180.dp)
          .testTag("category_challenge_card")
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(EmeraldPrimary.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.Category,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = strings.categoryChallenge,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (isArabic) "اختر مجالك وتعمّق" else "Choose category",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )
          }

          OutlinedButton(
            onClick = onNavigateToCategories,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(38.dp)
              .testTag("category_challenge_choose_btn")
          ) {
            Text(
              text = if (isArabic) "اختر القسم" else "Choose",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Secondary modes row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      ModeMiniTile(
        title = strings.practiceMode,
        subtitle = if (isArabic) "بلا وقت" else "Untimed",
        icon = Icons.AutoMirrored.Filled.MenuBook,
        testTag = "practice_mode_card",
        modifier = Modifier.weight(1f),
        onClick = onStartPracticeMode
      )

      ModeMiniTile(
        title = strings.timeChallenge,
        subtitle = if (isArabic) "٦٠ ثانية" else "60s rapid",
        icon = Icons.Filled.HourglassBottom,
        testTag = "time_challenge_card",
        modifier = Modifier.weight(1f),
        onClick = onStartTimeChallenge
      )

      ModeMiniTile(
        title = strings.survivalMode,
        subtitle = if (isArabic) "فرصة واحدة" else "1 life",
        icon = Icons.Filled.Shield,
        testTag = "survival_mode_card",
        modifier = Modifier.weight(1f),
        onClick = onStartSurvivalMode
      )
    }
  }
}

@Composable
private fun ModeMiniTile(
  title: String,
  subtitle: String,
  icon: ImageVector,
  testTag: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    modifier = modifier
      .height(80.dp)
      .clip(RoundedCornerShape(14.dp))
      .clickable { onClick() }
      .testTag(testTag)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = EmeraldPrimary,
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1
      )
      Text(
        text = subtitle,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
      )
    }
  }
}
