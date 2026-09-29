package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizationManager
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
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
  onLanguageChange: (AppLanguage) -> Unit,
  onSoundToggle: (Boolean) -> Unit,
  onMusicToggle: (Boolean) -> Unit,
  onVibrationToggle: (Boolean) -> Unit,
  onSoundVolumeChange: (Float) -> Unit,
  onDarkModeToggle: (Boolean?) -> Unit,
  onNotificationsToggle: (Boolean) -> Unit,
  onInAppBannerToggle: (Boolean) -> Unit,
  onSendTestNotification: () -> Unit,
  onResetProgress: () -> Unit,
  onBack: () -> Unit
) {
  val strings = LocalizationManager.get(currentLanguage)
  var showResetDialog by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    onNotificationsToggle(isGranted)
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
          IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
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
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
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
            // Sound Effects
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

            // Sound Volume Slider
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

            // Background Music
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

            // Vibration
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

            // 1. External Notifications Toggle (outside app)
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

            // 2. In-App Notification Banner Toggle (Removed from inside app by default)
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

            if (notificationsEnabled) {
              Spacer(modifier = Modifier.height(14.dp))
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

      // 5. Religious Accuracy & Content Safety Notice
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

      // 5. About & Scholarly References Card
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
            Text(
              text = strings.version,
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

      // 6. Reset Progress Option
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
