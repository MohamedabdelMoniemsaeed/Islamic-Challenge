package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.audio.AudioFeedbackManager
import com.example.core.localization.AppLanguage
import com.example.core.localization.AppStrings
import com.example.core.localization.LocalizationManager
import com.example.data.local.AppDatabase
import com.example.data.local.CategoryProgressEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.models.Achievement
import com.example.data.models.AchievementsProvider
import com.example.data.models.DailyRewardDay
import com.example.data.models.LifelineType
import com.example.data.models.PlayerProfile
import com.example.data.repository.PlayerRepository
import com.example.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application)
  val playerRepository = PlayerRepository(database.playerDao())
  val questionRepository = QuestionRepository()
  val audioManager = AudioFeedbackManager(application)

  val playerProfile: StateFlow<PlayerProfile> = playerRepository.playerProfileFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = PlayerProfile()
    )

  val unlockedAchievements: StateFlow<Set<String>> = playerRepository.unlockedAchievementsFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptySet()
    )

  val categoryProgress: StateFlow<Map<String, CategoryProgressEntity>> = playerRepository.categoryProgressFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyMap()
    )

  private val _language = MutableStateFlow(AppLanguage.ARABIC)
  val language: StateFlow<AppLanguage> = _language.asStateFlow()

  val strings: AppStrings
    get() = LocalizationManager.get(_language.value)

  private val _soundEnabled = MutableStateFlow(true)
  val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

  private val _musicEnabled = MutableStateFlow(false)
  val musicEnabled: StateFlow<Boolean> = _musicEnabled.asStateFlow()

  private val _vibrationEnabled = MutableStateFlow(true)
  val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

  private val _soundVolume = MutableStateFlow(0.8f)
  val soundVolume: StateFlow<Float> = _soundVolume.asStateFlow()

  private val _isDarkMode = MutableStateFlow<Boolean?>(null) // null = system
  val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

  private val _claimedRewardMessage = MutableStateFlow<DailyRewardDay?>(null)
  val claimedRewardMessage: StateFlow<DailyRewardDay?> = _claimedRewardMessage.asStateFlow()

  init {
    viewModelScope.launch {
      playerRepository.getOrCreateProfile()
      val settings = playerRepository.getOrCreateSettings()
      _soundEnabled.value = settings.soundEnabled
      _musicEnabled.value = settings.musicEnabled
      _vibrationEnabled.value = settings.vibrationEnabled
      _soundVolume.value = settings.soundVolume
      _language.value = settings.toLanguage()
      _isDarkMode.value = settings.toDarkModeBoolean()
      audioManager.updateVolume(settings.soundVolume)
    }
  }

  fun setLanguage(lang: AppLanguage) {
    _language.value = lang
    audioManager.playClickFeedback(_soundEnabled.value, _vibrationEnabled.value, _soundVolume.value)
    persistSettings()
  }

  fun toggleSound(enabled: Boolean) {
    _soundEnabled.value = enabled
    persistSettings()
  }

  fun toggleMusic(enabled: Boolean) {
    _musicEnabled.value = enabled
    persistSettings()
  }

  fun toggleVibration(enabled: Boolean) {
    _vibrationEnabled.value = enabled
    persistSettings()
  }

  fun setSoundVolume(volume: Float) {
    val clamped = volume.coerceIn(0.1f, 1.0f)
    _soundVolume.value = clamped
    audioManager.updateVolume(clamped)
    persistSettings()
  }

  fun setDarkMode(dark: Boolean?) {
    _isDarkMode.value = dark
    persistSettings()
  }

  private fun persistSettings() {
    viewModelScope.launch {
      val entity = UserSettingsEntity.fromPreferences(
        sound = _soundEnabled.value,
        music = _musicEnabled.value,
        vibration = _vibrationEnabled.value,
        volume = _soundVolume.value,
        darkMode = _isDarkMode.value,
        language = _language.value
      )
      playerRepository.updateUserSettings(entity)
    }
  }

  fun claimDailyReward() {
    viewModelScope.launch {
      val reward = playerRepository.claimDailyReward()
      if (reward != null) {
        _claimedRewardMessage.value = reward
        audioManager.playLevelUpFeedback(_soundEnabled.value, _vibrationEnabled.value, _soundVolume.value)
      }
    }
  }

  fun dismissRewardMessage() {
    _claimedRewardMessage.value = null
  }

  fun purchaseLifeline(type: LifelineType) {
    viewModelScope.launch {
      val success = playerRepository.purchaseLifeline(type)
      if (success) {
        audioManager.playClickFeedback(_soundEnabled.value, _vibrationEnabled.value, _soundVolume.value)
      }
    }
  }

  fun completeOnboarding() {
    viewModelScope.launch {
      playerRepository.completeOnboarding()
    }
  }

  fun resetAllProgress() {
    viewModelScope.launch {
      playerRepository.resetAllProgress()
    }
  }

  fun getDisplayAchievements(unlockedIds: Set<String>): List<Achievement> {
    return AchievementsProvider.allAchievements.map { ach ->
      ach.copy(isUnlocked = unlockedIds.contains(ach.id))
    }
  }
}
