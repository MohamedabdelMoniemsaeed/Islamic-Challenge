package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.models.Achievement
import com.example.data.models.AchievementsProvider
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SuccessGreen

@Composable
fun AchievementsPreviewSection(
  unlockedIds: Set<String>,
  isArabic: Boolean,
  strings: AppStrings,
  onViewAllAchievements: () -> Unit,
  modifier: Modifier = Modifier
) {
  val all = AchievementsProvider.allAchievements
  val totalCount = all.size
  val unlockedCount = unlockedIds.size

  // Show first 3 achievements
  val previewAchievements = all.take(3)

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("achievements_preview_card")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "🏆",
            fontSize = 20.sp
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isArabic) "الإنجازات والأوسمة" else "Achievements",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = EmeraldPrimary.copy(alpha = 0.12f)
        ) {
          Text(
            text = "$unlockedCount / $totalCount",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (ach in previewAchievements) {
          val isUnlocked = unlockedIds.contains(ach.id)
          AchievementRowItem(
            achievement = ach,
            isUnlocked = isUnlocked,
            isArabic = isArabic
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      TextButton(
        onClick = onViewAllAchievements,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("view_all_achievements_btn")
      ) {
        val label = if (isArabic) {
          "عرض كل الإنجازات والأوسمة ($unlockedCount/$totalCount) ←"
        } else {
          "View All Achievements ($unlockedCount/$totalCount) →"
        }
        Text(
          text = label,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = EmeraldPrimary
        )
      }
    }
  }
}

@Composable
private fun AchievementRowItem(
  achievement: Achievement,
  isUnlocked: Boolean,
  isArabic: Boolean
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isUnlocked) EmeraldPrimary.copy(alpha = 0.07f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(GoldAccent.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Text(text = achievement.iconEmoji, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Text(
            text = if (isArabic) achievement.titleAr else achievement.titleEn,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
          )
          Text(
            text = "+${achievement.xpReward} XP  •  +${achievement.coinReward} 🪙",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      if (isUnlocked) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = SuccessGreen.copy(alpha = 0.15f)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.Check,
              contentDescription = null,
              tint = SuccessGreen,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isArabic) "مكتمل" else "Unlocked",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = SuccessGreen
            )
          }
        }
      } else {
        Icon(
          imageVector = Icons.Filled.Lock,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}
