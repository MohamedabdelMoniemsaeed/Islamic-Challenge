package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizationManager
import com.example.data.models.GameMode
import com.example.data.models.PlayerProfile
import com.example.data.models.QuizCategory
import com.example.ui.screens.home.AchievementsPreviewSection
import com.example.ui.screens.home.CategoriesSection
import com.example.ui.screens.home.DailyChallengeHeroCard
import com.example.ui.screens.home.GameModesSection
import com.example.ui.screens.home.HomeHeader
import com.example.ui.screens.home.PlayerProgressCard
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight

@Composable
fun HomeScreen(
  profile: PlayerProfile,
  language: AppLanguage,
  canClaimReward: Boolean,
  isDailyChallengeCompleted: Boolean,
  unlockedAchievements: Set<String>,
  onStartMode: (GameMode, QuizCategory?) -> Unit,
  onNavigateToCategories: () -> Unit,
  onNavigateToAchievements: () -> Unit,
  onNavigateToDailyReward: () -> Unit,
  onNavigateToProfileStats: () -> Unit,
  onNavigateToSettings: () -> Unit
) {
  val strings = LocalizationManager.get(language)
  val isAr = language.isRtl

  Scaffold(
    topBar = {
      HomeHeader(
        profile = profile,
        isArabic = isAr,
        strings = strings,
        onProfileClick = onNavigateToProfileStats,
        onSettingsClick = onNavigateToSettings
      )
    },
    containerColor = MaterialTheme.colorScheme.background
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("home_screen_content"),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      // 1. Player Status & Level Progression Card
      item {
        PlayerProgressCard(
          profile = profile,
          isArabic = isAr,
          strings = strings,
          onClick = onNavigateToProfileStats
        )
      }

      // 2. Daily Reward Ready Banner (if eligible for claim)
      if (canClaimReward) {
        item {
          DailyRewardReadyBanner(
            strings = strings,
            onClick = onNavigateToDailyReward
          )
        }
      }

      // 3. Hero Banner with Islamic Art & Bismillah
      item {
        HomeHeroBanner(
          strings = strings,
          onQuickStart = { onStartMode(GameMode.QUICK_CHALLENGE, null) }
        )
      }

      // 4. Daily Challenge Hero Card
      item {
        DailyChallengeHeroCard(
          profile = profile,
          isCompletedToday = isDailyChallengeCompleted,
          isArabic = isAr,
          strings = strings,
          onStartChallenge = { onStartMode(GameMode.DAILY_CHALLENGE, null) }
        )
      }

      // 5. Game Modes Hub (Quick Challenge, Category Challenge, Practice, Time, Survival)
      item {
        GameModesSection(
          isArabic = isAr,
          strings = strings,
          onStartQuickChallenge = { onStartMode(GameMode.QUICK_CHALLENGE, null) },
          onNavigateToCategories = onNavigateToCategories,
          onStartPracticeMode = { onStartMode(GameMode.PRACTICE_MODE, null) },
          onStartTimeChallenge = { onStartMode(GameMode.TIME_CHALLENGE, null) },
          onStartSurvivalMode = { onStartMode(GameMode.SURVIVAL_MODE, null) }
        )
      }

      // 6. Categories (Quran, Seerah, Prophets, Worship, Ramadan, Adhkar, Manners, General)
      item {
        CategoriesSection(
          isArabic = isAr,
          strings = strings,
          onCategorySelected = { category ->
            onStartMode(GameMode.CATEGORY_CHALLENGE, category)
          },
          onViewAllCategories = onNavigateToCategories
        )
      }

      // 7. Achievements Preview Section
      item {
        AchievementsPreviewSection(
          unlockedIds = unlockedAchievements,
          isArabic = isAr,
          strings = strings,
          onViewAllAchievements = onNavigateToAchievements
        )
      }

      item {
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun HomeHeroBanner(
  strings: com.example.core.localization.AppStrings,
  onQuickStart: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    modifier = Modifier
      .fillMaxWidth()
      .height(130.dp)
      .clickable { onQuickStart() }
      .testTag("home_hero_banner")
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Image(
        painter = painterResource(id = R.drawable.img_islamic_hero),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )

      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.horizontalGradient(
              colors = listOf(
                EmeraldDeep.copy(alpha = 0.94f),
                EmeraldDark.copy(alpha = 0.70f),
                Color.Transparent
              )
            )
          )
      )

      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(18.dp),
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = "🌙 ${strings.appTitle}",
          color = GoldLight,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = strings.appSubtitle,
          color = Color.White.copy(alpha = 0.9f),
          fontSize = 12.sp,
          modifier = Modifier.fillMaxWidth(0.85f)
        )
      }
    }
  }
}

@Composable
private fun DailyRewardReadyBanner(
  strings: com.example.core.localization.AppStrings,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = GoldContainer,
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("daily_reward_claim_banner")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "🎁", fontSize = 24.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = strings.dailyRewardAvailable,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF78350F),
            fontSize = 15.sp
          )
          Text(
            text = strings.claimReward,
            color = Color(0xFF92400E),
            fontSize = 12.sp
          )
        }
      }
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFD97706)
      ) {
        Text(
          text = strings.claimReward,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
      }
    }
  }
}
