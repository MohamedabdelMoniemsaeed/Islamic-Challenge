package com.example.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.adhan.AdhanPlaybackState
import com.example.core.adhan.AdhanPlayerManager
import com.example.core.adhan.AdhanPlayerUiState
import com.example.core.adhan.CityLocation
import com.example.core.adhan.MuadhinAudio
import com.example.core.adhan.PrayerName
import com.example.core.adhan.PrayerTimeItem
import com.example.core.adhan.PrayerTimesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

data class PrayerTimesScreenUiState(
  val selectedCity: CityLocation = PrayerTimesManager.CITIES.first { it.id == "cairo" },
  val prayerTimes: List<PrayerTimeItem> = emptyList(),
  val nextPrayer: PrayerTimeItem? = null,
  val countdownString: String = "",
  val hijriDateString: String = "",
  val gregorianDateString: String = "",
  val showCityPicker: Boolean = false,
  val adhanPlayerState: AdhanPlayerUiState = AdhanPlayerUiState(),
  val prayerAlerts: Map<PrayerName, Boolean> = PrayerName.values().associateWith { true }
)

class PrayerTimesViewModel(application: Application) : AndroidViewModel(application) {

  private val prefs = application.getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
  private val playerManager = AdhanPlayerManager(application)

  private val _uiState = MutableStateFlow(PrayerTimesScreenUiState())
  val uiState: StateFlow<PrayerTimesScreenUiState> = _uiState.asStateFlow()

  init {
    loadSavedCity()
    refreshTimes()
    startCountdownTicker()
    observePlayerState()
  }

  private fun loadSavedCity() {
    val savedCityId = prefs.getString("selected_city_id", "cairo") ?: "cairo"
    val city = PrayerTimesManager.CITIES.find { it.id == savedCityId } ?: PrayerTimesManager.CITIES.first()
    _uiState.value = _uiState.value.copy(selectedCity = city)
  }

  fun selectCity(city: CityLocation) {
    prefs.edit().putString("selected_city_id", city.id).apply()
    _uiState.value = _uiState.value.copy(
      selectedCity = city,
      showCityPicker = false
    )
    refreshTimes()
  }

  fun toggleCityPicker(show: Boolean) {
    _uiState.value = _uiState.value.copy(showCityPicker = show)
  }

  fun togglePrayerAlert(prayer: PrayerName) {
    val currentAlerts = _uiState.value.prayerAlerts.toMutableMap()
    val newValue = !(currentAlerts[prayer] ?: true)
    currentAlerts[prayer] = newValue
    prefs.edit().putBoolean("alert_${prayer.name}", newValue).apply()
    _uiState.value = _uiState.value.copy(prayerAlerts = currentAlerts)
  }

  fun refreshTimes() {
    val city = _uiState.value.selectedCity
    val now = Calendar.getInstance()
    val list = PrayerTimesManager.calculatePrayerTimes(city, now)
    val next = list.find { it.isNext } ?: list.firstOrNull()

    val gregorianFormat = java.text.SimpleDateFormat("EEEE d MMMM yyyy", Locale("ar"))
    val gregorianDate = gregorianFormat.format(now.time)

    _uiState.value = _uiState.value.copy(
      prayerTimes = list,
      nextPrayer = next,
      gregorianDateString = gregorianDate
    )
    updateCountdown()
  }

  private fun startCountdownTicker() {
    viewModelScope.launch {
      while (isActive) {
        updateCountdown()
        delay(1000)
      }
    }
  }

  private fun updateCountdown() {
    val next = _uiState.value.nextPrayer ?: return
    val nowMillis = System.currentTimeMillis()
    var diff = next.timestampMillis - nowMillis

    // If next prayer is in the past today, it means Fajr tomorrow
    if (diff <= 0) {
      diff += TimeUnit.DAYS.toMillis(1)
    }

    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

    val countdownText = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    _uiState.value = _uiState.value.copy(countdownString = countdownText)
  }

  private fun observePlayerState() {
    viewModelScope.launch {
      playerManager.uiState.collect { playerState ->
        _uiState.value = _uiState.value.copy(adhanPlayerState = playerState)
      }
    }
  }

  fun selectMuadhin(muadhin: MuadhinAudio) {
    playerManager.selectMuadhin(muadhin)
  }

  fun togglePlayAdhan() {
    val state = _uiState.value.adhanPlayerState.state
    if (state == AdhanPlaybackState.PLAYING) {
      playerManager.pause()
    } else {
      playerManager.playCurrent()
    }
  }

  fun stopAdhan() {
    playerManager.stop()
  }

  override fun onCleared() {
    super.onCleared()
    playerManager.release()
  }
}
