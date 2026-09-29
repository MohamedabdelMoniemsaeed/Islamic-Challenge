package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizationManager
import com.example.data.models.DailyRewardDay
import com.example.data.models.PlayerProfile
import com.example.data.models.defaultDailyRewards
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.IslamicGeometricBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRewardScreen(
  profile: PlayerProfile,
  language: AppLanguage,
  canClaim: Boolean,
  onClaim: () -> Unit,
  onBack: () -> Unit
) {
  val strings = LocalizationManager.get(language)
  val isAr = language.isRtl
  val currentCycleDay = profile.dailyRewardStreakDay // 0 to 7

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = strings.dailyRewardTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("daily_reward_back_btn")) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        windowInsets = TopAppBarDefaults.windowInsets
      )
    },
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.navigationBars
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("daily_reward_screen_body")
    ) {
      // Streak Info Header
      StreakBanner(
        currentStreak = profile.currentStreak,
        longestStreak = profile.longestStreak,
        strings = strings
      )

      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(defaultDailyRewards) { reward ->
          val isClaimed = if (canClaim) {
            reward.dayNumber <= currentCycleDay
          } else {
            reward.dayNumber <= currentCycleDay
          }
          val isCurrentClaimable = canClaim && (reward.dayNumber == (currentCycleDay % 7) + 1)
          val isMegaDay7 = reward.dayNumber == 7

          DailyRewardGridItem(
            reward = reward,
            isArabic = isAr,
            isClaimed = isClaimed && !isCurrentClaimable,
            isCurrentClaimable = isCurrentClaimable,
            isMegaDay7 = isMegaDay7,
            strings = strings
          )
        }
      }

      // Claim Action Bar
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          if (canClaim) {
            Button(
              onClick = onClaim,
              colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = EmeraldDark),
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("claim_daily_reward_btn")
            ) {
              Text(
                text = "🎁 ${strings.claimReward}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
              )
            }
          } else {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = EmeraldPrimary.copy(alpha = 0.1f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Filled.CheckCircle,
                  contentDescription = null,
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isAr) "تم استلام مكافأة اليوم! عُد غداً لمواصلة الحماس" else "Today's reward claimed! Return tomorrow for the next reward.",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium,
                  color = EmeraldPrimary,
                  textAlign = TextAlign.Center
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StreakBanner(
  currentStreak: Int,
  longestStreak: Int,
  strings: com.example.core.localization.AppStrings
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = EmeraldDark),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 8.dp)
  ) {
    Box(modifier = Modifier.fillMaxWidth()) {
      IslamicGeometricBackground(
        patternColor = GoldAccent.copy(alpha = 0.08f),
        strokeWidth = 1f
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(50.dp)
              .clip(CircleShape)
              .background(GoldAccent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Filled.Whatshot,
              contentDescription = "Streak",
              tint = GoldAccent,
              modifier = Modifier.size(28.dp)
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column {
            Text(
              text = "$currentStreak ${strings.streakDays}",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = "${strings.longestStreak}: $longestStreak",
              fontSize = 12.sp,
              color = GoldLight.copy(alpha = 0.85f)
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = GoldContainer
        ) {
          Text(
            text = "Active 🔥",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFF78350F),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun DailyRewardGridItem(
  reward: DailyRewardDay,
  isArabic: Boolean,
  isClaimed: Boolean,
  isCurrentClaimable: Boolean,
  isMegaDay7: Boolean,
  strings: com.example.core.localization.AppStrings
) {
  val borderColor = when {
    isCurrentClaimable -> GoldAccent
    isClaimed -> EmeraldPrimary
    else -> Color.Transparent
  }

  val containerColor = when {
    isCurrentClaimable -> GoldContainer
    isClaimed -> EmeraldPrimary.copy(alpha = 0.08f)
    isMegaDay7 -> EmeraldDark.copy(alpha = 0.06f)
    else -> MaterialTheme.colorScheme.surface
  }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = BorderStroke(if (isCurrentClaimable) 2.dp else 1.dp, borderColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .height(130.dp)
      .testTag("reward_day_${reward.dayNumber}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isArabic) reward.titleAr else reward.titleEn,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        if (isClaimed) {
          Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Claimed",
            tint = EmeraldPrimary,
            modifier = Modifier.size(16.dp)
          )
        } else if (!isCurrentClaimable) {
          Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = "Locked",
            tint = Color.Gray,
            modifier = Modifier.size(14.dp)
          )
        }
      }

      Text(
        text = reward.iconEmoji,
        fontSize = 28.sp
      )

      Text(
        text = if (isArabic) reward.rewardTextAr else reward.rewardTextEn,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        color = if (isCurrentClaimable) Color(0xFF78350F) else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
