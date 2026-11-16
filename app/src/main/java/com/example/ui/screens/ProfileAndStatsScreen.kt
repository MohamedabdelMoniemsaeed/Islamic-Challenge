package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizationManager
import com.example.data.local.CategoryProgressEntity
import com.example.data.models.Achievement
import com.example.data.models.LifelineType
import com.example.data.models.PlayerProfile
import com.example.data.models.QuizCategory
import com.example.ui.components.LifelineStoreDialog
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.IslamicGeometricBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAndStatsScreen(
  profile: PlayerProfile,
  achievements: List<Achievement>,
  language: AppLanguage,
  categoryProgress: Map<String, CategoryProgressEntity> = emptyMap(),
  onPurchaseLifeline: (LifelineType) -> Unit = {},
  onBack: (() -> Unit)? = null
) {
  val strings = LocalizationManager.get(language)
  val isAr = language.isRtl
  var showStoreDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = strings.profileTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          if (onBack != null) {
            IconButton(
              onClick = onBack,
              modifier = Modifier
                .size(48.dp)
                .testTag("profile_back_btn")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = if (isAr) "رجوع" else "Back"
              )
            }
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        windowInsets = TopAppBarDefaults.windowInsets
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
          .widthIn(max = 620.dp)
          .testTag("profile_screen_body"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // 1. Profile Header Card
        item {
          ProfileHeaderCard(
            profile = profile,
            isArabic = isAr,
            strings = strings
          )
        }

        // 2. Lifelines Inventory & Store Card
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (isAr) "وسائل المساعدة المتوفرة" else "Available Lifelines",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = GoldContainer
                ) {
                  Text(
                    text = "${profile.coins} 🪙",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF78350F),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                LifelineCountBadge("50/50", profile.lifelines5050, Modifier.weight(1f))
                LifelineCountBadge(if (isAr) "تلميح" else "Hint", profile.lifelinesHint, Modifier.weight(1f))
                LifelineCountBadge("+15s", profile.lifelinesTime, Modifier.weight(1f))
                LifelineCountBadge(if (isAr) "تخطي" else "Skip", profile.lifelinesSkip, Modifier.weight(1f))
              }

              Spacer(modifier = Modifier.height(12.dp))

              Button(
                onClick = { showStoreDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("profile_open_store_btn")
              ) {
                Icon(imageVector = Icons.Filled.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isAr) "شراء وسائل مساعدة بالنقاط الذهبية" else "Get Extra Lifelines with Coins",
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        // 3. Performance Metrics Grid
        item {
          Text(
            text = strings.statisticsTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            StatTile(
              label = strings.totalGames,
              value = "${profile.gamesPlayed}",
              icon = "🎮",
              modifier = Modifier.weight(1f)
            )
            StatTile(
              label = strings.totalQuestions,
              value = "${profile.totalQuestions}",
              icon = "❓",
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            StatTile(
              label = strings.accuracy,
              value = "${profile.accuracyPercentage}%",
              icon = "🎯",
              modifier = Modifier.weight(1f)
            )
            StatTile(
              label = strings.longestStreak,
              value = "${profile.longestStreak} ${strings.streakDays}",
              icon = "🔥",
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            StatTile(
              label = strings.correctAnswers,
              value = "${profile.correctAnswers}",
              icon = "✅",
              modifier = Modifier.weight(1f)
            )
            StatTile(
              label = strings.wrongAnswers,
              value = "${profile.incorrectAnswers}",
              icon = "❌",
              modifier = Modifier.weight(1f)
            )
          }
        }

        // 4. Category Mastery Progress
        item {
          Text(
            text = if (isAr) "إتقان أقسام المعرفة" else "Category Mastery",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
        }

        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              QuizCategory.entries.forEach { category ->
                val prog = categoryProgress[category.id]
                val answered = prog?.questionsAnswered ?: 0
                val correct = prog?.correctCount ?: 0
                val accuracy = if (answered > 0) ((correct * 100f) / answered).toInt() else 0
                val masteryFraction = (answered.coerceAtMost(18).toFloat() / 18f)

                Column(modifier = Modifier.fillMaxWidth()) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = if (isAr) category.titleAr else category.titleEn,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = if (answered > 0) "$correct/$answered ($accuracy%)" else if (isAr) "لم يبدأ بعد" else "Not started",
                      style = MaterialTheme.typography.labelSmall,
                      color = if (answered > 0) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                      fontWeight = FontWeight.Bold
                    )
                  }
                  Spacer(modifier = Modifier.height(6.dp))
                  LinearProgressIndicator(
                    progress = { masteryFraction },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(RoundedCornerShape(3.dp)),
                    color = EmeraldPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                  )
                }
              }
            }
          }
        }

        // 5. Achievements & Badges List
        item {
          Text(
            text = strings.achievementsTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
        }

        items(achievements, key = { it.id }) { ach ->
          AchievementRow(
            achievement = ach,
            isArabic = isAr,
            strings = strings
          )
        }

        item {
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }

  if (showStoreDialog) {
    LifelineStoreDialog(
      profile = profile,
      isArabic = isAr,
      onPurchase = onPurchaseLifeline,
      onDismiss = { showStoreDialog = false }
    )
  }
}

@Composable
private fun LifelineCountBadge(
  label: String,
  count: Int,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    modifier = modifier.padding(horizontal = 3.dp)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(vertical = 8.dp)
    ) {
      Text(text = "$count", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = EmeraldPrimary)
      Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun ProfileHeaderCard(
  profile: PlayerProfile,
  isArabic: Boolean,
  strings: com.example.core.localization.AppStrings
) {
  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = EmeraldDark),
    modifier = Modifier.fillMaxWidth()
  ) {
    Box(modifier = Modifier.fillMaxWidth()) {
      IslamicGeometricBackground(
        patternColor = GoldAccent.copy(alpha = 0.08f),
        strokeWidth = 1f
      )

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(GoldAccent.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "🕌", fontSize = 36.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "${strings.currentLevel} ${profile.level}: ${profile.getTitle(isArabic)}",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = GoldAccent
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "${profile.xp} XP • ${profile.coins} 🪙",
          fontSize = 14.sp,
          color = Color.White.copy(alpha = 0.9f)
        )
      }
    }
  }
}

@Composable
private fun StatTile(
  label: String,
  value: String,
  icon: String,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = modifier.height(84.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = label,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = icon, fontSize = 14.sp)
      }
      Text(
        text = value,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

@Composable
private fun AchievementRow(
  achievement: Achievement,
  isArabic: Boolean,
  strings: com.example.core.localization.AppStrings
) {
  val isUnlocked = achievement.isUnlocked

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isUnlocked) EmeraldPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (isUnlocked) GoldContainer else MaterialTheme.colorScheme.surfaceVariant),
          contentAlignment = Alignment.Center
        ) {
          Text(text = achievement.iconEmoji, fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isArabic) achievement.titleAr else achievement.titleEn,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = if (isArabic) achievement.descAr else achievement.descEn,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      if (isUnlocked) {
        Icon(
          imageVector = Icons.Filled.CheckCircle,
          contentDescription = strings.unlocked,
          tint = EmeraldPrimary,
          modifier = Modifier
            .padding(start = 8.dp)
            .size(22.dp)
        )
      } else {
        Icon(
          imageVector = Icons.Filled.Lock,
          contentDescription = strings.locked,
          tint = Color.Gray,
          modifier = Modifier
            .padding(start = 8.dp)
            .size(18.dp)
        )
      }
    }
  }
}
