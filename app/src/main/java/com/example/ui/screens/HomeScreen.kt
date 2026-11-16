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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.adhan.PrayerTimeItem
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizationManager
import com.example.data.models.GameMode
import com.example.data.models.LifelineType
import com.example.data.models.PlayerProfile
import com.example.data.models.QuizCategory
import com.example.ui.components.LifelineStoreDialog
import com.example.ui.screens.home.AchievementsPreviewSection
import com.example.ui.screens.home.CategoriesSection
import com.example.ui.screens.home.DailyChallengeHeroCard
import com.example.ui.screens.home.GameModesSection
import com.example.ui.screens.home.HomeHeader
import com.example.ui.screens.home.PlayerProgressCard
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight

@Composable
fun HomeScreen(
  profile: PlayerProfile,
  language: AppLanguage,
  canClaimReward: Boolean,
  showInAppBanner: Boolean = false,
  isDailyChallengeCompleted: Boolean,
  unlockedAchievements: Set<String>,
  nextPrayer: PrayerTimeItem? = null,
  prayerCountdown: String = "",
  hijriDate: String = "",
  cityName: String = "",
  onStartMode: (GameMode, QuizCategory?) -> Unit,
  onNavigateToCategories: () -> Unit,
  onNavigateToAchievements: () -> Unit,
  onNavigateToDailyReward: () -> Unit,
  onNavigateToProfileStats: () -> Unit,
  onNavigateToSettings: () -> Unit,
  onNavigateToPrayerTimes: () -> Unit = {},
  onNavigateToAdhkarTasbih: () -> Unit = {},
  onPurchaseLifeline: (LifelineType) -> Unit = {},
  onDismissInAppBanner: () -> Unit = {}
) {
  val strings = LocalizationManager.get(language)
  val isAr = language.isRtl
  var showLifelineStore by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      HomeHeader(
        profile = profile,
        isArabic = isAr,
        strings = strings,
        onProfileClick = onNavigateToProfileStats,
        onSettingsClick = onNavigateToSettings,
        onCoinsClick = { showLifelineStore = true }
      )
    },
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets(0, 0, 0, 0)
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentAlignment = Alignment.TopCenter
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = 640.dp)
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

        // 2. Live Next Prayer & Hijri Date Banner
        item {
          PrayerTimesHomeBanner(
            isArabic = isAr,
            nextPrayer = nextPrayer,
            countdown = prayerCountdown,
            hijriDate = hijriDate,
            cityName = cityName,
            onClick = onNavigateToPrayerTimes
          )
        }

        // 3. Quick Companion Row: Adhkar & Tasbih + Lifelines Store
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            HomeQuickCompanionTile(
              title = if (isAr) "الأذكار والمسبحة" else "Adhkar & Tasbih",
              subtitle = if (isAr) "الصباح والمساء والتسبيح" else "Morning, Evening & Dhikr",
              icon = Icons.Filled.AutoAwesome,
              testTag = "home_adhkar_tasbih_btn",
              modifier = Modifier.weight(1f),
              onClick = onNavigateToAdhkarTasbih
            )
            HomeQuickCompanionTile(
              title = if (isAr) "متجر المساعدات" else "Lifelines Store",
              subtitle = if (isAr) "رصيدك: ${profile.coins} 🪙" else "Balance: ${profile.coins} 🪙",
              icon = Icons.Filled.ShoppingBag,
              testTag = "home_lifeline_store_btn",
              modifier = Modifier.weight(1f),
              onClick = { showLifelineStore = true }
            )
          }
        }

        // 4. Daily Reward Ready Banner (Only shown if user chose to show it inside app)
        if (canClaimReward && showInAppBanner) {
          item {
            DailyRewardReadyBanner(
              strings = strings,
              onClick = onNavigateToDailyReward,
              onDismiss = onDismissInAppBanner
            )
          }
        }

        // 5. Hero Banner with Islamic Art & Bismillah
        item {
          HomeHeroBanner(
            strings = strings,
            onQuickStart = { onStartMode(GameMode.QUICK_CHALLENGE, null) }
          )
        }

        // 6. Daily Challenge Hero Card
        item {
          DailyChallengeHeroCard(
            profile = profile,
            isCompletedToday = isDailyChallengeCompleted,
            isArabic = isAr,
            strings = strings,
            onStartChallenge = { onStartMode(GameMode.DAILY_CHALLENGE, null) }
          )
        }

        // 7. Game Modes Hub (Quick, Category, Practice, Time, Survival, Level Mode)
        item {
          GameModesSection(
            isArabic = isAr,
            strings = strings,
            onStartQuickChallenge = { onStartMode(GameMode.QUICK_CHALLENGE, null) },
            onNavigateToCategories = onNavigateToCategories,
            onStartPracticeMode = { onStartMode(GameMode.PRACTICE_MODE, null) },
            onStartTimeChallenge = { onStartMode(GameMode.TIME_CHALLENGE, null) },
            onStartSurvivalMode = { onStartMode(GameMode.SURVIVAL_MODE, null) },
            onStartLevelMode = { onStartMode(GameMode.LEVEL_MODE, null) }
          )
        }

        // 8. Categories (Quran, Seerah, Prophets, Worship, Ramadan, Adhkar, Manners, General)
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

        // 9. Achievements Preview Section
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

  if (showLifelineStore) {
    LifelineStoreDialog(
      profile = profile,
      isArabic = isAr,
      onPurchase = onPurchaseLifeline,
      onDismiss = { showLifelineStore = false }
    )
  }
}

@Composable
private fun HomeQuickCompanionTile(
  title: String,
  subtitle: String,
  icon: ImageVector,
  testTag: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag(testTag)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = CircleShape,
        color = EmeraldPrimary.copy(alpha = 0.12f),
        modifier = Modifier.size(38.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(20.dp)
          )
        }
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
  onClick: () -> Unit,
  onDismiss: () -> Unit
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
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
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

      Row(verticalAlignment = Alignment.CenterVertically) {
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

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(32.dp)
            .testTag("dismiss_in_app_banner_btn")
        ) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Dismiss",
            tint = Color(0xFF78350F),
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
fun PrayerTimesHomeBanner(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isArabic: Boolean = true,
  nextPrayer: PrayerTimeItem? = null,
  countdown: String = "",
  hijriDate: String = "",
  cityName: String = ""
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = EmeraldDeep
    ),
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("prayer_times_home_banner")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Surface(
          shape = CircleShape,
          color = GoldAccent.copy(alpha = 0.2f),
          modifier = Modifier.size(44.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.AccessTime,
              contentDescription = null,
              tint = GoldLight,
              modifier = Modifier.size(22.dp)
            )
          }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          val heading = if (nextPrayer != null) {
            if (isArabic) {
              "القادمة: ${nextPrayer.prayer.arName} (${nextPrayer.getTimeString(true)})"
            } else {
              "Next: ${nextPrayer.prayer.enName} (${nextPrayer.getTimeString(false)})"
            }
          } else {
            if (isArabic) "مواقيت الصلاة والأذان والقبلة 🕌" else "Prayer Times, Adhan & Qiblah 🕌"
          }
          Text(
            text = heading,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          val subText = buildString {
            if (countdown.isNotBlank()) {
              append(if (isArabic) "متبقي $countdown" else "In $countdown")
            }
            if (cityName.isNotBlank()) {
              if (isNotEmpty()) append(" • ")
              append(cityName)
            }
            if (hijriDate.isNotBlank() && isEmpty()) {
              append(hijriDate)
            }
            if (isEmpty()) {
              append(if (isArabic) "المواقيت، الأذان واتجاه القبلة" else "Schedule, Adhan audio & Qiblah")
            }
          }
          Text(
            text = subText,
            style = MaterialTheme.typography.bodySmall,
            color = GoldLight.copy(alpha = 0.9f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = GoldAccent
      ) {
        Text(
          text = if (isArabic) "فتح" else "Open",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = EmeraldDeep,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
      }
    }
  }
}
