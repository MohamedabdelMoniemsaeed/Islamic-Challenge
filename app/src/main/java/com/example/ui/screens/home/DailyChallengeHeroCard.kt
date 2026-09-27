package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.data.models.PlayerProfile
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.SuccessGreen

@Composable
fun DailyChallengeHeroCard(
  profile: PlayerProfile,
  isCompletedToday: Boolean,
  isArabic: Boolean,
  strings: AppStrings,
  onStartChallenge: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    modifier = modifier
      .fillMaxWidth()
      .border(
        width = 1.5.dp,
        color = if (isCompletedToday) SuccessGreen.copy(alpha = 0.5f) else GoldAccent.copy(alpha = 0.6f),
        shape = RoundedCornerShape(22.dp)
      )
      .testTag("daily_challenge_hero_card")
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      // Header: Icon + Title + Question Count badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(
                  colors = listOf(EmeraldPrimary, EmeraldDark)
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Filled.Today,
              contentDescription = null,
              tint = GoldLight,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = if (isArabic) "📅 تحدي اليوم" else "📅 Daily Challenge",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (isArabic) "١٠ أسئلة يومية متجددة" else "10 curated questions",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Streak badge
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = GoldContainer
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.Whatshot,
              contentDescription = null,
              tint = Color(0xFFD97706),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            val streakLabel = if (isArabic) "سلسلة: ${profile.currentStreak} أيام" else "Streak: ${profile.currentStreak}d"
            Text(
              text = streakLabel,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF78350F)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Reward Banner
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = EmeraldPrimary.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = if (isArabic) "مكافأة إكمال التحدي:" else "Completion Reward:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = EmeraldDark
            ) {
              Text(
                text = "+50 XP",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldLight,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFD97706)
            ) {
              Text(
                text = "+25 🪙",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // State handling: If completed vs If pending
      if (isCompletedToday) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = SuccessGreen.copy(alpha = 0.12f),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_challenge_completed_tag")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = null,
              tint = SuccessGreen,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isArabic) "✓ تم إكمال تحدي اليوم بنجاح! ننتظرك غداً" else "✓ Today's challenge completed! See you tomorrow",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = SuccessGreen
            )
          }
        }
      } else {
        Button(
          onClick = onStartChallenge,
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("daily_challenge_start_btn")
        ) {
          Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = GoldLight
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isArabic) "ابدأ التحدي اليومي" else "Start Daily Challenge",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }
  }
}
