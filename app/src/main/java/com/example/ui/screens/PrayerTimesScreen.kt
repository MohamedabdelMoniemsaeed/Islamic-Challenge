package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.adhan.AdhanPlaybackState
import com.example.core.adhan.CityLocation
import com.example.core.adhan.MuadhinAudio
import com.example.core.adhan.PrayerName
import com.example.core.adhan.PrayerTimeItem
import com.example.core.adhan.PrayerTimesManager
import com.example.ui.components.AdMobBannerBar
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.NavyNight
import com.example.ui.viewmodels.PrayerTimesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
  viewModel: PrayerTimesViewModel,
  onBack: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "مواقيت الصلاة والأذان",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("prayer_times_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "الرجوع"
            )
          }
        },
        actions = {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
              .padding(end = 12.dp)
              .clickable { viewModel.toggleCityPicker(true) }
              .testTag("city_picker_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = uiState.selectedCity.nameAr,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      AdMobBannerBar(modifier = Modifier.testTag("prayer_times_admob_banner"))
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Next Prayer Hero Card
      item {
        NextPrayerHeroCard(
          nextPrayer = uiState.nextPrayer,
          countdown = uiState.countdownString,
          gregorianDate = uiState.gregorianDateString,
          cityName = uiState.selectedCity.nameAr,
          onListenAdhan = { viewModel.togglePlayAdhan() },
          isPlaying = uiState.adhanPlayerState.state == AdhanPlaybackState.PLAYING
        )
      }

      // 2. Interactive Adhan Player Card
      item {
        AdhanPlayerCard(
          playerState = uiState.adhanPlayerState,
          onTogglePlay = { viewModel.togglePlayAdhan() },
          onStop = { viewModel.stopAdhan() },
          onSelectMuadhin = { viewModel.selectMuadhin(it) }
        )
      }

      // 3. Section Title for 5 Prayers
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "مواقيت الصلوات الخمس اليوم",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "توقيت محلي دقيق",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // 4. Prayer Times List Items
      items(uiState.prayerTimes) { prayerItem ->
        val alertEnabled = uiState.prayerAlerts[prayerItem.prayer] ?: true
        PrayerTimeCard(
          item = prayerItem,
          alertEnabled = alertEnabled,
          onToggleAlert = { viewModel.togglePrayerAlert(prayerItem.prayer) }
        )
      }

      // 5. Dua After Adhan
      item {
        DuaAfterAdhanCard()
      }
    }
  }

  // City Picker Dialog
  if (uiState.showCityPicker) {
    CityPickerDialog(
      currentCity = uiState.selectedCity,
      onSelect = { viewModel.selectCity(it) },
      onDismiss = { viewModel.toggleCityPicker(false) }
    )
  }
}

@Composable
private fun NextPrayerHeroCard(
  nextPrayer: PrayerTimeItem?,
  countdown: String,
  gregorianDate: String,
  cityName: String,
  onListenAdhan: () -> Unit,
  isPlaying: Boolean
) {
  Card(
    shape = RoundedCornerShape(24.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("next_prayer_hero_card"),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(EmeraldDark, EmeraldDeep, NavyNight)
          )
        )
        .padding(20.dp)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = gregorianDate,
            style = MaterialTheme.typography.labelMedium,
            color = GoldLight
          )
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.15f)
          ) {
            Text(
              text = cityName,
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom
        ) {
          Column {
            Text(
              text = "الصلاة القادمة",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White.copy(alpha = 0.8f)
            )
            Text(
              text = "صلاة ${nextPrayer?.prayer?.arName ?: "الفجر"}",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }

          Text(
            text = nextPrayer?.timeString ?: "--:--",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = GoldAccent
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Countdown Ticker Banner
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color.Black.copy(alpha = 0.25f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.AccessTime,
                contentDescription = null,
                tint = GoldLight,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "متبقي على الأذان:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
              )
            }

            Text(
              text = countdown.ifEmpty { "00:00:00" },
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = GoldLight
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action button to play Adhan
        FilledTonalButton(
          onClick = onListenAdhan,
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isPlaying) GoldAccent else Color.White.copy(alpha = 0.2f),
            contentColor = if (isPlaying) EmeraldDeep else Color.White
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("listen_adhan_button")
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.VolumeUp,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isPlaying) "إيقاف صوت الأذان مؤقتاً" else "استمع إلى صوت الأذان الآن",
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
private fun AdhanPlayerCard(
  playerState: com.example.core.adhan.AdhanPlayerUiState,
  onTogglePlay: () -> Unit,
  onStop: () -> Unit,
  onSelectMuadhin: (MuadhinAudio) -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("adhan_player_card")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.VolumeUp,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "مشغل صوت الأذان والتكبيرات",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        if (playerState.state == AdhanPlaybackState.PLAYING) {
          AudioWaveIndicator()
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Current Muadhin description
      Text(
        text = playerState.currentMuadhin.nameAr,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
      )
      Text(
        text = playerState.currentMuadhin.description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Muadhin selector pills
      Text(
        text = "اختر المؤذن أو التكبيرات:",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        PrayerTimesManager.MUADHINS.take(3).forEach { muadhin ->
          val isSelected = muadhin.id == playerState.currentMuadhin.id
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            tonalElevation = if (isSelected) 3.dp else 1.dp,
            modifier = Modifier
              .weight(1f)
              .clickable { onSelectMuadhin(muadhin) }
          ) {
            Text(
              text = when (muadhin.id) {
                "makkah" -> "الحرم المكي"
                "madinah" -> "المسجد النبوي"
                else -> "الأقصى"
              },
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(vertical = 8.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        PrayerTimesManager.MUADHINS.drop(3).forEach { muadhin ->
          val isSelected = muadhin.id == playerState.currentMuadhin.id
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            tonalElevation = if (isSelected) 3.dp else 1.dp,
            modifier = Modifier
              .weight(1f)
              .clickable { onSelectMuadhin(muadhin) }
          ) {
            Text(
              text = when (muadhin.id) {
                "abdulbasit" -> "الشيخ عبد الباسط"
                else -> "تكبيرات العيد"
              },
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(vertical = 8.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Player Controls
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        IconButton(
          onClick = onStop,
          enabled = playerState.state != AdhanPlaybackState.IDLE,
          modifier = Modifier.size(48.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.Stop,
            contentDescription = "إيقاف",
            tint = if (playerState.state != AdhanPlaybackState.IDLE) MaterialTheme.colorScheme.error else Color.Gray
          )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Big Play/Pause Button
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier
            .size(56.dp)
            .clickable { onTogglePlay() }
            .testTag("play_pause_adhan_button")
        ) {
          Box(contentAlignment = Alignment.Center) {
            if (playerState.state == AdhanPlaybackState.BUFFERING) {
              CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp
              )
            } else {
              Icon(
                imageVector = if (playerState.state == AdhanPlaybackState.PLAYING) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = "تشغيل أو إيقاف",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
              )
            }
          }
        }
      }

      // Error message if any
      playerState.errorMessage?.let { error ->
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = error,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

@Composable
private fun PrayerTimeCard(
  item: PrayerTimeItem,
  alertEnabled: Boolean,
  onToggleAlert: () -> Unit
) {
  val isHighlighted = item.isNext || item.isCurrent

  OutlinedCard(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.outlinedCardColors(
      containerColor = if (isHighlighted) EmeraldDark.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    ),
    border = CardDefaults.outlinedCardBorder().copy(
      width = if (isHighlighted) 2.dp else 1.dp
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("prayer_card_${item.prayer.name.lowercase()}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = CircleShape,
          color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.size(40.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = if (item.prayer == PrayerName.SUNRISE) Icons.Filled.WbSunny else Icons.Filled.AccessTime,
              contentDescription = null,
              tint = if (isHighlighted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = item.prayer.arName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
              color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            if (item.isNext) {
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = GoldContainer
              ) {
                Text(
                  text = "القادمة",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
          Text(
            text = item.prayer.enName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = item.timeString,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(12.dp))

        IconButton(
          onClick = onToggleAlert,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = if (alertEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
            contentDescription = "تنبيه الأذان",
            tint = if (alertEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun DuaAfterAdhanCard() {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = GoldContainer.copy(alpha = 0.5f)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("dua_after_adhan_card")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.CheckCircle,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "دعاء ما بعد الأذان (سُنّة نبوية مؤكدة)",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = PrayerTimesManager.getDuaAfterAdhan(),
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Medium,
          lineHeight = 28.sp,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(16.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = PrayerTimesManager.getDuaVirtue(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 20.sp
      )
    }
  }
}

@Composable
private fun CityPickerDialog(
  currentCity: CityLocation,
  onSelect: (CityLocation) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "اختر المدينة لحساب مواقيت الصلاة",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      LazyColumn(
        modifier = Modifier.height(350.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(PrayerTimesManager.CITIES) { city ->
          val isSelected = city.id == currentCity.id
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelect(city) }
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = city.nameAr,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
              Text(
                text = city.nameEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("إغلاق")
      }
    }
  )
}

@Composable
private fun AudioWaveIndicator() {
  val transition = rememberInfiniteTransition(label = "audio_wave")
  val bar1 by transition.animateFloat(
    initialValue = 6f,
    targetValue = 20f,
    animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
    label = "bar1"
  )
  val bar2 by transition.animateFloat(
    initialValue = 18f,
    targetValue = 8f,
    animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
    label = "bar2"
  )
  val bar3 by transition.animateFloat(
    initialValue = 10f,
    targetValue = 22f,
    animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
    label = "bar3"
  )

  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(3.dp),
    modifier = Modifier.height(24.dp)
  ) {
    Box(
      modifier = Modifier
        .width(4.dp)
        .height(bar1.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary)
    )
    Box(
      modifier = Modifier
        .width(4.dp)
        .height(bar2.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary)
    )
    Box(
      modifier = Modifier
        .width(4.dp)
        .height(bar3.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary)
    )
  }
}
