package com.example

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.test.core.app.ApplicationProvider
import com.example.core.adhan.CalculationMethod
import com.example.core.adhan.GpsLocationResolver
import com.example.core.adhan.GpsLocationResult
import com.example.core.adhan.PrayerTimesManager
import com.example.core.notification.AppNotificationManager
import com.example.core.permissions.DeviceAccessManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DeviceAccessAndGpsTest {

  private lateinit var context: Application

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    context.getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    context.getSharedPreferences("app_notification_prefs", Context.MODE_PRIVATE).edit().clear().commit()
  }

  @Test
  fun `DeviceAccessManager reflects granted and denied permissions accurately`() {
    val shadowApp = Shadows.shadowOf(context)
    shadowApp.denyPermissions(
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.POST_NOTIFICATIONS
    )

    val deniedStatus = DeviceAccessManager.getStatus(context)
    assertFalse(deniedStatus.hasLocationPermission)
    assertFalse(deniedStatus.hasNotificationPermission)

    shadowApp.grantPermissions(
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.POST_NOTIFICATIONS
    )

    val grantedStatus = DeviceAccessManager.getStatus(context)
    assertTrue(grantedStatus.hasLocationPermission)
    assertTrue(grantedStatus.isPreciseLocationGranted)
    assertTrue(grantedStatus.hasNotificationPermission)
  }

  @Test
  fun `inferCalculationMethodForCoordinates selects accurate regional authorities`() {
    assertEquals(
      CalculationMethod.EGYPT,
      PrayerTimesManager.inferCalculationMethodForCoordinates(30.0444, 31.2357)
    )
    assertEquals(
      CalculationMethod.UMM_AL_QURA,
      PrayerTimesManager.inferCalculationMethodForCoordinates(21.4225, 39.8262)
    )
    assertEquals(
      CalculationMethod.ISNA,
      PrayerTimesManager.inferCalculationMethodForCoordinates(40.7128, -74.0060)
    )
    assertEquals(
      CalculationMethod.KARACHI,
      PrayerTimesManager.inferCalculationMethodForCoordinates(24.8607, 67.0011)
    )
    assertEquals(
      CalculationMethod.MWL,
      PrayerTimesManager.inferCalculationMethodForCoordinates(51.5072, -0.1276)
    )
  }

  @Test
  fun `GPS city location persists to SharedPreferences and is used by AppNotificationManager`() {
    val prefs = context.getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
    val gpsCity = PrayerTimesManager.createGpsCityLocation(
      latitude = 21.4225,
      longitude = 39.8262,
      localityAr = "مكة المكرمة",
      localityEn = "Makkah",
      timeZoneId = "Asia/Riyadh"
    )
    PrayerTimesManager.saveCitySelection(prefs, gpsCity)

    val loaded = PrayerTimesManager.loadSavedCityLocation(prefs)
    assertEquals(PrayerTimesManager.GPS_CITY_ID, loaded.id)
    assertEquals(21.4225, loaded.latitude, 0.0001)
    assertEquals(39.8262, loaded.longitude, 0.0001)
    assertEquals(CalculationMethod.UMM_AL_QURA, loaded.defaultMethod)

    // Schedule prayer alarms from prefs using saved GPS location
    val notifManager = AppNotificationManager(context)
    notifManager.scheduleAllPrayerAlertsFromPrefs()
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    assertEquals(5, Shadows.shadowOf(alarmManager).scheduledAlarms.size)
  }

  @Test
  fun `GpsLocationResolver handles permission denied and resolves cached GPS fix when granted`() = runBlocking {
    val shadowApp = Shadows.shadowOf(context)
    shadowApp.denyPermissions(
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val deniedResult = GpsLocationResolver.detectCurrentLocation(context)
    assertTrue(deniedResult is GpsLocationResult.PermissionDenied)

    // Now grant permission and simulate a fresh GPS location fix
    shadowApp.grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val shadowLoc = Shadows.shadowOf(locationManager)
    shadowLoc.setProviderEnabled(LocationManager.GPS_PROVIDER, true)
    shadowLoc.setLocationEnabled(true)

    val fix = Location(LocationManager.GPS_PROVIDER).apply {
      latitude = 30.0444
      longitude = 31.2357
      time = System.currentTimeMillis()
      accuracy = 12f
    }
    shadowLoc.setLastKnownLocation(LocationManager.GPS_PROVIDER, fix)

    val successResult = GpsLocationResolver.detectCurrentLocation(context)
    assertTrue("Expected Success but got $successResult", successResult is GpsLocationResult.Success)
    val gpsCity = (successResult as GpsLocationResult.Success).cityLocation
    assertEquals(PrayerTimesManager.GPS_CITY_ID, gpsCity.id)
    assertEquals(30.0444, gpsCity.latitude, 0.0001)
    assertEquals(31.2357, gpsCity.longitude, 0.0001)
    assertEquals(CalculationMethod.EGYPT, gpsCity.defaultMethod)
  }
}
