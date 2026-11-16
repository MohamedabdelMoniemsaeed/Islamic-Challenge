package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.core.adhan.AdhanPlaybackState
import com.example.core.adhan.AdhanPlayerUiState
import com.example.core.adhan.AsrJuristicMethod
import com.example.core.adhan.CalculationMethod
import com.example.core.adhan.CityLocation
import com.example.core.adhan.MuadhinAudio
import com.example.core.adhan.PrayerName
import com.example.core.adhan.PrayerTimeItem
import com.example.core.adhan.PrayerTimesManager
import com.example.core.adhan.QiblahCalculator
import com.example.core.localization.AppLanguage
import com.example.core.permissions.DeviceAccessManager
import com.example.core.permissions.DeviceAccessStatus
import com.example.core.widgets.HomeScreenWidgetHelper
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.NavyNight
import com.example.ui.viewmodels.PrayerTimesViewModel
import java.util.Locale
import kotlin.math.roundToInt

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
  viewModel: PrayerTimesViewModel,
  language: AppLanguage = AppLanguage.ARABIC,
  hourlySalawatEnabled: Boolean = true,
  onHourlySalawatToggle: (Boolean) -> Unit = {},
  onTestSalawatReminder: () -> Unit = {},
  onBack: (() -> Unit)? = null
) {
  val uiState by viewModel.uiState.collectAsState()
  val isAr = language == AppLanguage.ARABIC
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = androidx.compose.runtime.rememberCoroutineScope()

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) {
    viewModel.refreshBackgroundStatus()
  }

  val locationPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    viewModel.refreshBackgroundStatus()
    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (granted) {
      viewModel.detectLocationWithGps(isAr) { msg ->
        scope.launch { snackbarHostState.showSnackbar(msg) }
      }
    } else {
      scope.launch {
        snackbarHostState.showSnackbar(
          if (isAr) "لم يتم منح إذن الموقع (GPS). يمكنك اختيار المدينة يدوياً أو منحه من الإعدادات."
          else "GPS permission denied. You can choose a city manually or enable it in Settings."
        )
      }
    }
  }

  val triggerGpsDetection: () -> Unit = {
    val status = DeviceAccessManager.getStatus(context)
    when {
      !status.hasLocationPermission -> {
        locationPermissionLauncher.launch(
          arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
          )
        )
      }
      !status.isDeviceGpsEnabled -> {
        DeviceAccessManager.openLocationSourceSettings(context)
      }
      else -> {
        viewModel.detectLocationWithGps(isAr) { msg ->
          scope.launch { snackbarHostState.showSnackbar(msg) }
        }
      }
    }
  }

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        viewModel.refreshBackgroundStatus()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (isAr) "المواقيت والأذان والقبلة" else "Prayer, Adhan & Qiblah",
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
          )
        },
        navigationIcon = {
          if (onBack != null) {
            IconButton(
              onClick = onBack,
              modifier = Modifier
                .size(48.dp)
                .testTag("prayer_times_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = if (isAr) "الرجوع" else "Back"
              )
            }
          }
        },
        actions = {
          IconButton(
            onClick = { viewModel.toggleMethodPicker(true) },
            modifier = Modifier
              .size(48.dp)
              .testTag("method_picker_button")
          ) {
            Icon(
              imageVector = Icons.Filled.Tune,
              contentDescription = if (isAr) "طريقة الحساب" else "Calculation Method",
              tint = MaterialTheme.colorScheme.primary
            )
          }

          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
              .padding(end = 10.dp)
              .clickable { viewModel.toggleCityPicker(true) }
              .testTag("city_picker_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
              Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isAr) uiState.selectedCity.nameAr else uiState.selectedCity.nameEn,
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
    contentWindowInsets = WindowInsets(0, 0, 0, 0)
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentAlignment = Alignment.TopCenter
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = 620.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // 1. Next Prayer Hero Card
        item {
          NextPrayerHeroCard(
            nextPrayer = uiState.nextPrayer,
            countdown = uiState.countdownString,
            hijriDate = uiState.hijriDateString,
            gregorianDate = uiState.gregorianDateString,
            cityName = if (isAr) uiState.selectedCity.nameAr else uiState.selectedCity.nameEn,
            isAr = isAr,
            onListenAdhan = { viewModel.togglePlayAdhan(isAr) },
            playbackState = uiState.adhanPlayerState.state
          )
        }

        // 2. Interactive Adhan Player Card (with buffering/seek/retry/offline chime)
        item {
          AdhanPlayerCard(
            playerState = uiState.adhanPlayerState,
            isAr = isAr,
            onTogglePlay = { viewModel.togglePlayAdhan(isAr) },
            onStop = { viewModel.stopAdhan() },
            onSeek = { viewModel.seekAdhan(it) },
            onPlayOfflineChime = { viewModel.playOfflineChimeFallback() },
            onSelectMuadhin = { viewModel.selectMuadhin(it) }
          )
        }

        // 2b. Background Adhan, Hourly Salawat & Device Readiness Card
        item {
          BackgroundAdhanAndSalawatCard(
            isAr = isAr,
            autoPlayAdhan = uiState.autoPlayAdhanAtPrayerTime,
            hasNotificationPermission = uiState.hasNotificationPermission,
            canScheduleExactAlarms = uiState.canScheduleExactAlarms,
            hourlySalawatEnabled = hourlySalawatEnabled,
            deviceAccessStatus = uiState.deviceAccessStatus,
            isUsingGpsLocation = uiState.isUsingGpsLocation,
            isDetectingGps = uiState.isDetectingGps,
            currentCityName = if (isAr) uiState.selectedCity.nameAr else uiState.selectedCity.nameEn,
            onToggleAutoPlayAdhan = { viewModel.toggleAutoPlayAdhan(it) },
            onGrantNotificationPermission = {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !uiState.hasNotificationPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
              } else {
                DeviceAccessManager.openAppNotificationSettings(context)
              }
            },
            onGrantExactAlarmPermission = {
              DeviceAccessManager.openExactAlarmSettings(context)
            },
            onRequestGpsLocation = triggerGpsDetection,
            onOpenInternetSettings = { DeviceAccessManager.openInternetSettings(context) },
            onToggleHourlySalawat = { enabled ->
              if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !uiState.hasNotificationPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
              }
              onHourlySalawatToggle(enabled)
            },
            onTestSalawat = onTestSalawatReminder,
            onPinHomeWidget = {
              val ok = HomeScreenWidgetHelper.requestPinPrayerTimesWidget(context)
              scope.launch {
                snackbarHostState.showSnackbar(
                  if (ok) {
                    if (isAr) "أكّد الإضافة لوضع ويدجت مواقيت الصلاة على الشاشة الرئيسية"
                    else "Confirm the launcher prompt to place the Prayer Times widget on Home Screen"
                  } else {
                    if (isAr) "اضغط مطولاً على شاشة الهاتف ← الأدوات (Widgets) ← تحدي المعرفة الإسلامية لإضافة الويدجت"
                    else "Long-press Home Screen -> Widgets -> Islamic Challenge to add the widget"
                  }
                )
              }
            }
          )
        }

        // 3. Qiblah Compass Card (Zero Permissions)
        item {
          QiblahCompassCard(
            bearingDegrees = uiState.qiblahBearingDegrees,
            cityName = if (isAr) uiState.selectedCity.nameAr else uiState.selectedCity.nameEn,
            isAr = isAr
          )
        }

        // 4. Section Title for Prayer Schedule
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = if (isAr) "مواقيت الصلوات اليوم" else "Today's Prayer Schedule",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = if (isAr) uiState.calculationMethod.nameAr else uiState.calculationMethod.nameEn,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.widthIn(max = 180.dp)
            )
          }
        }

        // 5. Prayer Times List Items
        items(uiState.prayerTimes, key = { it.prayer.name }) { prayerItem ->
          val alertEnabled = uiState.prayerAlerts[prayerItem.prayer] ?: true
          PrayerTimeCard(
            item = prayerItem,
            isAr = isAr,
            alertEnabled = alertEnabled,
            onToggleAlert = { viewModel.togglePrayerAlert(prayerItem.prayer) }
          )
        }

        // 6. Dua After Adhan
        item {
          DuaAfterAdhanCard(isAr = isAr)
        }
      }
    }
  }

  // City Picker Dialog
  if (uiState.showCityPicker) {
    CityPickerDialog(
      currentCity = uiState.selectedCity,
      isAr = isAr,
      isDetectingGps = uiState.isDetectingGps,
      deviceAccessStatus = uiState.deviceAccessStatus,
      onDetectGps = triggerGpsDetection,
      onSelect = { viewModel.selectCity(it) },
      onDismiss = { viewModel.toggleCityPicker(false) }
    )
  }

  // Calculation Method & Asr Madhab Dialog
  if (uiState.showMethodPicker) {
    CalculationMethodDialog(
      currentMethod = uiState.calculationMethod,
      currentAsr = uiState.asrMethod,
      isAr = isAr,
      onSelectMethod = { viewModel.selectCalculationMethod(it) },
      onSelectAsr = { viewModel.selectAsrMethod(it) },
      onDismiss = { viewModel.toggleMethodPicker(false) }
    )
  }
}

@Composable
private fun NextPrayerHeroCard(
  nextPrayer: PrayerTimeItem?,
  countdown: String,
  hijriDate: String,
  gregorianDate: String,
  cityName: String,
  isAr: Boolean,
  onListenAdhan: () -> Unit,
  playbackState: AdhanPlaybackState
) {
  val isPlaying = playbackState == AdhanPlaybackState.PLAYING
  val isBuffering = playbackState == AdhanPlaybackState.BUFFERING

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
          verticalAlignment = Alignment.Top
        ) {
          Column(modifier = Modifier.weight(1f)) {
            if (hijriDate.isNotBlank()) {
              Text(
                text = hijriDate,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = GoldLight
              )
            }
            Text(
              text = gregorianDate,
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.85f)
            )
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.16f)
          ) {
            Text(
              text = cityName,
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = if (isAr) "الصلاة القادمة" else "Next Prayer",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.82f)
              )
              if (nextPrayer?.isTomorrow == true) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = GoldAccent.copy(alpha = 0.25f)
                ) {
                  Text(
                    text = if (isAr) "غداً" else "Tomorrow",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldLight,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
            val prayerTitle = if (isAr) {
              "صلاة ${nextPrayer?.prayer?.arName ?: "الفجر"}"
            } else {
              "${nextPrayer?.prayer?.enName ?: "Fajr"} Prayer"
            }
            Text(
              text = prayerTitle,
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }

          Text(
            text = nextPrayer?.getTimeString(isAr) ?: "--:--",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = GoldAccent
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color.Black.copy(alpha = 0.28f),
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
                text = if (isAr) "متبقي على الأذان:" else "Time Remaining:",
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

        FilledTonalButton(
          onClick = onListenAdhan,
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isPlaying) GoldAccent else Color.White.copy(alpha = 0.22f),
            contentColor = if (isPlaying) EmeraldDeep else Color.White
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("listen_adhan_button")
        ) {
          if (isBuffering) {
            CircularProgressIndicator(
              color = Color.White,
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp
            )
          } else {
            Icon(
              imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.VolumeUp,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = when {
              isBuffering -> if (isAr) "جاري تحميل الصوت..." else "Loading audio..."
              isPlaying -> if (isAr) "إيقاف صوت الأذان مؤقتاً" else "Pause Adhan Audio"
              else -> if (isAr) "استمع إلى صوت الأذان الآن" else "Listen to Adhan Now"
            },
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
private fun AdhanPlayerCard(
  playerState: AdhanPlayerUiState,
  isAr: Boolean,
  onTogglePlay: () -> Unit,
  onStop: () -> Unit,
  onSeek: (Int) -> Unit,
  onPlayOfflineChime: () -> Unit,
  onSelectMuadhin: (MuadhinAudio) -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
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
            text = if (isAr) "مشغل صوت الأذان والتكبيرات" else "Adhan & Takbeerat Player",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        if (playerState.state == AdhanPlaybackState.PLAYING) {
          AudioWaveIndicator()
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = playerState.currentMuadhin.getName(isAr),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
      )
      Text(
        text = playerState.currentMuadhin.getDescription(isAr),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = if (isAr) "اختر المؤذن أو المنبّه:" else "Select Muadhin or Audio:",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(6.dp))

      val firstRow = PrayerTimesManager.MUADHINS.take(3)
      val secondRow = PrayerTimesManager.MUADHINS.drop(3)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        firstRow.forEach { muadhin ->
          MuadhinChip(
            muadhin = muadhin,
            isSelected = muadhin.id == playerState.currentMuadhin.id,
            isAr = isAr,
            modifier = Modifier.weight(1f),
            onClick = { onSelectMuadhin(muadhin) }
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        secondRow.forEach { muadhin ->
          MuadhinChip(
            muadhin = muadhin,
            isSelected = muadhin.id == playerState.currentMuadhin.id,
            isAr = isAr,
            modifier = Modifier.weight(1f),
            onClick = { onSelectMuadhin(muadhin) }
          )
        }
      }

      // Audio progress Slider when active or paused
      if (playerState.durationMs > 0 &&
        (playerState.state == AdhanPlaybackState.PLAYING || playerState.state == AdhanPlaybackState.PAUSED)
      ) {
        Spacer(modifier = Modifier.height(10.dp))
        val duration = playerState.durationMs.coerceAtLeast(1)
        val pos = playerState.currentPositionMs.coerceIn(0, duration)
        Slider(
          value = pos.toFloat(),
          onValueChange = { onSeek(it.toInt()) },
          valueRange = 0f..duration.toFloat(),
          modifier = Modifier.fillMaxWidth()
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = formatAudioTime(pos),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = formatAudioTime(duration),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

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
            contentDescription = if (isAr) "إيقاف" else "Stop",
            tint = if (playerState.state != AdhanPlaybackState.IDLE) MaterialTheme.colorScheme.error else Color.Gray
          )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
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
                imageVector = if (playerState.state == AdhanPlaybackState.PLAYING) {
                  Icons.Filled.Pause
                } else {
                  Icons.Filled.PlayArrow
                },
                contentDescription = if (isAr) "تشغيل أو إيقاف" else "Play or Pause",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
              )
            }
          }
        }
      }

      if (playerState.state == AdhanPlaybackState.BUFFERING) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = if (isAr) "جاري تحميل الملف الصوتي وتجهيزه..." else "Downloading and preparing Adhan audio...",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.primary,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }

      playerState.errorMessage?.let { error ->
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = error,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onTogglePlay,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
          ) {
            Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = if (isAr) "إعادة المحاولة" else "Retry", fontSize = 12.sp)
          }
          Button(
            onClick = onPlayOfflineChime,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
          ) {
            Text(
              text = if (isAr) "نغمة المسجد" else "Offline Chime",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MuadhinChip(
  muadhin: MuadhinAudio,
  isSelected: Boolean,
  isAr: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val label = when (muadhin.id) {
    "makkah" -> if (isAr) "الحرم المكي" else "Makkah"
    "madinah" -> if (isAr) "المسجد النبوي" else "Madinah"
    "alaqsa" -> if (isAr) "الأقصى" else "Al-Aqsa"
    "abdulbasit" -> if (isAr) "عبد الباسط" else "Abdul Basit"
    "takbeerat" -> if (isAr) "التكبيرات" else "Takbeerat"
    else -> if (isAr) "نغمة المسجد" else "Offline Chime"
  }

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
    tonalElevation = if (isSelected) 3.dp else 1.dp,
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp)
    )
  }
}

@Composable
private fun QiblahCompassCard(
  bearingDegrees: Double,
  cityName: String,
  isAr: Boolean
) {
  val context = LocalContext.current
  var deviceAzimuth by remember { mutableFloatStateOf(0f) }
  var hasCompassSensor by remember { mutableStateOf(false) }

  DisposableEffect(context) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val magnetSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val gravity = FloatArray(3)
    val geomagnetic = FloatArray(3)
    var hasGravity = false
    var hasMagnet = false
    val rotationMatrix = FloatArray(9)
    val orientationAngles = FloatArray(3)

    val listener = object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
          Sensor.TYPE_ROTATION_VECTOR -> {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val az = ((Math.toDegrees(orientationAngles[0].toDouble()).toFloat()) + 360f) % 360f
            deviceAzimuth = az
          }

          Sensor.TYPE_ACCELEROMETER -> {
            System.arraycopy(event.values, 0, gravity, 0, 3)
            hasGravity = true
          }

          Sensor.TYPE_MAGNETIC_FIELD -> {
            System.arraycopy(event.values, 0, geomagnetic, 0, 3)
            hasMagnet = true
          }
        }
        if (rotationSensor == null && hasGravity && hasMagnet) {
          if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val az = ((Math.toDegrees(orientationAngles[0].toDouble()).toFloat()) + 360f) % 360f
            deviceAzimuth = az
          }
        }
      }

      override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    if (rotationSensor != null) {
      hasCompassSensor = true
      sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
    } else if (accelSensor != null && magnetSensor != null) {
      hasCompassSensor = true
      sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
      sensorManager.registerListener(listener, magnetSensor, SensorManager.SENSOR_DELAY_UI)
    } else {
      hasCompassSensor = false
    }

    onDispose {
      sensorManager?.unregisterListener(listener)
    }
  }

  val targetArrowRotation = if (hasCompassSensor) {
    (bearingDegrees.toFloat() - deviceAzimuth + 360f) % 360f
  } else {
    bearingDegrees.toFloat()
  }
  val smoothRotation by animateFloatAsState(
    targetValue = targetArrowRotation,
    animationSpec = tween(durationMillis = 250),
    label = "qiblah_arrow"
  )

  val bearingInt = bearingDegrees.roundToInt()
  val dirLabel = if (isAr) {
    QiblahCalculator.getDirectionLabelAr(bearingDegrees)
  } else {
    QiblahCalculator.getDirectionLabelEn(bearingDegrees)
  }

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("qiblah_compass_card")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.Explore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isAr) "اتجاه القبلة الشريفة" else "Qiblah Direction",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = if (isAr) {
            "$bearingInt° من الشمال الحقيقي ($dirLabel)"
          } else {
            "$bearingInt° from True North ($dirLabel)"
          },
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (hasCompassSensor) {
            if (isAr) "حرّك هاتفك أفقياً لمحاذاة السهم الذهبي نحو الكعبة 🕋" else "Rotate your device flat to align the golden arrow toward Kaaba 🕋"
          } else {
            if (isAr) "اتجاه القبلة من $cityName بالنسبة للشمال الجغرافي" else "Qiblah angle from True North for $cityName"
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(96.dp)
      ) {
        val ringColor = MaterialTheme.colorScheme.outlineVariant
        val fillColor = EmeraldPrimary.copy(alpha = 0.08f)

        Canvas(modifier = Modifier.fillMaxSize()) {
          val radius = size.minDimension / 2f
          drawCircle(color = fillColor, radius = radius)
          drawCircle(color = ringColor, radius = radius - 2.dp.toPx(), style = Stroke(width = 2.dp.toPx()))

          rotate(degrees = smoothRotation) {
            val arrowPath = Path().apply {
              moveTo(center.x, center.y - radius + 12.dp.toPx())
              lineTo(center.x - 10.dp.toPx(), center.y + 8.dp.toPx())
              lineTo(center.x, center.y)
              lineTo(center.x + 10.dp.toPx(), center.y + 8.dp.toPx())
              close()
            }
            drawPath(path = arrowPath, color = GoldAccent)
            drawCircle(color = EmeraldDark, radius = 4.dp.toPx(), center = Offset(center.x, center.y))
          }
        }

        Text(
          text = "N",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 4.dp)
        )
      }
    }
  }
}

@Composable
private fun PrayerTimeCard(
  item: PrayerTimeItem,
  isAr: Boolean,
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
        .padding(horizontal = 16.dp, vertical = 12.dp),
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
              text = if (isAr) item.prayer.arName else item.prayer.enName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
              color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            if (item.isNext || item.isCurrent) {
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (item.isNext) GoldContainer else MaterialTheme.colorScheme.secondaryContainer
              ) {
                Text(
                  text = when {
                    item.isNext -> if (isAr) "القادمة" else "Next"
                    else -> if (isAr) "الحالية" else "Current"
                  },
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
          Text(
            text = if (isAr) item.prayer.enName else item.prayer.arName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = item.getTimeString(isAr),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(
          onClick = onToggleAlert,
          modifier = Modifier.size(48.dp)
        ) {
          Icon(
            imageVector = if (alertEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
            contentDescription = if (isAr) "تنبيه الأذان" else "Prayer Notification Alert",
            tint = if (alertEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.size(22.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun DuaAfterAdhanCard(isAr: Boolean) {
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
          text = if (isAr) "دعاء ما بعد الأذان (سُنّة نبوية)" else "Supplication After Adhan (Sunnah)",
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
          fontFamily = AmiriFontFamily,
          fontSize = 19.sp,
          lineHeight = 32.sp,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(16.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = PrayerTimesManager.getDuaVirtue(isAr),
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
  isAr: Boolean,
  isDetectingGps: Boolean = false,
  deviceAccessStatus: DeviceAccessStatus = DeviceAccessStatus(),
  onDetectGps: () -> Unit = {},
  onSelect: (CityLocation) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isAr) "اختر المدينة أو الموقع (GPS)" else "Select City or GPS Location",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column {
        Button(
          onClick = onDetectGps,
          enabled = !isDetectingGps,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("city_picker_gps_btn")
        ) {
          if (isDetectingGps) {
            CircularProgressIndicator(
              color = Color.White,
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp
            )
          } else {
            Icon(
              imageVector = Icons.Filled.MyLocation,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          val gpsBtnText = when {
            isDetectingGps -> if (isAr) "جاري تحديد موقعك..." else "Detecting location..."
            !deviceAccessStatus.hasLocationPermission -> if (isAr) "السماح وتحديد موقعي بـ GPS" else "Allow & Detect via GPS"
            !deviceAccessStatus.isDeviceGpsEnabled -> if (isAr) "تشغيل خدمة GPS من الهاتف" else "Turn On Phone GPS"
            else -> if (isAr) "تحديد موقعي التلقائي عبر GPS" else "Auto-Detect Location via GPS"
          }
          Text(text = gpsBtnText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
          modifier = Modifier.height(310.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(PrayerTimesManager.CITIES, key = { it.id }) { city ->
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
                  text = if (isAr) city.nameAr else city.nameEn,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                  text = if (isAr) city.nameEn else city.nameAr,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(if (isAr) "إغلاق" else "Close")
      }
    }
  )
}

@Composable
private fun CalculationMethodDialog(
  currentMethod: CalculationMethod,
  currentAsr: AsrJuristicMethod,
  isAr: Boolean,
  onSelectMethod: (CalculationMethod) -> Unit,
  onSelectAsr: (AsrJuristicMethod) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isAr) "إعدادات حساب المواقيت" else "Prayer Calculation Settings",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      LazyColumn(
        modifier = Modifier.height(380.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        item {
          Text(
            text = if (isAr) "الهيئة الفلكية المعتمدة:" else "Calculation Authority:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
        items(CalculationMethod.entries, key = { it.id }) { method ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .clickable { onSelectMethod(method) }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = method == currentMethod,
              onClick = { onSelectMethod(method) }
            )
            Text(
              text = if (isAr) method.nameAr else method.nameEn,
              style = MaterialTheme.typography.bodyMedium
            )
          }
        }
        item {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (isAr) "مذهب صلاة العصر:" else "Asr Juristic Method:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
        items(AsrJuristicMethod.entries, key = { it.id }) { asr ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .clickable { onSelectAsr(asr) }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = asr == currentAsr,
              onClick = { onSelectAsr(asr) }
            )
            Text(
              text = if (isAr) asr.nameAr else asr.nameEn,
              style = MaterialTheme.typography.bodyMedium
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(if (isAr) "تم" else "Done")
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

private fun formatAudioTime(ms: Int): String {
  val totalSec = (ms / 1000).coerceAtLeast(0)
  val min = totalSec / 60
  val sec = totalSec % 60
  return String.format(Locale.US, "%02d:%02d", min, sec)
}

@Composable
private fun BackgroundAdhanAndSalawatCard(
  isAr: Boolean,
  autoPlayAdhan: Boolean,
  hasNotificationPermission: Boolean,
  canScheduleExactAlarms: Boolean,
  hourlySalawatEnabled: Boolean,
  deviceAccessStatus: DeviceAccessStatus = DeviceAccessStatus(),
  isUsingGpsLocation: Boolean = false,
  isDetectingGps: Boolean = false,
  currentCityName: String = "",
  onToggleAutoPlayAdhan: (Boolean) -> Unit,
  onGrantNotificationPermission: () -> Unit,
  onGrantExactAlarmPermission: () -> Unit,
  onRequestGpsLocation: () -> Unit = {},
  onOpenInternetSettings: () -> Unit = {},
  onToggleHourlySalawat: (Boolean) -> Unit,
  onTestSalawat: () -> Unit,
  onPinHomeWidget: () -> Unit = {}
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("background_adhan_salawat_card")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Auto-Play Adhan Even When Closed
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier.weight(1f).padding(end = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Filled.NotificationsActive,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = if (isAr) "تشغيل الأذان تلقائياً عند الصلاة" else "Auto-Play Adhan at Prayer Time",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (isAr) {
                "يعمل في مواعيد الصلاة حتى عند إغلاق التطبيق"
              } else {
                "Plays automatically at prayer times even when the app is closed"
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
        Switch(
          checked = autoPlayAdhan,
          onCheckedChange = onToggleAutoPlayAdhan,
          colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = EmeraldPrimary
          ),
          modifier = Modifier.testTag("auto_play_adhan_switch")
        )
      }

      if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
          onClick = onGrantNotificationPermission,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("prayer_grant_notifications_btn")
        ) {
          Icon(
            imageVector = Icons.Filled.Notifications,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isAr) "تفعيل إذن الإشعارات لتنبيهات الأذان" else "Allow Notifications for Adhan Alerts",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = EmeraldPrimary
          )
        }
      }

      if (!canScheduleExactAlarms && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = GoldContainer.copy(alpha = 0.65f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.Alarm,
                contentDescription = null,
                tint = EmeraldDark,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isAr) {
                  "لضمان انطلاق الأذان في الثانية المحددة حتى والتطبيق مغلق، فعّل إذن المنبّهات الدقيقة:"
                } else {
                  "For exact Adhan timing when the app is closed, enable Alarms & Reminders access:"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
              onClick = onGrantExactAlarmPermission,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("prayer_grant_exact_alarm_btn")
            ) {
              Text(
                text = if (isAr) "تفعيل التوقيت الدقيق للأذان" else "Enable Exact Adhan Timing",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
              )
            }
          }
        }
      } else if (autoPlayAdhan) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isAr) {
              "منبّه الأذان الدقيق مُفعّل — ينطلق في الموعد بالثانية مع تجهيز الصوت مسبقاً"
            } else {
              "Exact AlarmClock Adhan active — triggers on the exact second with pre-cached audio"
            },
            style = MaterialTheme.typography.labelSmall,
            color = EmeraldPrimary,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
      Spacer(modifier = Modifier.height(12.dp))

      // GPS Location & Internet Connectivity Quick Controls
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onRequestGpsLocation,
          enabled = !isDetectingGps,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("prayer_gps_detect_btn")
        ) {
          if (isDetectingGps) {
            CircularProgressIndicator(
              color = EmeraldPrimary,
              modifier = Modifier.size(16.dp),
              strokeWidth = 2.dp
            )
          } else {
            Icon(
              imageVector = Icons.Filled.MyLocation,
              contentDescription = null,
              tint = if (deviceAccessStatus.isGpsReady && isUsingGpsLocation) EmeraldPrimary else MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          val gpsLabel = when {
            isDetectingGps -> if (isAr) "تحديد..." else "Locating..."
            !deviceAccessStatus.hasLocationPermission -> if (isAr) "إذن GPS" else "GPS Access"
            !deviceAccessStatus.isDeviceGpsEnabled -> if (isAr) "تشغيل GPS" else "Enable GPS"
            isUsingGpsLocation -> if (isAr) "تحديث GPS" else "Refresh GPS"
            else -> if (isAr) "موقعي بـ GPS" else "Use GPS"
          }
          Text(
            text = gpsLabel,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        OutlinedButton(
          onClick = onOpenInternetSettings,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("prayer_internet_settings_btn")
        ) {
          Icon(
            imageVector = if (deviceAccessStatus.isInternetConnected) Icons.Filled.Wifi else Icons.Filled.WifiOff,
            contentDescription = null,
            tint = if (deviceAccessStatus.isInternetConnected) EmeraldPrimary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (deviceAccessStatus.isInternetConnected) {
              if (isAr) "الإنترنت متصل" else "Online"
            } else {
              if (isAr) "إعداد الإنترنت" else "Connect Net"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (deviceAccessStatus.isInternetConnected) EmeraldPrimary else MaterialTheme.colorScheme.error,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      if (currentCityName.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = if (isAr) {
            "الموقع الحالي للمواقيت والقبلة: $currentCityName"
          } else {
            "Active location for Prayer & Qiblah: $currentCityName"
          },
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
      Spacer(modifier = Modifier.height(12.dp))

      // Hourly "Salli ala Muhammad" Reminder
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
          Text(
            text = if (isAr) "تذكير «صلِّ على محمد ﷺ» عند رأس كل ساعة" else "Hourly \"Salli 'ala Muhammad ﷺ\" (:00)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (isAr) {
              "ينطق «صلِّ على محمد» بصوت رجل عند الدقيقة ٠٠ من كل ساعة"
            } else {
              "Says \"Salli 'ala Muhammad\" in a male voice at minute :00 every hour"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Switch(
          checked = hourlySalawatEnabled,
          onCheckedChange = onToggleHourlySalawat,
          colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = EmeraldPrimary
          ),
          modifier = Modifier.testTag("prayer_hourly_salawat_switch")
        )
      }

      if (hourlySalawatEnabled) {
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
          onClick = onTestSalawat,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("prayer_test_salawat_btn")
        ) {
          Icon(
            imageVector = Icons.Filled.VolumeUp,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isAr) "تجربة صوت «صلِّ على محمد» الآن" else "Test \"Salli 'ala Muhammad\" Now",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = EmeraldPrimary
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
      Spacer(modifier = Modifier.height(12.dp))

      FilledTonalButton(
        onClick = onPinHomeWidget,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("prayer_pin_home_widget_btn")
      ) {
        Icon(
          imageVector = Icons.Filled.Widgets,
          contentDescription = null,
          tint = EmeraldPrimary,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isAr) "إضافة ويدجت مواقيت الصلاة للشاشة الرئيسية" else "Pin Prayer Times Widget to Home Screen",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = EmeraldPrimary
        )
      }
    }
  }
}
