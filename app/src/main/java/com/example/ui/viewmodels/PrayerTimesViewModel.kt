package com.example.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.adhan.AdhanPlaybackState
import com.example.core.adhan.AdhanPlayerManager
import com.example.core.adhan.AdhanPlayerUiState
import com.example.core.adhan.AsrJuristicMethod
import com.example.core.adhan.CalculationMethod
import com.example.core.adhan.CityLocation
import com.example.core.adhan.GpsLocationResolver
import com.example.core.adhan.GpsLocationResult
import com.example.core.adhan.MuadhinAudio
import com.example.core.adhan.PrayerName
import com.example.core.adhan.PrayerTimeItem
import com.example.core.adhan.PrayerTimesManager
import com.example.core.adhan.QiblahCalculator
import com.example.core.notification.AppNotificationManager
import com.example.core.permissions.DeviceAccessManager
import com.example.core.permissions.DeviceAccessStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class PrayerTimesScreenUiState(
  val selectedCity: CityLocation = PrayerTimesManager.CITIES.first { it.id == "cairo" },
  val calculationMethod: CalculationMethod = CalculationMethod.EGYPT,
  val asrMethod: AsrJuristicMethod = AsrJuristicMethod.SHAFI,
  val prayerTimes: List<PrayerTimeItem> = emptyList(),
  val nextPrayer: PrayerTimeItem? = null,
  val countdownString: String = "00:00:00",
  val hijriDateString: String = "",
  val gregorianDateString: String = "",
  val qiblahBearingDegrees: Double = 136.0,
  val showCityPicker: Boolean = false,
  val showMethodPicker: Boolean = false,
  val adhanPlayerState: AdhanPlayerUiState = AdhanPlayerUiState(),
  val prayerAlerts: Map<PrayerName, Boolean> = PrayerName.entries.associateWith { true },
  val autoPlayAdhanAtPrayerTime: Boolean = true,
  val canScheduleExactAlarms: Boolean = true,
  val hasNotificationPermission: Boolean = true,
  val isDetectingGps: Boolean = false,
  val gpsStatusMessage: String? = null,
  val deviceAccessStatus: DeviceAccessStatus = DeviceAccessStatus()
) {
  val isUsingGpsLocation: Boolean
    get() = selectedCity.id == PrayerTimesManager.GPS_CITY_ID
}

class PrayerTimesViewModel(application: Application) : AndroidViewModel(application) {

  private val prefs = application.getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
  private val playerManager = AdhanPlayerManager(application)
  private val notificationManager = AppNotificationManager(application)
  private var currentIsArabic = true

  private val _uiState = MutableStateFlow(PrayerTimesScreenUiState())
  val uiState: StateFlow<PrayerTimesScreenUiState> = _uiState.asStateFlow()

  init {
    loadSavedPreferences()
    refreshTimes(isArabic = true)
    startCountdownTicker()
    observePlayerState()
    observeDeviceConnectivity()
  }

  private fun loadSavedPreferences() {
    val city = PrayerTimesManager.loadSavedCityLocation(prefs)

    val savedMethodId = prefs.getString("calculation_method_id", null)
    val method = CalculationMethod.entries.find { it.id == savedMethodId } ?: city.defaultMethod

    val savedAsrId = prefs.getString("asr_method_id", null)
    val asr = AsrJuristicMethod.entries.find { it.id == savedAsrId } ?: AsrJuristicMethod.SHAFI

    val savedAlerts = PrayerName.entries.associateWith { prayer ->
      prefs.getBoolean("alert_${prayer.name}", prayer != PrayerName.SUNRISE)
    }

    val savedMuadhinId = prefs.getString("selected_muadhin_id", null)
    val muadhin = PrayerTimesManager.MUADHINS.find { it.id == savedMuadhinId }
      ?: PrayerTimesManager.MUADHINS.first()
    playerManager.selectMuadhin(muadhin)

    val autoPlayAdhan = prefs.getBoolean("auto_play_adhan", true)
    val accessStatus = DeviceAccessManager.getStatus(getApplication())

    _uiState.value = _uiState.value.copy(
      selectedCity = city,
      calculationMethod = method,
      asrMethod = asr,
      prayerAlerts = savedAlerts,
      autoPlayAdhanAtPrayerTime = autoPlayAdhan,
      canScheduleExactAlarms = accessStatus.canScheduleExactAlarms,
      hasNotificationPermission = accessStatus.hasNotificationPermission,
      deviceAccessStatus = accessStatus
    )
  }

  fun selectCity(city: CityLocation) {
    PrayerTimesManager.saveCitySelection(prefs, city)
    _uiState.value = _uiState.value.copy(
      selectedCity = city,
      calculationMethod = city.defaultMethod,
      showCityPicker = false,
      gpsStatusMessage = null
    )
    refreshTimes(currentIsArabic)
  }

  fun selectCalculationMethod(method: CalculationMethod) {
    prefs.edit().putString("calculation_method_id", method.id).apply()
    _uiState.value = _uiState.value.copy(
      calculationMethod = method,
      showMethodPicker = false
    )
    refreshTimes(currentIsArabic)
  }

  fun selectAsrMethod(asr: AsrJuristicMethod) {
    prefs.edit().putString("asr_method_id", asr.id).apply()
    _uiState.value = _uiState.value.copy(asrMethod = asr)
    refreshTimes(currentIsArabic)
  }

  fun toggleCityPicker(show: Boolean) {
    _uiState.value = _uiState.value.copy(showCityPicker = show)
  }

  fun toggleMethodPicker(show: Boolean) {
    _uiState.value = _uiState.value.copy(showMethodPicker = show)
  }

  fun togglePrayerAlert(prayer: PrayerName) {
    val currentAlerts = _uiState.value.prayerAlerts.toMutableMap()
    val newValue = !(currentAlerts[prayer] ?: true)
    currentAlerts[prayer] = newValue
    prefs.edit().putBoolean("alert_${prayer.name}", newValue).apply()
    _uiState.value = _uiState.value.copy(prayerAlerts = currentAlerts)
    notificationManager.schedulePrayerAlerts(_uiState.value.prayerTimes, currentAlerts, currentIsArabic)
  }

  fun toggleAutoPlayAdhan(enabled: Boolean) {
    prefs.edit().putBoolean("auto_play_adhan", enabled).apply()
    _uiState.value = _uiState.value.copy(autoPlayAdhanAtPrayerTime = enabled)
    notificationManager.schedulePrayerAlerts(
      _uiState.value.prayerTimes,
      _uiState.value.prayerAlerts,
      currentIsArabic
    )
  }

  fun refreshBackgroundStatus() {
    val accessStatus = DeviceAccessManager.getStatus(getApplication())
    _uiState.value = _uiState.value.copy(
      canScheduleExactAlarms = accessStatus.canScheduleExactAlarms,
      hasNotificationPermission = accessStatus.hasNotificationPermission,
      deviceAccessStatus = accessStatus
    )
    if (_uiState.value.prayerTimes.isNotEmpty()) {
      notificationManager.schedulePrayerAlerts(
        _uiState.value.prayerTimes,
        _uiState.value.prayerAlerts,
        currentIsArabic
      )
    }
  }

  fun detectLocationWithGps(
    isArabic: Boolean = currentIsArabic,
    onResultMessage: ((String) -> Unit)? = null
  ) {
    if (_uiState.value.isDetectingGps) return
    currentIsArabic = isArabic
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isDetectingGps = true, gpsStatusMessage = null)
      val result = GpsLocationResolver.detectCurrentLocation(getApplication())
      val accessStatus = DeviceAccessManager.getStatus(getApplication())
      when (result) {
        is GpsLocationResult.Success -> {
          val gpsCity = result.cityLocation
          PrayerTimesManager.saveCitySelection(prefs, gpsCity)
          val msg = if (isArabic) {
            "تم تحديد موقعك عبر GPS: ${gpsCity.nameAr}"
          } else {
            "GPS location updated: ${gpsCity.nameEn}"
          }
          _uiState.value = _uiState.value.copy(
            selectedCity = gpsCity,
            calculationMethod = gpsCity.defaultMethod,
            showCityPicker = false,
            isDetectingGps = false,
            gpsStatusMessage = msg,
            deviceAccessStatus = accessStatus
          )
          refreshTimes(isArabic)
          onResultMessage?.invoke(msg)
        }
        GpsLocationResult.PermissionDenied -> {
          val msg = if (isArabic) {
            "يرجى السماح بإذن الموقع الجغرافي (GPS) أولاً"
          } else {
            "Please allow GPS location permission first"
          }
          _uiState.value = _uiState.value.copy(
            isDetectingGps = false,
            gpsStatusMessage = msg,
            deviceAccessStatus = accessStatus
          )
          onResultMessage?.invoke(msg)
        }
        GpsLocationResult.GpsDisabled -> {
          val msg = if (isArabic) {
            "خدمة الموقع (GPS) مغلقة في الهاتف؛ يرجى تفعيلها"
          } else {
            "Phone GPS/Location is turned off; please enable it"
          }
          _uiState.value = _uiState.value.copy(
            isDetectingGps = false,
            gpsStatusMessage = msg,
            deviceAccessStatus = accessStatus
          )
          onResultMessage?.invoke(msg)
        }
        GpsLocationResult.Unavailable -> {
          val msg = if (isArabic) {
            "تعذّر التقاط إشارة GPS حالياً؛ جرب في مكان مفتوح أو اختر المدينة يدوياً"
          } else {
            "Could not acquire GPS fix right now; try outdoors or pick city manually"
          }
          _uiState.value = _uiState.value.copy(
            isDetectingGps = false,
            gpsStatusMessage = msg,
            deviceAccessStatus = accessStatus
          )
          onResultMessage?.invoke(msg)
        }
      }
    }
  }

  private fun observeDeviceConnectivity() {
    viewModelScope.launch {
      DeviceAccessManager.observeConnectivity(getApplication()).collect { _ ->
        val updated = DeviceAccessManager.getStatus(getApplication())
        _uiState.value = _uiState.value.copy(deviceAccessStatus = updated)
      }
    }
  }

  fun refreshTimes(isArabic: Boolean = currentIsArabic) {
    currentIsArabic = isArabic
    val state = _uiState.value
    val city = state.selectedCity
    val nowMillis = System.currentTimeMillis()

    val schedule = PrayerTimesManager.calculateSchedule(
      city = city,
      method = state.calculationMethod,
      asrMethod = state.asrMethod,
      nowMillis = nowMillis
    )

    val locale = if (isArabic) Locale("ar") else Locale.US
    val gregorianFormat = SimpleDateFormat("EEEE d MMMM yyyy", locale).apply {
      timeZone = TimeZone.getTimeZone(city.timeZoneId)
    }
    val gregorianDate = gregorianFormat.format(Date(nowMillis))
    val hijriDate = PrayerTimesManager.getHijriDateString(
      isArabic = isArabic,
      timeZoneId = city.timeZoneId,
      nowMillis = nowMillis
    )
    val qiblahBearing = QiblahCalculator.calculateQiblahBearing(city.latitude, city.longitude)

    _uiState.value = state.copy(
      prayerTimes = schedule.todayPrayers,
      nextPrayer = schedule.nextPrayer,
      gregorianDateString = gregorianDate,
      hijriDateString = hijriDate,
      qiblahBearingDegrees = qiblahBearing
    )
    updateCountdown()
    notificationManager.schedulePrayerAlerts(schedule.todayPrayers, state.prayerAlerts, isArabic)
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
    val diff = next.timestampMillis - nowMillis

    if (diff <= 0L) {
      refreshTimes(currentIsArabic)
      return
    }

    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

    val countdownText = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
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
    prefs.edit().putString("selected_muadhin_id", muadhin.id).apply()
    playerManager.selectMuadhin(muadhin)
  }

  fun togglePlayAdhan(isArabic: Boolean = currentIsArabic) {
    val state = _uiState.value.adhanPlayerState.state
    if (state == AdhanPlaybackState.PLAYING) {
      playerManager.pause()
    } else {
      playerManager.playCurrent(isArabic)
    }
  }

  fun playOfflineChimeFallback() {
    playerManager.playOfflineChimeFallback()
  }

  fun seekAdhan(positionMs: Int) {
    playerManager.seekTo(positionMs)
  }

  fun stopAdhan() {
    playerManager.stop()
  }

  override fun onCleared() {
    super.onCleared()
    playerManager.release()
  }
}
