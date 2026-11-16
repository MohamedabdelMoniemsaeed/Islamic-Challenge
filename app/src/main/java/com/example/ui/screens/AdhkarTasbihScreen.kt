package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.adhkar.AdhkarCategory
import com.example.core.adhkar.AdhkarRepository
import com.example.core.adhkar.DhikrItem
import com.example.core.audio.AudioFeedbackManager
import com.example.core.localization.AppLanguage
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarTasbihScreen(
  language: AppLanguage,
  audioManager: AudioFeedbackManager,
  soundEnabled: Boolean,
  vibrationEnabled: Boolean,
  onBack: (() -> Unit)? = null
) {
  val isAr = language == AppLanguage.ARABIC
  val context = LocalContext.current
  val prefs = remember { context.getSharedPreferences("tasbih_prefs", Context.MODE_PRIVATE) }

  var selectedTab by remember { mutableIntStateOf(0) } // 0=Tasbih, 1=Morning, 2=Evening, 3=PostPrayer
  var lifetimeTasbih by remember { mutableIntStateOf(prefs.getInt("lifetime_tasbih_count", 0)) }
  var currentCount by remember { mutableIntStateOf(prefs.getInt("current_tasbih_count", 0)) }
  var targetCount by remember { mutableIntStateOf(prefs.getInt("tasbih_target", 33)) } // 0 means unlimited
  var selectedPhraseIdx by remember { mutableIntStateOf(prefs.getInt("tasbih_phrase_idx", 0).coerceIn(0, AdhkarRepository.TASBIH_PHRASES.lastIndex)) }

  val dhikrProgress = remember { mutableStateMapOf<String, Int>() }

  val tabs = remember(isAr) {
    if (isAr) {
      listOf("المسبحة الإلكترونية", "أذكار الصباح", "أذكار المساء", "بعد الصلاة")
    } else {
      listOf("Digital Tasbih", "Morning", "Evening", "After Salah")
    }
  }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = if (isAr) "الأذكار والمسبحة" else "Adhkar & Tasbih",
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
                .testTag("adhkar_back_btn")
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
        )
      )
    },
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets(0, 0, 0, 0)
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .testTag("adhkar_tasbih_screen")
    ) {
      PrimaryScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 16.dp
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = {
              Text(
                text = title,
                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
              )
            }
          )
        }
      }

      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
      ) {
        when (selectedTab) {
          0 -> {
            TasbihContent(
              isAr = isAr,
              phrases = AdhkarRepository.TASBIH_PHRASES,
              selectedPhraseIdx = selectedPhraseIdx,
              currentCount = currentCount,
              targetCount = targetCount,
              lifetimeCount = lifetimeTasbih,
              onSelectPhrase = { idx ->
                selectedPhraseIdx = idx
                currentCount = 0
                prefs.edit()
                  .putInt("tasbih_phrase_idx", idx)
                  .putInt("current_tasbih_count", 0)
                  .apply()
              },
              onSelectTarget = { newTarget ->
                targetCount = newTarget
                prefs.edit().putInt("tasbih_target", newTarget).apply()
              },
              onIncrement = {
                val next = if (targetCount > 0 && currentCount >= targetCount) 1 else currentCount + 1
                currentCount = next
                lifetimeTasbih += 1
                prefs.edit()
                  .putInt("current_tasbih_count", next)
                  .putInt("lifetime_tasbih_count", lifetimeTasbih)
                  .apply()
                if (targetCount > 0 && next == targetCount) {
                  audioManager.playAchievementFeedback(soundEnabled, vibrationEnabled)
                } else {
                  audioManager.playClickFeedback(soundEnabled, vibrationEnabled)
                }
              },
              onReset = {
                currentCount = 0
                prefs.edit().putInt("current_tasbih_count", 0).apply()
                audioManager.playClickFeedback(soundEnabled, vibrationEnabled)
              }
            )
          }

          else -> {
            val category = when (selectedTab) {
              1 -> AdhkarCategory.MORNING
              2 -> AdhkarCategory.EVENING
              else -> AdhkarCategory.AFTER_PRAYER
            }
            val list = remember(category) {
              AdhkarRepository.ALL_ADHKAR.filter { it.category == category }
            }
            AdhkarCategoryList(
              items = list,
              isAr = isAr,
              progressMap = dhikrProgress,
              onTapDhikr = { item ->
                val prev = dhikrProgress[item.id] ?: 0
                if (prev < item.targetCount) {
                  val next = prev + 1
                  dhikrProgress[item.id] = next
                  if (next == item.targetCount) {
                    audioManager.playAchievementFeedback(soundEnabled, vibrationEnabled)
                  } else {
                    audioManager.playClickFeedback(soundEnabled, vibrationEnabled)
                  }
                }
              },
              onResetAll = {
                list.forEach { dhikrProgress[it.id] = 0 }
              }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TasbihContent(
  isAr: Boolean,
  phrases: List<com.example.core.adhkar.TasbihPhrase>,
  selectedPhraseIdx: Int,
  currentCount: Int,
  targetCount: Int,
  lifetimeCount: Int,
  onSelectPhrase: (Int) -> Unit,
  onSelectTarget: (Int) -> Unit,
  onIncrement: () -> Unit,
  onReset: () -> Unit
) {
  val currentPhrase = phrases[selectedPhraseIdx]
  val progressFraction = if (targetCount > 0) {
    (currentCount.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f)
  } else {
    ((currentCount % 33).toFloat() / 33f)
  }
  val animatedSweep by animateFloatAsState(
    targetValue = progressFraction * 360f,
    animationSpec = tween(durationMillis = 220),
    label = "tasbih_ring"
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .widthIn(max = 600.dp),
    contentPadding = PaddingValues(18.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // 1. Phrase Display Banner
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.linearGradient(colors = listOf(EmeraldDark, EmeraldDeep))
            )
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = GoldAccent.copy(alpha = 0.22f)
            ) {
              Text(
                text = if (isAr) "إجمالي التسبيحات: $lifetimeCount" else "Total Dhikr: $lifetimeCount",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = GoldLight,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }

            if (targetCount > 0 && currentCount >= targetCount) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = SuccessGreen
              ) {
                Text(
                  text = if (isAr) "تقبّل الله ✓" else "Completed ✓",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = currentPhrase.textAr,
            fontFamily = AmiriFontFamily,
            fontSize = 26.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
          )

          if (!isAr) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "${currentPhrase.transliterationEn} — ${currentPhrase.meaningEn}",
              style = MaterialTheme.typography.bodySmall,
              color = GoldLight.copy(alpha = 0.9f),
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }

    // 2. Dhikr Phrase Chips
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        phrases.forEachIndexed { idx, phrase ->
          FilterChip(
            selected = idx == selectedPhraseIdx,
            onClick = { onSelectPhrase(idx) },
            label = {
              Text(
                text = if (isAr) phrase.textAr else phrase.transliterationEn,
                maxLines = 1,
                fontWeight = if (idx == selectedPhraseIdx) FontWeight.Bold else FontWeight.Medium
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primary,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
          )
        }
      }
    }

    // 3. Target Count Chips
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isAr) "الهدف:" else "Target:",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        listOf(33, 99, 100, 0).forEach { target ->
          val label = if (target == 0) "∞" else "$target"
          FilterChip(
            selected = targetCount == target,
            onClick = { onSelectTarget(target) },
            label = { Text(label, fontWeight = FontWeight.Bold) },
            modifier = Modifier.padding(horizontal = 3.dp)
          )
        }
      }
    }

    // 4. Interactive Circular Tasbih Counter
    item {
      Spacer(modifier = Modifier.height(4.dp))
      val trackColor = MaterialTheme.colorScheme.surfaceVariant
      val ringColor = if (targetCount > 0 && currentCount >= targetCount) GoldAccent else EmeraldPrimary

      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(230.dp)
          .clip(CircleShape)
          .clickable { onIncrement() }
          .testTag("tasbih_tap_circle")
      ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
          drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
          )
          drawArc(
            color = ringColor,
            startAngle = -90f,
            sweepAngle = animatedSweep,
            useCenter = false,
            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
          )
        }

        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 4.dp,
          modifier = Modifier.size(180.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "$currentCount",
              fontSize = 48.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary
            )
            if (targetCount > 0) {
              Text(
                text = "/ $targetCount",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.TouchApp,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isAr) "اضغط للتسبيح" else "Tap to count",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    // 5. Reset Counter Action
    item {
      OutlinedButton(
        onClick = onReset,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .height(48.dp)
          .testTag("tasbih_reset_btn")
      ) {
        Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isAr) "تصفير العداد الحالي" else "Reset Current Counter",
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }
}

@Composable
private fun AdhkarCategoryList(
  items: List<DhikrItem>,
  isAr: Boolean,
  progressMap: Map<String, Int>,
  onTapDhikr: (DhikrItem) -> Unit,
  onResetAll: () -> Unit
) {
  val completedCount = items.count { (progressMap[it.id] ?: 0) >= it.targetCount }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .widthIn(max = 600.dp),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isAr) {
              "المكتمل: $completedCount من ${items.size}"
            } else {
              "Completed: $completedCount of ${items.size}"
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )

          IconButton(
            onClick = onResetAll,
            modifier = Modifier.size(48.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.Refresh,
              contentDescription = if (isAr) "إعادة الضبط" else "Reset All",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }

    items(items, key = { it.id }) { dhikr ->
      val current = progressMap[dhikr.id] ?: 0
      val isDone = current >= dhikr.targetCount
      val remaining = (dhikr.targetCount - current).coerceAtLeast(0)

      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isDone) {
            EmeraldPrimary.copy(alpha = 0.08f)
          } else {
            MaterialTheme.colorScheme.surface
          }
        ),
        border = BorderStroke(
          width = if (isDone) 1.5.dp else 1.dp,
          color = if (isDone) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = dhikr.textAr,
            fontFamily = AmiriFontFamily,
            fontSize = 20.sp,
            lineHeight = 34.sp,
            color = MaterialTheme.colorScheme.onSurface
          )

          if (!isAr) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = dhikr.transliterationEn,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = dhikr.translationEn,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 19.sp
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = GoldContainer.copy(alpha = 0.7f)
            ) {
              Text(
                text = if (isAr) dhikr.referenceAr else dhikr.referenceEn,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF78350F),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }

            Button(
              onClick = { onTapDhikr(dhikr) },
              enabled = !isDone,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isDone) SuccessGreen else MaterialTheme.colorScheme.primary,
                disabledContainerColor = SuccessGreen.copy(alpha = 0.85f),
                disabledContentColor = Color.White
              ),
              modifier = Modifier.height(48.dp)
            ) {
              if (isDone) {
                Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = if (isAr) "تم" else "Done", fontWeight = FontWeight.Bold)
              } else {
                Text(
                  text = if (isAr) "تسبيح ($remaining)" else "Count ($remaining)",
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }
  }
}
