package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.BuildConfig
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizationManager
import com.example.core.permissions.DeviceAccessManager
import com.example.core.widgets.HomeScreenWidgetHelper
import com.example.core.widgets.HomeShortcutTarget
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  currentLanguage: AppLanguage,
  soundEnabled: Boolean,
  musicEnabled: Boolean,
  vibrationEnabled: Boolean,
  soundVolume: Float,
  isDarkMode: Boolean?,
  notificationsEnabled: Boolean,
  showInAppBanner: Boolean,
  hourlySalawatEnabled: Boolean = true,
  currentLocationLabel: String = "",
  isDetectingGps: Boolean = false,
  onDetectGpsLocation: ((onMessage: (String) -> Unit) -> Unit)? = null,
  onRefreshPermissions: () -> Unit = {},
  onLanguageChange: (AppLanguage) -> Unit,
  onSoundToggle: (Boolean) -> Unit,
  onMusicToggle: (Boolean) -> Unit,
  onVibrationToggle: (Boolean) -> Unit,
  onSoundVolumeChange: (Float) -> Unit,
  onDarkModeToggle: (Boolean?) -> Unit,
  onNotificationsToggle: (Boolean) -> Unit,
  onInAppBannerToggle: (Boolean) -> Unit,
  onHourlySalawatToggle: (Boolean) -> Unit = {},
  onSendTestNotification: () -> Unit,
  onSendTestSalawat: () -> Unit = {},
  onResetProgress: () -> Unit,
  onBack: () -> Unit
) {
  val isAr = currentLanguage == AppLanguage.ARABIC
  val strings = LocalizationManager.get(currentLanguage)
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  var showResetDialog by remember { mutableStateOf(false) }
  var showPrivacyDialog by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  var deviceAccessStatus by remember { mutableStateOf(DeviceAccessManager.getStatus(context)) }

  // Live connectivity updates while user stays on SettingsScreen
  LaunchedEffect(context) {
    DeviceAccessManager.observeConnectivity(context).collect {
      deviceAccessStatus = DeviceAccessManager.getStatus(context)
    }
  }

  // Refresh all permission states immediately when returning from Android Settings
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        deviceAccessStatus = DeviceAccessManager.getStatus(context)
        onRefreshPermissions()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    deviceAccessStatus = DeviceAccessManager.getStatus(context)
    onRefreshPermissions()
    onNotificationsToggle(isGranted)
  }

  val salawatPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    deviceAccessStatus = DeviceAccessManager.getStatus(context)
    onRefreshPermissions()
    onHourlySalawatToggle(isGranted)
  }

  val locationPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    deviceAccessStatus = DeviceAccessManager.getStatus(context)
    onRefreshPermissions()
    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (granted) {
      if (deviceAccessStatus.isDeviceGpsEnabled) {
        onDetectGpsLocation?.invoke { msg ->
          scope.launch { snackbarHostState.showSnackbar(msg) }
        }
      } else {
        scope.launch {
          snackbarHostState.showSnackbar(
            if (isAr) "تم منح إذن الموقع؛ يرجى تشغيل خدمة GPS في الهاتف"
            else "GPS permission granted; please turn on phone Location/GPS"
          )
        }
      }
    } else {
      scope.launch {
        snackbarHostState.showSnackbar(
          if (isAr) "تم رفض إذن الموقع الجغرافي؛ يمكنك تفعيله من إعدادات التطبيق"
          else "GPS permission denied; you can enable it from App Settings"
        )
      }
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = strings.settingsTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier
              .size(48.dp)
              .testTag("settings_back_btn")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = if (isAr) "رجوع" else "Back"
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
          .testTag("settings_screen_body"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // 1. Language Setting
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Language, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = strings.language,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                )
              }

              Spacer(modifier = Modifier.height(14.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                LanguageChoiceButton(
                  label = "العربية (Arabic)",
                  isSelected = currentLanguage == AppLanguage.ARABIC,
                  modifier = Modifier.weight(1f).testTag("lang_ar_btn"),
                  onClick = { onLanguageChange(AppLanguage.ARABIC) }
                )
                LanguageChoiceButton(
                  label = "English",
                  isSelected = currentLanguage == AppLanguage.ENGLISH,
                  modifier = Modifier.weight(1f).testTag("lang_en_btn"),
                  onClick = { onLanguageChange(AppLanguage.ENGLISH) }
                )
              }
            }
          }
        }

        // 2. Audio & Haptics
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = EmeraldPrimary)
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(text = strings.soundEffects, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }
                Switch(
                  checked = soundEnabled,
                  onCheckedChange = onSoundToggle,
                  colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EmeraldPrimary),
                  modifier = Modifier.testTag("sound_toggle_switch")
                )
              }

              if (soundEnabled) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = strings.soundVolume,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                      text = "${(soundVolume * 100).toInt()}%",
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.Bold,
                      color = EmeraldPrimary
                    )
                  }
                  Slider(
                    value = soundVolume,
                    onValueChange = onSoundVolumeChange,
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(
                      thumbColor = EmeraldPrimary,
                      activeTrackColor = EmeraldPrimary
                    ),
                    modifier = Modifier.testTag("sound_volume_slider")
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Filled.MusicNote, contentDescription = null, tint = EmeraldPrimary)
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(text = strings.backgroundMusic, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }
                Switch(
                  checked = musicEnabled,
                  onCheckedChange = onMusicToggle,
                  colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EmeraldPrimary),
                  modifier = Modifier.testTag("music_toggle_switch")
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Filled.Vibration, contentDescription = null, tint = EmeraldPrimary)
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(text = strings.vibration, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }
                Switch(
                  checked = vibrationEnabled,
                  onCheckedChange = onVibrationToggle,
                  colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EmeraldPrimary),
                  modifier = Modifier.testTag("vibration_toggle_switch")
                )
              }
            }
          }
        }

        // 3. Theme Mode Selector
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.DarkMode, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = strings.darkMode,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                ThemeOptionButton(
                  label = strings.themeModeSystem,
                  isSelected = isDarkMode == null,
                  modifier = Modifier.weight(1f).testTag("theme_system_btn"),
                  onClick = { onDarkModeToggle(null) }
                )
                ThemeOptionButton(
                  label = strings.themeModeLight,
                  isSelected = isDarkMode == false,
                  modifier = Modifier.weight(1f).testTag("theme_light_btn"),
                  onClick = { onDarkModeToggle(false) }
                )
                ThemeOptionButton(
                  label = strings.themeModeDark,
                  isSelected = isDarkMode == true,
                  modifier = Modifier.weight(1f).testTag("theme_dark_btn"),
                  onClick = { onDarkModeToggle(true) }
                )
              }
            }
          }
        }

        // 4. External Notifications Settings (Outside App)
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("notifications_settings_card")
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Filled.Notifications,
                  contentDescription = null,
                  tint = EmeraldPrimary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = strings.notificationsTitle,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                )
              }

              Spacer(modifier = Modifier.height(14.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                  Text(
                    text = strings.notificationsEnabled,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                  )
                  Text(
                    text = strings.notificationsDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                  )
                }
                Switch(
                  checked = notificationsEnabled,
                  onCheckedChange = { isChecked ->
                    if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                      permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                      onNotificationsToggle(isChecked)
                    }
                  },
                  colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = EmeraldPrimary
                  ),
                  modifier = Modifier.testTag("notifications_toggle_switch")
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                  Text(
                    text = strings.inAppBannerOption,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                  )
                  Text(
                    text = strings.inAppBannerDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                  )
                }
                Switch(
                  checked = showInAppBanner,
                  onCheckedChange = onInAppBannerToggle,
                  colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = EmeraldPrimary
                  ),
                  modifier = Modifier.testTag("in_app_banner_toggle_switch")
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                  Text(
                    text = strings.hourlySalawatTitle,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                  )
                  Text(
                    text = strings.hourlySalawatDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                  )
                }
                Switch(
                  checked = hourlySalawatEnabled,
                  onCheckedChange = { isChecked ->
                    if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                      salawatPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                      onHourlySalawatToggle(isChecked)
                    }
                  },
                  colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = EmeraldPrimary
                  ),
                  modifier = Modifier.testTag("hourly_salawat_toggle_switch")
                )
              }

              if (hourlySalawatEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                  onClick = {
                    onSendTestSalawat()
                    scope.launch {
                      snackbarHostState.showSnackbar(strings.testSalawatSent)
                    }
                  },
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("test_salawat_btn")
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = strings.testSalawatBtn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = EmeraldPrimary
                  )
                }
              }

              if (notificationsEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                  onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                      permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                    onSendTestNotification()
                    scope.launch {
                      snackbarHostState.showSnackbar(strings.testNotificationSent)
                    }
                  },
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("send_test_notification_btn")
                ) {
                  Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = strings.sendTestNotification,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                }
              }
            }
          }
        }

        // 4b. Permissions & Device Readiness Hub (GPS, Internet, Notifications, Exact Alarms)
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("permissions_hub_card")
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Security, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = if (isAr) "أذونات الجهاز والاتصال" else "Permissions & Device Access",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                  )
                  Text(
                    text = if (isAr) {
                      "تحكم في إذن GPS والإنترنت والإشعارات والمنبّه مباشرة من هنا"
                    } else {
                      "Manage GPS location, Internet, Notifications & Exact Alarms in one place"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

              // 1) GPS Location Access
              PermissionHubRow(
                icon = Icons.Filled.MyLocation,
                title = if (isAr) "الموقع الجغرافي (GPS)" else "GPS Location Access",
                subtitle = when {
                  isDetectingGps -> if (isAr) "جاري تحديد موقعك..." else "Detecting location..."
                  !deviceAccessStatus.hasLocationPermission -> if (isAr) {
                    "لم يُمنح الإذن بعد • يُستخدم لدقة مواقيت الصلاة والقبلة"
                  } else {
                    "Permission needed • Used for accurate Prayer Times & Qiblah"
                  }
                  !deviceAccessStatus.isDeviceGpsEnabled -> if (isAr) {
                    "الإذن ممنوح لكن خدمة GPS مغلقة في الهاتف"
                  } else {
                    "Permission granted, but device GPS is turned off"
                  }
                  else -> if (isAr) {
                    "مفعّل" + if (currentLocationLabel.isNotBlank()) " • $currentLocationLabel" else ""
                  } else {
                    "Active" + if (currentLocationLabel.isNotBlank()) " • $currentLocationLabel" else ""
                  }
                },
                isReady = deviceAccessStatus.isGpsReady,
                statusText = when {
                  deviceAccessStatus.isGpsReady -> if (isAr) "مُفعّل" else "Ready"
                  deviceAccessStatus.hasLocationPermission -> if (isAr) "GPS مغلق" else "GPS Off"
                  else -> if (isAr) "مطلوب" else "Required"
                },
                buttonLabel = when {
                  isDetectingGps -> if (isAr) "جاري التحديد..." else "Locating..."
                  !deviceAccessStatus.hasLocationPermission -> if (isAr) "منح إذن GPS" else "Allow GPS"
                  !deviceAccessStatus.isDeviceGpsEnabled -> if (isAr) "تشغيل GPS" else "Turn On GPS"
                  else -> if (isAr) "تحديث الموقع" else "Update GPS"
                },
                isLoading = isDetectingGps,
                testTag = "perm_hub_gps_btn",
                onActionClick = {
                  when {
                    !deviceAccessStatus.hasLocationPermission -> {
                      locationPermissionLauncher.launch(
                        arrayOf(
                          Manifest.permission.ACCESS_COARSE_LOCATION,
                          Manifest.permission.ACCESS_FINE_LOCATION
                        )
                      )
                    }
                    !deviceAccessStatus.isDeviceGpsEnabled -> {
                      DeviceAccessManager.openLocationSourceSettings(context)
                    }
                    else -> {
                      onDetectGpsLocation?.invoke { msg ->
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                      }
                    }
                  }
                }
              )

              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

              // 2) Internet Connectivity
              PermissionHubRow(
                icon = if (deviceAccessStatus.isInternetConnected) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                title = if (isAr) "الاتصال بالإنترنت" else "Internet Connection",
                subtitle = if (deviceAccessStatus.isInternetConnected) {
                  if (isAr) "${deviceAccessStatus.internetLabel(true)} • بث وتحميل صوت الأذان متاح"
                  else "${deviceAccessStatus.internetLabel(false)} • Adhan streaming & downloads ready"
                } else {
                  if (isAr) "غير متصل • الحسابات والأذكار ونغمة المسجد تعمل بدون نت"
                  else "Offline • Prayer times, Adhkar & offline chime work without internet"
                },
                isReady = deviceAccessStatus.isInternetConnected,
                statusText = if (deviceAccessStatus.isInternetConnected) {
                  if (isAr) "متصل" else "Online"
                } else {
                  if (isAr) "غير متصل" else "Offline"
                },
                buttonLabel = if (isAr) "إعداد الإنترنت" else "Net Settings",
                testTag = "perm_hub_internet_btn",
                onActionClick = { DeviceAccessManager.openInternetSettings(context) }
              )

              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

              // 3) Notifications Permission
              PermissionHubRow(
                icon = Icons.Filled.Notifications,
                title = if (isAr) "الوصول إلى الإشعارات" else "Notifications Access",
                subtitle = if (deviceAccessStatus.hasNotificationPermission) {
                  if (isAr) "مفعّل لإشعارات الصلاة والتذكير اليومي والصلاة على النبي ﷺ"
                  else "Enabled for Adhan, daily reminders & hourly Salawat alerts"
                } else {
                  if (isAr) "غير مفعّل • لن تظهر إشعارات الأذان والتذكير في الشريط"
                  else "Disabled • Adhan & reminder alerts will not appear"
                },
                isReady = deviceAccessStatus.hasNotificationPermission,
                statusText = if (deviceAccessStatus.hasNotificationPermission) {
                  if (isAr) "مُفعّل" else "Allowed"
                } else {
                  if (isAr) "مغلق" else "Blocked"
                },
                buttonLabel = if (!deviceAccessStatus.hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                  if (isAr) "تفعيل الإذن" else "Allow"
                } else {
                  if (isAr) "فتح الإعدادات" else "Settings"
                },
                testTag = "perm_hub_notifications_btn",
                onActionClick = {
                  if (!deviceAccessStatus.hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  } else {
                    DeviceAccessManager.openAppNotificationSettings(context)
                  }
                }
              )

              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

              // 4) Exact Alarms Permission
              PermissionHubRow(
                icon = Icons.Filled.Alarm,
                title = if (isAr) "إذن المنبّهات الدقيقة" else "Exact Alarms & Reminders",
                subtitle = if (deviceAccessStatus.canScheduleExactAlarms) {
                  if (isAr) "مفعّل لتشغيل الأذان في وقته و«صلِّ على محمد» عند الدقيقة ٠٠"
                  else "Enabled for exact Prayer Adhan and top-of-hour (:00) Salawat"
                } else {
                  if (isAr) "مطلوب لتشغيل الأذان والمنبّه عند الدقيقة المحددة والتطبيق مغلق"
                  else "Required for exact background Adhan and hourly alarms when closed"
                },
                isReady = deviceAccessStatus.canScheduleExactAlarms,
                statusText = if (deviceAccessStatus.canScheduleExactAlarms) {
                  if (isAr) "مُفعّل" else "Allowed"
                } else {
                  if (isAr) "مطلوب" else "Required"
                },
                buttonLabel = if (isAr) "إعداد المنبّه" else "Alarm Access",
                testTag = "perm_hub_alarm_btn",
                onActionClick = { DeviceAccessManager.openExactAlarmSettings(context) }
              )

              if (!deviceAccessStatus.isIgnoringBatteryOptimizations) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                PermissionHubRow(
                  icon = Icons.Filled.BatteryChargingFull,
                  title = if (isAr) "تحسين البطارية في الخلفية" else "Battery Optimization",
                  subtitle = if (isAr) {
                    "بعض الهواتف قد تؤخر الأذان إذا كان توفير البطارية مقيّداً للتطبيق"
                  } else {
                    "Allow unrestricted battery so the OS does not delay background Adhan"
                  },
                  isReady = false,
                  statusText = if (isAr) "مُقيّد" else "Optimized",
                  buttonLabel = if (isAr) "ضبط البطارية" else "Battery",
                  testTag = "perm_hub_battery_btn",
                  onActionClick = { DeviceAccessManager.openBatteryOptimizationSettings(context) }
                )
              }
            }
          }
        }

        // 4c. Home Screen Mini Icons & Widgets (Outside App)
        item {
          val showPinFeedback: (Boolean, String) -> Unit = { accepted, label ->
            scope.launch {
              snackbarHostState.showSnackbar(
                if (accepted) {
                  if (isAr) "أكّد الإضافة من نافذة شاشة الهاتف لوضع «$label» على الشاشة الرئيسية"
                  else "Confirm the launcher prompt to place \"$label\" on your Home Screen"
                } else {
                  if (isAr) "اضغط مطولاً على شاشة الهاتف ← الأدوات (Widgets) ← تحدي المعرفة الإسلامية لإضافة «$label»"
                  else "Long-press your phone Home Screen -> Widgets -> Islamic Challenge to add \"$label\""
                }
              )
            }
          }

          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("home_widgets_card")
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Widgets, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = if (isAr) "أيقونات وويدجت الشاشة الرئيسية" else "Home Screen Widgets & Mini Icons",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                  )
                  Text(
                    text = if (isAr) {
                      "أضف مواقيت الصلاة (مع العدّ التنازلي والشروق والقبلة) وأيقونات مصغّرة مرنة قابلة للتكبير والتصغير على شاشة الهاتف"
                    } else {
                      "Pin flexible resizable Prayer Times (with live countdown, Sunrise & Qiblah) and mini shortcut icons to your Home Screen"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

              Button(
                onClick = {
                  val label = if (isAr) "ويدجت مواقيت الصلاة" else "Prayer Times Widget"
                  val ok = HomeScreenWidgetHelper.requestPinPrayerTimesWidget(context)
                  showPinFeedback(ok, label)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("pin_prayer_widget_btn")
              ) {
                Icon(imageVector = Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isAr) "إضافة ويدجت مواقيت الصلاة للشاشة الرئيسية" else "Add Prayer Times Widget to Home Screen",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
              }

              OutlinedButton(
                onClick = {
                  val label = if (isAr) "شريط الاختصارات المصغّرة" else "Quick Shortcuts Widget"
                  val ok = HomeScreenWidgetHelper.requestPinShortcutsWidget(context)
                  showPinFeedback(ok, label)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("pin_shortcuts_widget_btn")
              ) {
                Icon(
                  imageVector = Icons.Filled.AddToHomeScreen,
                  contentDescription = null,
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isAr) "إضافة شريط الاختصارات المصغّرة (٤ أيقونات)" else "Add Quick 4-Icon Shortcuts Widget",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = EmeraldPrimary
                )
              }

              Text(
                text = if (isAr) "أو أضف أيقونة مصغّرة مستقلة (١×١):" else "Or pin a standalone 1×1 mini icon:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              val targets = HomeShortcutTarget.entries
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                targets.take(2).forEach { target ->
                  val iconVec = when (target) {
                    HomeShortcutTarget.PRAYER_TIMES -> Icons.Filled.AccessTime
                    HomeShortcutTarget.QIBLAH -> Icons.Filled.Explore
                    HomeShortcutTarget.ADHKAR_TASBIH -> Icons.Filled.AutoAwesome
                    HomeShortcutTarget.QUIZ_CHALLENGE -> Icons.Filled.Category
                  }
                  val label = if (isAr) target.labelAr else target.labelEn
                  OutlinedButton(
                    onClick = {
                      val ok = HomeScreenWidgetHelper.requestPinShortcutIcon(context, target, isAr)
                      showPinFeedback(ok, label)
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                      .weight(1f)
                      .height(48.dp)
                      .testTag("pin_shortcut_${target.id}_btn")
                  ) {
                    Icon(imageVector = iconVec, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = label,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                targets.drop(2).forEach { target ->
                  val iconVec = when (target) {
                    HomeShortcutTarget.PRAYER_TIMES -> Icons.Filled.AccessTime
                    HomeShortcutTarget.QIBLAH -> Icons.Filled.Explore
                    HomeShortcutTarget.ADHKAR_TASBIH -> Icons.Filled.AutoAwesome
                    HomeShortcutTarget.QUIZ_CHALLENGE -> Icons.Filled.Category
                  }
                  val label = if (isAr) target.labelAr else target.labelEn
                  OutlinedButton(
                    onClick = {
                      val ok = HomeScreenWidgetHelper.requestPinShortcutIcon(context, target, isAr)
                      showPinFeedback(ok, label)
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                      .weight(1f)
                      .height(48.dp)
                      .testTag("pin_shortcut_${target.id}_btn")
                  ) {
                    Icon(imageVector = iconVec, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = label,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }
            }
          }
        }

        // 5. Privacy Policy & Share App
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              OutlinedButton(
                onClick = { showPrivacyDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("privacy_policy_btn")
              ) {
                Icon(imageVector = Icons.Filled.PrivacyTip, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isAr) "سياسة الخصوصية وأمان البيانات" else "Privacy Policy & Data Safety",
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              Button(
                onClick = {
                  val shareText = if (isAr) {
                    "جرّب تطبيق التحدي الإسلامي — مسابقات دينية موثوقة، مواقيت الصلاة، الأذان، اتجاه القبلة والأذكار اليومية:\nhttps://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
                  } else {
                    "Try Islamic Challenge — verified Islamic quizzes, Prayer Times, Adhan, Qiblah & daily Adhkar:\nhttps://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
                  }
                  val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                  }
                  context.startActivity(
                    Intent.createChooser(shareIntent, if (isAr) "مشاركة التطبيق" else "Share App")
                  )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("share_app_btn")
              ) {
                Icon(imageVector = Icons.Filled.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isAr) "مشاركة التطبيق مع الأهل والأصدقاء" else "Share App with Family & Friends",
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        // 6. Religious Accuracy & Content Safety Notice
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Info, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = strings.contentSafetyNotice,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = strings.contentSafetyDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
              )
            }
          }
        }

        // 7. About & Scholarly References Card (Dynamic BuildConfig.VERSION_NAME)
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "🕌 ${strings.aboutApp}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Spacer(modifier = Modifier.height(6.dp))
              val versionText = if (isAr) {
                "الإصدار ${BuildConfig.VERSION_NAME} (أندرويد أصيل وCompose)"
              } else {
                "Version ${BuildConfig.VERSION_NAME} (Native Android & Compose)"
              }
              Text(
                text = versionText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = strings.credits,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = EmeraldDark
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = strings.creditsDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
              )
            }
          }
        }

        // 8. Reset Progress Option
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showResetDialog = true }
              .testTag("reset_progress_card")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.RestartAlt, contentDescription = null, tint = Color(0xFFEF4444))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = strings.resetProgress,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFFEF4444),
                  fontSize = 15.sp
                )
              }
            }
          }
        }

        item {
          Spacer(modifier = Modifier.height(20.dp))
        }
      }
    }
  }

  // Reset Confirmation Alert Dialog
  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      title = {
        Text(
          text = strings.resetConfirmTitle,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFEF4444)
        )
      },
      text = {
        Text(
          text = strings.resetConfirmDesc,
          fontSize = 14.sp,
          lineHeight = 20.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showResetDialog = false
            onResetProgress()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
          modifier = Modifier.testTag("confirm_reset_btn")
        ) {
          Text(text = strings.confirm, color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetDialog = false }) {
          Text(text = strings.cancel)
        }
      }
    )
  }

  // Privacy Policy & Data Safety Disclosure Dialog
  if (showPrivacyDialog) {
    AlertDialog(
      onDismissRequest = { showPrivacyDialog = false },
      title = {
        Text(
          text = if (isAr) "سياسة الخصوصية وأمان البيانات" else "Privacy Policy & Data Safety",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(
          text = if (isAr) {
            "• يحفظ التطبيق تقدمك وإحصائياتك وتفضيلاتك محلياً على جهازك فقط ولا ينشئ حسابات أو يجمع أسماء أو أرقام هواتف.\n\n" +
              "• إذن الموقع الجغرافي (GPS) اختياري تماماً ويُستخدم داخل التطبيق فقط لتحديد مدينتك بدقة وحساب مواقيت الصلاة واتجاه القبلة محلياً على جهازك دون إرسال موقعك لأي خوادم، وإذن المنبهات الدقيقة والإشعارات يُستخدم لتنبيهات الصلاة و«صلِّ على محمد ﷺ».\n\n" +
              "• لا يستخدم التطبيق معرّف الإعلانات (Advertising ID)؛ وتظهر إعلانات Google AdMob بصورة غير مزعجة وفق سياسات Google Play."
          } else {
            "• Your quiz progress, Tasbih counters, and preferences are stored strictly locally on your device. No personal account, name, or phone number is collected.\n\n" +
              "• Location permission (GPS) is optional and used strictly in the foreground on your device to detect coordinates for Prayer Times and Qiblah direction without storing or sharing location on servers. Notifications and Exact Alarms are used for Prayer Adhan and Salawat reminders.\n\n" +
              "• This app does not use the Advertising ID (AD_ID). Contextual Google AdMob ads follow Google Play and AdMob UX policies."
          },
          fontSize = 13.sp,
          lineHeight = 20.sp
        )
      },
      confirmButton = {
        TextButton(onClick = { showPrivacyDialog = false }) {
          Text(text = strings.gotIt, fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}

@Composable
private fun LanguageChoiceButton(
  label: String,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  if (isSelected) {
    Button(
      onClick = onClick,
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
      modifier = modifier
    ) {
      Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
  } else {
    OutlinedButton(
      onClick = onClick,
      shape = RoundedCornerShape(12.dp),
      modifier = modifier
    ) {
      Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
    }
  }
}

@Composable
private fun ThemeOptionButton(
  label: String,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  if (isSelected) {
    Button(
      onClick = onClick,
      shape = RoundedCornerShape(10.dp),
      colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
      modifier = modifier
    ) {
      Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
  } else {
    OutlinedButton(
      onClick = onClick,
      shape = RoundedCornerShape(10.dp),
      modifier = modifier
    ) {
      Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
  }
}

@Composable
private fun PermissionHubRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  isReady: Boolean,
  statusText: String,
  buttonLabel: String,
  isLoading: Boolean = false,
  testTag: String,
  onActionClick: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f).padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (isReady) EmeraldPrimary else MaterialTheme.colorScheme.error,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = title,
          fontWeight = FontWeight.SemiBold,
          fontSize = 14.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isReady) EmeraldPrimary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
      ) {
        Text(
          text = statusText,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = if (isReady) EmeraldPrimary else MaterialTheme.colorScheme.error,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 17.sp,
        modifier = Modifier.weight(1f).padding(end = 10.dp)
      )
      OutlinedButton(
        onClick = onActionClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier
          .height(48.dp)
          .testTag(testTag)
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            color = EmeraldPrimary,
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp
          )
          Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
          text = buttonLabel,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = EmeraldPrimary
        )
      }
    }
  }
}
