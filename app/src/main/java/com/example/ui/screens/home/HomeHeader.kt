package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldContainer

@Composable
fun HomeHeader(
  profile: PlayerProfile,
  isArabic: Boolean,
  strings: AppStrings,
  onProfileClick: () -> Unit,
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left side: Avatar & Greeting with player title
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .clickable { onProfileClick() }
          .padding(4.dp)
          .testTag("home_profile_avatar_btn")
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(EmeraldDark),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "🕌",
            fontSize = 22.sp
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Text(
            text = if (isArabic) "السلام عليكم 👋" else "Peace be upon you 👋",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = profile.getTitle(isArabic),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
          )
        }
      }

      // Right side: Streak chip, Coins chip & Settings icon
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Streak Chip
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = GoldContainer,
          modifier = Modifier
            .clickable { onProfileClick() }
            .testTag("home_streak_chip")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.Whatshot,
              contentDescription = "Streak",
              tint = Color(0xFFD97706),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${profile.currentStreak}",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF78350F)
            )
          }
        }

        // Coins Chip
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = EmeraldPrimary.copy(alpha = 0.12f),
          modifier = Modifier
            .clickable { onProfileClick() }
            .testTag("home_coins_chip")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
          ) {
            Text(
              text = "🪙",
              fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${profile.coins}",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary
            )
          }
        }

        // Settings Button (Min touch target 48dp)
        IconButton(
          onClick = onSettingsClick,
          modifier = Modifier
            .size(48.dp)
            .testTag("home_settings_btn")
        ) {
          Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = strings.settingsTitle,
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }
}
